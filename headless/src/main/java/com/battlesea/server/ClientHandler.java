package com.battlesea.server;

import com.battlesea.enums.Cell;
import com.battlesea.enums.GameMode;
import com.battlesea.model.Board;
import com.battlesea.model.Game;
import com.battlesea.model.Message;
import com.battlesea.model.Player;
import com.battlesea.service.BattleService;
import com.battlesea.service.GameService;
import com.battlesea.service.ShipPlacementService;
import com.google.gson.*;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class ClientHandler {
    private final Socket socket;
    private final GameServer gameServer;
    private final BufferedReader in;
    private final PrintWriter out;
    private final Gson gson;
    private Board playerBoard1;
    private Game game;
    private final GameService gameService;

    public ClientHandler(Socket socket, GameServer gameServer) throws Exception {
        this.socket = socket;
        this.gameServer = gameServer;
        this.gameService = new GameService();
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

                if ("PVE_AUTO".equals(message.getType())) {
                    ShipPlacementService service = new ShipPlacementService();
                    playerBoard1 = service.generateRandomShips(player);
                    Message response = new Message();
                    response.setType("PVE_SUCCESS");
                    response.setBoardPlayer1(playerBoard1);

                    String responseJson = gson.toJson(response);
                    out.println(responseJson);
                }

                if ("START_GAME_PVE".equals(message.getType())) {
                    game = gameService.startGame(player, playerBoard1, GameMode.PVE);
                    Message response = new Message();
                    response.setType("START_GAME_PVE_SUCCESS");
                    response.setGame(game);

                    String responseJson = gson.toJson(response);
                    out.println(responseJson);
                }

                if ("START_GAME_PVP_ONLINE".equals(message.getType())) {
                    Message response = new Message();
                    game = gameService.startGame(player, playerBoard1, GameMode.PVP_ONLINE);
                    System.out.println(game.getPlayer1());
                    System.out.println(game.getPlayer2());
                    LocalTime timeStartWaiting = LocalTime.now();
                    while (true) {
                        if (ChronoUnit.MINUTES.between(timeStartWaiting, LocalTime.now()) >= 3) {
                            response.setType("Timeout");
                            out.println(gson.toJson(response));
                            break;
                        }
                        if (game.getPlayer2() != null)
                            break;
                    }
                    if (game.getPlayer2() == null) {
                        continue;
                    }
                    response.setType("START_GAME_PVP_ONLINE_SUCCESS");
                    response.setGame(game);

                    String responseJson = gson.toJson(response);
                    out.println(responseJson);


                }

                if ("SHOOT".equals(message.getType())) {
                    int x = message.getX();
                    int y = message.getY();
                    BattleService battleService = new BattleService();
                    game.setTurnPlayer(player);
                    Cell resultShoot = battleService.shoot(game, x, y);
                    game = battleService.getGame();

                    Message response = new Message();
                    response.setType("RESULT_SHOOT");
                    response.setGame(game);
                    response.setX(x);
                    response.setY(y);
                    if (resultShoot == null) {
                        String responseJson = gson.toJson(response);
                        out.println(responseJson);
                    } else {
                        response.setResultShoot(resultShoot);
                        String responseJson = gson.toJson(response);
                        out.println(responseJson);

                        boolean gameOver = battleService.isGameOver();
                        if (gameOver) {
                            Message gameOverResponse = new Message();
                            gameOverResponse.setType("GAME_OVER");
                            String gameOverResponseJson = gson.toJson(gameOverResponse);
                            out.println(gameOverResponseJson);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
