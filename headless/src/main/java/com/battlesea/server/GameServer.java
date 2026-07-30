package com.battlesea.server;

import java.net.ServerSocket;
import java.net.Socket;


public class GameServer {
    private final int port;

    public GameServer(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
