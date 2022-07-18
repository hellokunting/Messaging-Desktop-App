package com.kunting.server;

import com.kunting.bean.Message;
import com.kunting.bean.MessageType;
import com.kunting.bean.User;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Handler implements Runnable{
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private OutputStream outputStream;
    private ObjectInputStream objectInputStream;
    private InputStream inputStream;

    private User user;

    public Handler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            inputStream = socket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);

            outputStream = socket.getOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);


            Message firstMessage = (Message) objectInputStream.readObject();

            if (nameExisted(firstMessage)) {
                System.out.println("Name exists");
                return;
            }

            sendNotificationToOther(firstMessage);

            // add new user's outputstream into hashset
            Server.messagers.add(objectOutputStream);
            sendNotificationToCurrent(firstMessage);
            showUsers();

            while (socket.isConnected()) {
                Message m = (Message) objectInputStream.readObject();
                switch (m.getType()) {
                    case TEXT:
                        sendMessage(m);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                closeConnection();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //  all users receive notification that new user entering the chatting room
    private void sendNotificationToOther(Message message) throws IOException {
        message.setMsg("Enters the Chatting Room");
        message.setType(MessageType.NOTIFICATION);

        sendMessage(message);
    }

    private void sendNotificationToCurrent(Message message) throws IOException {
        message.setMsg("Welcome to the chatting room");
        message.setName("Server");
        message.setType(MessageType.WELCOME);
        objectOutputStream.writeObject(message);
    }

    private void showUsers() throws IOException {
        Message message = new Message();
        message.setMsg("Welcome");
        message.setName("Server");
        message.setType(MessageType.JOINED);
        sendMessage(message);
    }


    // send messages to all users

    private void sendMessage(Message message) throws IOException {

        // set online users
        message.setOnlineUsers(new ArrayList<>(Server.userMap.values()));
        for (ObjectOutputStream messenger : Server.messagers) {
            messenger.writeObject(message);
        }
    }

    // user login name check duplicates
    // thread safety issue:
    // two or more threads simultaneously enter the same name
    private synchronized boolean nameExisted(Message message) throws IOException, ClassNotFoundException {
        if (!Server.userMap.containsKey(message.getName())) {
            user = new User();
            user.setName(message.getName());
            user.setPicture(message.getPicture());

            Server.userMap.put(user.getName(), user);
            return false;
        }
        message.setMsg("The nickname you entered already exists! Try again.");
        message.setType(MessageType.ERROR);

        // return the error message to the user who tries to login with duplicated name
        this.objectOutputStream.writeObject(message);
        return true;
    }

    private void closeConnection() throws IOException {
        if (user.getName() != null) {
            Server.userMap.remove(user.getName());
        }

        if (user.getName() != null) {
            Server.messagers.remove(objectOutputStream);
        }

        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (objectOutputStream != null) {
            try {
                objectOutputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (objectInputStream != null) {
            try {
                objectInputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // remove user from other users' list
        Message message = new Message();
        message.setMsg("Leave the Chatting Room");
        message.setName("Server");
        message.setType(MessageType.DISCONNECTED);
        sendMessage(message);
    }
}
