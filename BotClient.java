package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.ConsoleHelper;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BotClient extends Client {

    public class BotSocketThread extends SocketThread{
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage( "Привет чатику. Я бот. "
                +  "Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды,"
                + " кто олень");
            super.clientMainLoop();
        }

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            if (message.contains(":")) {
                String[] messageParts = message.split(":");
                if( messageParts.length !=2) {return;}
                String userName = messageParts[0];
                String request = messageParts[1];
                Calendar calendar = Calendar.getInstance();
                Date curTime = calendar.getTime();
                SimpleDateFormat dateFormat = null;

                switch (request.trim()){
                    case  "дата" :
                        dateFormat = new SimpleDateFormat("d.MM.YYYY");
                        break;
                    case  "день" :
                        dateFormat = new SimpleDateFormat("d");
                        break;
                    case "месяц" :
                        dateFormat = new SimpleDateFormat("MMMM");
                        break;
                    case "год" :
                        dateFormat = new SimpleDateFormat("YYYY");
                        break;
                    case "время" :
                        dateFormat = new SimpleDateFormat("H:mm:ss");
                        break;
                    case "час" :
                        dateFormat = new SimpleDateFormat("H");
                        break;
                    case "минуты" :
                        dateFormat = new SimpleDateFormat("m");
                        break;
                    case "секунды" :

                        dateFormat = new SimpleDateFormat("s");
                        break;
                    case "кто олень" :
                        sendTextMessage(String.format("Информация для %s: ОЛЕНЬ - лесное животное с рогами", userName));
                         return;
                    default:
                        return;
                }

                if (dateFormat != null) {
                    sendTextMessage(String.format("Информация для %s: %s", userName, dateFormat.format(curTime)));
                }
            }
        }
    }


    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();

    }

    protected boolean shouldSendTextFromConsole(){
        return false;
    }

    protected String getUserName() {
        // ConsoleHelper.writeMessage("Укажите свое имя:");
        return "date_bot_" + (int) (Math.random() * 100);
    }
    public static void main(String[] args){
        BotClient botClient = new BotClient();
        botClient.run();
    }
}
