package com.battlesea.Client;

import com.battlesea.model.Board;
import com.battlesea.model.Game;
import com.battlesea.model.Message;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;

public class Client {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final Gson gson;
    private Board board;
    private Game game;

    public Client(String host, int port) throws Exception {
        this.socket = new Socket(host, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.gson = new Gson();

        new Thread(() -> {

            try {
                while (true) {
                    Message message = new Message();
                    message.setType("AUTH");
                    message.setUsername("qwerty");

                    out.println(gson.toJson(message));

                    String json = in.readLine();
                    if (json == null) {
                        break;
                    }

                    message = gson.fromJson(json, Message.class);
                    System.out.println("message: " + message);
                    if ("AUTH_SUCCESS".equals(message.getType())) {
                        System.out.println("AUTH_SUCCESS");
                        break;
                    }
                }

                while(true){
//                    Message message = new Message();
//                    message.setType("PVE_AUTO");
//                    out.println(gson.toJson(message));


                    String json = in.readLine();
                    if (json == null) {
                        break;
                    }
                    Message message = gson.fromJson(json, Message.class);
                    if ("PVE_SUCCESS".equals(message.getType())) {
                        System.out.println("PVE_SUCCESS");
//                        Game game = message.getGame();

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

    public Board getBoard() {
        return board;
    }

    public Game getGame() {
        return game;
    }

    public void sendAutoPlaceRequest() {
        Message message = new Message();
        message.setType("PVE_AUTO");
        out.println(gson.toJson(message));
    }
}
