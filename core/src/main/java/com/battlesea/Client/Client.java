package com.battlesea.Client;

import com.battlesea.model.Board;
import com.battlesea.model.Message;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class Client {
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;

    public Client(String host, int port) throws Exception {
        this.socket = new Socket(host, port);
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());

        new Thread(() -> {
            Gson gson = new Gson();
            try {
                while (true) {
                    Message message = new Message();
                    message.setType("AUTH");
                    message.setUsername("qwerty");

                    out.write(gson.toJson(message).getBytes());

                    String json = in.readUTF();
                    message = gson.fromJson(json, Message.class);
                    if (message.getType().equals("AUTH_SUCCESS")) {
                        break;
                    }

                }
                while (true) {
                    String json = in.readUTF();
                    Message message = gson.fromJson(json, Message.class);
                    if (message.getType().equals("PVE_SUCCESS")) {
                        Board board = gson.fromJson(json, Board.class);
                        board.printCells();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
