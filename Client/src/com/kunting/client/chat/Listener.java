package com.kunting.client.chat;

import com.kunting.bean.Message;
import com.kunting.bean.MessageType;
import com.kunting.client.login.LoginController;

import java.io.*;
import java.net.Socket;

public class Listener implements Runnable {
    private String hostname;
    private int port;
    private String username;
    private String picture;
    private Socket socket;

    private ChatController chatController;

    private ObjectInputStream objectInputStream;
    private InputStream inputStream;
    private ObjectOutputStream objectOutputStream;
    private OutputStream outputStream;

    public static Listener instance;

    public Listener(String hostname, int port, String username, String picture, ChatController chatController) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.picture = picture;
        this.chatController = chatController;
        instance = this;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(hostname, port);
            outputStream = socket.getOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);
            inputStream = socket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);

            connect();

            while (socket.isConnected()) {
                Message message = (Message) objectInputStream.readObject();

                if (message != null) {
                    switch (message.getType()) {
                        case NOTIFICATION:
                            LoginController.getInstance().showScene();
                            chatController.notify(message.getName() + " enters the room", message.getPicture(),
                                    "New user enters the room", "sounds/user-enter-notify.mp3");
                            break;
                        case ERROR:
                            chatController.notify(message.getMsg(), message.getPicture(), "Problem occurs",
                                    "sounds/error.mp3");
                            break;
                        case WELCOME:
                            LoginController.getInstance().showScene();
                            chatController.notify(message.getMsg(), message.getPicture(), "Welcome",
                                    "sounds/start.mp3");
                            break;
                        case JOINED:
                        case DISCONNECTED:
                            chatController.setUserList(message);
                            break;
                        case TEXT:
                            chatController.showMsg(message);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void connect() throws IOException {
        Message message = new Message();
        message.setName(username);
        message.setType(MessageType.JOINED);
        message.setPicture(picture);
        message.setMsg("Connected");

        objectOutputStream.writeObject(message);
    }

    public void send(String msg) throws IOException {
        Message message = new Message();
        message.setName(username);
        message.setType(MessageType.TEXT);
        message.setPicture(picture);
        message.setMsg(msg);

        objectOutputStream.writeObject(message);
        objectOutputStream.flush();
    }

}
