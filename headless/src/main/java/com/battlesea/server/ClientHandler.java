package com.battlesea.server;

import com.battlesea.enums.GameMode;
import com.battlesea.model.Board;
import com.battlesea.model.Game;
import com.battlesea.model.Message;
import com.battlesea.model.Player;
import com.battlesea.service.GameService;
import com.battlesea.service.ShipPlacementService;
import com.google.gson.*;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClientHandler {
    private final Socket socket;
    private final GameServer gameServer;
    private final BufferedReader in;
    private final PrintWriter out;
    private final Gson gson;
    private Board playerBoard1;

    public ClientHandler(Socket socket, GameServer gameServer) throws Exception {
        this.socket = socket;
        this.gameServer = gameServer;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        // ← НАСТРАИВАЕМ GSON ДЛЯ РАБОТЫ С LocalDateTime
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class,
                (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                    src == null ? null : new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class,
                (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                    LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .create();

        new Thread(() -> {
            System.out.println("polucheno podkluchenie");
            String json;

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

                while (true) {
                    json = in.readLine();
                    Message message = gson.fromJson(json, Message.class);
                    System.out.println("ClientHandler: " + message);

                    if ("PVE_AUTO".equals(message.getType())) {
                        ShipPlacementService service = new ShipPlacementService();
                        playerBoard1 = service.generateRandomShips(player);

                        Message response = new Message();
                        response.setType("PVE_SUCCESS");
                        response.setBoardPlayer1(playerBoard1);

                        String responseJson = gson.toJson(response);
                        out.println(responseJson);
                    }

                    if("START_GAME_PVE".equals(message.getType())) {
                        GameService gameService = new GameService();
                        Game game = gameService.startGame(player, playerBoard1, GameMode.PVE);
                        Message response = new Message();
                        response.setType("START_GAME_PVE_SUCCESS");
                        response.setGame(game);

                        String responseJson = gson.toJson(response);
                        out.println(responseJson);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
