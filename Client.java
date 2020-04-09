package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.*;

import java.io.IOException;
import java.net.Socket;

public class Client {
    protected  Connection connection;
    private volatile boolean clientConnected = false;

    protected  String getServerAddress(){
        ConsoleHelper.writeMessage("Введите адрес сервера:");
        return ConsoleHelper.readString();
    }

    protected int getServerPort(){
        ConsoleHelper.writeMessage("Введите адрес порта сервера:");
        return ConsoleHelper.readInt();
    }

    protected String getUserName(){
        ConsoleHelper.writeMessage("Укажите свое имя:");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole(){
        return true;
    }

     protected void sendTextMessage(String text)  {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Не удалось отправить сообщение");
            clientConnected = false;
        }
    }

    protected SocketThread getSocketThread(){
         return new SocketThread();
    }


    public static void main(String[] args){
        Client client = new Client();
        client.run();

    }
    public void run(){
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();

        synchronized (this) {    // это не клиент, а объект внутреннего класса клиента "SocketThread". Т.е. "основной поток".
            try {
                wait();
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage("Ошибка при ожидании пробуждения потока");
                return;
            }
            if (clientConnected) {ConsoleHelper.writeMessage("Соединение установлено. (для выхода exit");}
            else {ConsoleHelper.writeMessage("Ошибка во время работы клиента");
            }

            String text;
            while (clientConnected){
                text = ConsoleHelper.readString();
                if (text.equals("exit")) { break;}
                if (shouldSendTextFromConsole()) { sendTextMessage(text);}
            }
        }
    }


    public class SocketThread extends Thread{

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage("участник с именем " + userName + " присоединился к чату.");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage("участник с именем " + userName + " покинул чат.");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        protected void  clientHandshake() throws IOException, ClassNotFoundException {
            String userName;
            Message answer;
            do {
                answer = Client.this.connection.receive();
                if(answer.getType() == MessageType.NAME_REQUEST) {
                    userName = getUserName();
                    Message newMessage = new Message(MessageType.USER_NAME, userName);
                    connection.send(newMessage);
                } else if(answer.getType() == MessageType.NAME_ACCEPTED) {
                            notifyConnectionStatusChanged(true);
                            return;
                        } else { throw new IOException("Unexpected MessageType");
                }
            }while (true);
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            Message answer;
            do {
                answer = Client.this.connection.receive();
                if (answer.getType() == MessageType.TEXT) {
                    processIncomingMessage(answer.getData());
                } else if (answer.getType() == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(answer.getData());
                } else if (answer.getType() == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(answer.getData());
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }while (true);
        }

        public void run(){
            try (Socket socket = new Socket(getServerAddress(), getServerPort())) {
                Client.this.connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }
    }
}
