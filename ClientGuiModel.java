package com.javarush.task.task30.task3008.client;


import java.util.HashSet;
import java.util.Set;

public class ClientGuiModel {
    private String newMessage;
    private final Set<String>  allUserNames = new HashSet<>();

    public Set<String> getAllUserNames() {
        return allUserNames;
    }

    public String getNewMessage() {
        return newMessage;
    }

    public void setNewMessage(String newMessage) {
        this.newMessage = newMessage;
    }

    public void addUser(String userName){
        allUserNames.add(userName);
     }

    public void deleteUser(String userName){
        if (allUserNames.contains(userName)) {
            allUserNames.remove(userName);
        }
    }
}
