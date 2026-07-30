package com.battlesea.server;

import com.battlesea.enums.GameMode;
import com.battlesea.model.Board;
import com.battlesea.model.Message;
import com.battlesea.model.Player;
import com.battlesea.service.GameService;
import com.battlesea.service.ShipPlacementService;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ClientHandler {
    private final Socket socket;
    private final GameServer gameServer;
    private final DataInputStream in;
    private final DataOutputStream out;

    public ClientHandler(Socket socket, GameServer gameServer) throws Exception {
        this.socket = socket;
        this.gameServer = gameServer;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());


        new Thread(() -> {
            String json;
            Message message;
            Gson gson = new Gson();
            Player player;
            try {
                while (true) {
                    json = in.readUTF();
                    message = gson.fromJson(json, Message.class);

                    if ("AUTH".equals(message.getType())) {
                        String username = message.getUsername();
                        player = new Player(username);
                        System.out.println("Авторизован игрок: " + username);

                        Message response = new Message();
                        response.setType("AUTH_SUCCESS");
                        out.write(gson.toJson(response).getBytes());
                        break;
                    }
                }

                while (true) {
                    json = in.readUTF();
                    message = gson.fromJson(json, Message.class);
                    if ("PVE_AUTO".equals(message.getType())) {
                        ShipPlacementService service = new ShipPlacementService();
                        Board playerBoard = service.generateRandomShips(player);
                        GameService gameService = new GameService();
                        gameService.startGame(player, playerBoard, GameMode.PVE);
                        Message response = new Message();
                        response.setType("PVE_SUCCESS");
                        response.setResponse(playerBoard);
                        out.write(gson.toJson(response).getBytes());
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
