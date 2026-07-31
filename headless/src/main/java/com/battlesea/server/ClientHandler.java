package com.battlesea.server;

import com.battlesea.enums.GameMode;
import com.battlesea.model.Board;
import com.battlesea.model.Game;
import com.battlesea.model.Message;
import com.battlesea.model.Player;
import com.battlesea.service.GameService;
import com.battlesea.service.ShipPlacementService;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;

public class ClientHandler {
    private final Socket socket;
    private final GameServer gameServer;
    private final BufferedReader in;
    private final PrintWriter out;

    public ClientHandler(Socket socket, GameServer gameServer) throws Exception {
        this.socket = socket;
        this.gameServer = gameServer;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);


        new Thread(() -> {
            System.out.println("polucheno podkluchenie");
            String json;

            Gson gson = new Gson();
            Player player;
            try {
                while (true) {
                    json = in.readLine();
                    Message message = gson.fromJson(json, Message.class);

                    if ("AUTH".equals(message.getType())) {
                        String username = message.getUsername();
                        player = new Player(username);
                        System.out.println("avtorizovan igrok: " + username);

                        Message response = new Message();
                        response.setType("AUTH_SUCCESS");
                        out.println(gson.toJson(response));
                        break;
                    }
                }
                System.out.println("*****************************");

                while (true) {
                    json = in.readLine();
                    Message message = gson.fromJson(json, Message.class);

                    if ("PVE_AUTO".equals(message.getType())) {
                        ShipPlacementService service = new ShipPlacementService();
                        Board playerBoard = service.generateRandomShips(player);

                        GameService gameService = new GameService();
                        Game game = gameService.startGame(player, playerBoard, GameMode.PVE);

                        Message response = new Message();
                        response.setType("PVE_SUCCESS");
                        response.setGame(game);


                        String responseJson = gson.toJson(response);
                        out.println(responseJson);
                        System.out.println("message send");
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
