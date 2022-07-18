package com.kunting.server;

import com.kunting.bean.User;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {

    public static Map<String, User> userMap = new HashMap<>(); //store username and user object pair
    public static Set<ObjectOutputStream> messagers = new HashSet<>(); // stores user's output stream

    // Server thread pool
    public static ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(16, 32,
            1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(16));

    public static void main(String[] args) {
        try {
            // 9001 is the port num of server
            ServerSocket listener = new ServerSocket(9001);

            // wait for client's side socket
            while (true) {
                // call server socket's accept() method to accept the socket from client side
                poolExecutor.execute(new Handler(listener.accept()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
