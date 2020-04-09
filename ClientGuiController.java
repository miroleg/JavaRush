package com.javarush.task.task30.task3008.client;

public class ClientGuiController extends Client {
    private ClientGuiModel model = new ClientGuiModel();
    private ClientGuiView view = new ClientGuiView(this);

    public ClientGuiModel getModel() {
        return model;
    }

    protected  SocketThread getSocketThread(){
        return new GuiSocketThread();
    }


    public static void main(String[] args){
        ClientGuiController clientGuiController = new ClientGuiController();
        clientGuiController.run();

    }
    public void run() {

        SocketThread socketThread = getSocketThread();
        socketThread.run();
    }

    public int getServerPort() {
        return view.getServerPort();
    }
    public String getServerAddress() {
        return view.getServerAddress();
    }
    public String getUserName(){
        return view.getUserName();
    }



        public class GuiSocketThread extends  SocketThread{
        protected  void processIncomingMessage(String message){
            model.setNewMessage(message);
            view.refreshMessages();
        }

        protected  void informAboutAddingNewUser(String userName){
            model.addUser(userName);
            view.refreshUsers();
        }

        protected  void informAboutDeletingNewUser(String userName){
            model.deleteUser(userName);
            view.refreshUsers();
        }


        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            super.notifyConnectionStatusChanged(clientConnected);  // ?? super
            view.notifyConnectionStatusChanged(clientConnected);

        }
    }
}
