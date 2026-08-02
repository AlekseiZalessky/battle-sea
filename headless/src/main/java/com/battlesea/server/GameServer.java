package com.battlesea.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class GameServer {
    private final int port;
    private ExecutorService executor;

    public GameServer(int port) {
        this.port = port;
        this.executor = Executors.newFixedThreadPool(5);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                executor.execute(() ->{
                    try {
                        new ClientHandler(socket, this);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
