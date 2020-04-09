package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<String,Connection>();
    // где ключом будет имя клиента, а значением - соединение с ним.

    public static void main(String[] args) throws IOException {
        ConsoleHelper.writeMessage(" Введите порт для сервера:");
        try(ServerSocket serverSocket = new ServerSocket(ConsoleHelper.readInt())) {
            ConsoleHelper.writeMessage("Server started");
            //System.out.println(" Сервер запущен. Начинаем писать сообщения:");
            Socket socket;
            while (true){
                socket = serverSocket.accept();
                new Handler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage("Адрес"+socket.getRemoteSocketAddress());
            try {
                Connection connection = new Connection(socket);
                String name = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, name));
                notifyUsers(connection, name);
                serverMainLoop(connection, name);
                connectionMap.remove(name);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, name));
                ConsoleHelper.writeMessage("Соединение закрыто");
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом");
            }

        }




        /*
        public void run() {
         String  userName;
            ConsoleHelper.writeMessage("Connection established with " + socket.getRemoteSocketAddress());
            try (Connection connection = new Connection(socket)) {
                userName = serverHandshake(connection);
                if (!userName.isEmpty()) sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);
                if (!userName.isEmpty()) {
                    connectionMap.remove(userName);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                }
                ConsoleHelper.writeMessage("disconnected with " + userName);

            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом");
            }
        }

         */

        private String serverHandshake(Connection connection)throws IOException, ClassNotFoundException
        {
            String userName;
            Message request = new Message(MessageType.NAME_REQUEST, "Enter your name");
            Message answer;
            do
            {
                connection.send(request);
                answer = connection.receive();
                userName = answer.getData();

            }while((answer.getType() != MessageType.USER_NAME)
                    ||userName.isEmpty()
                    ||connectionMap.containsKey(userName));
            connectionMap.put(userName, connection);
            connection.send(new Message(MessageType.NAME_ACCEPTED, "Ваше имя принято!"));
            return userName;
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            Set<Map.Entry<String,Connection>> entrySet=connectionMap.entrySet();
            for (Map.Entry<String,Connection> pair : entrySet) {
                if (!userName.equals(pair.getKey())) {
                    connection.send(new Message(MessageType.USER_ADDED, pair.getKey()));
                }
            }

        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            Message answer;
            while (true){
                answer = connection.receive();
                if (answer.getType() == MessageType.TEXT) {
                    sendBroadcastMessage(new Message(MessageType.TEXT, userName + ": " + answer.getData()));
                }else {
                    ConsoleHelper.writeMessage(" Ошибка : тип сообщения должен быть TEXT");
                }
            }
        }
        
    }



    public static void sendBroadcastMessage(Message message){
        connectionMap.forEach((name, connection) -> {
           try {
                connection.send(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
