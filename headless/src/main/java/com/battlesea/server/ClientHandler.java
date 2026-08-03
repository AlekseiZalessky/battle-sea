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
import java.time.format.DateTimeFormatter;

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
                Message input = gson.fromJson(json, Message.class);

                if ("AUTH".equals(input.getType())) {
                    String username = input.getUsername();
                    player = new Player(username);
                    gameServer.registerPlayer(player, this);
                    System.out.println("avtorizovan igrok: " + username);
                    Message response = new Message();
                    response.setType("AUTH_SUCCESS");
                    response.setTurnPlayer(player);
                    out.println(gson.toJson(response));
                    break;
                }
            }

            while (true) {
                json = in.readLine();
                Message input = gson.fromJson(json, Message.class);

                if ("PVE_AUTO".equals(input.getType())) {

                    ShipPlacementService service = new ShipPlacementService();
                    playerBoard1 = service.generateRandomShips(player);
                    Message response = new Message();
                    response.setType("PVE_SUCCESS");
                    response.setCreator(player);
                    response.setBoardPlayer1(playerBoard1);

                    String responseJson = gson.toJson(response);
                    out.println(responseJson);
                }

                if ("START_GAME_PVE".equals(input.getType())) {
                    System.out.println("START_GAME_PVE");
                    game = gameService.startGame(player, playerBoard1, GameMode.PVE);
                    Message response = new Message();
                    response.setType("START_GAME_PVE_SUCCESS");
                    response.setGame(game);
                    String responseJson = gson.toJson(response);
                    out.println(responseJson);
                }

                if ("START_GAME_PVP_ONLINE".equals(input.getType())) {
                    Message response = new Message();
                    game = gameService.startGame(player, playerBoard1, GameMode.PVP_ONLINE);
                    System.out.println(game.getCreator());
                    System.out.println(game.getOpponent());
                    int waitingTime = 0;
                    int maxWaitingTime = 60000;
                    while (true) {
                        System.out.println("waiting time: " + waitingTime);
                        if (waitingTime >= maxWaitingTime) {
                            gameService.deleteGameFromCreatedGames(game);
                            response.setType("TIMEOUT");
                            out.println(gson.toJson(response));
                            break;
                        }
                        if (game.getOpponent() != null)
                            break;
                        Thread.sleep(1000);
                        waitingTime += 1000;
                    }
                    if (game.getOpponent() == null) {
                        continue;
                    }
                    response.setType("START_GAME_PVP_ONLINE_SUCCESS");
                    response.setGame(game);
                    broadcastToGamePlayers(gameServer, response);
                }

                if ("SHOOT".equals(input.getType())) {
                    Message response = new Message();
                    int x = input.getX();
                    int y = input.getY();
                    BattleService battleService = new BattleService();
                    game.setTurnPlayer(player);
                    Cell resultShoot = battleService.shoot(game, x, y);
                    game = battleService.getGame();

                    response.setType("RESULT_SHOOT");
                    response.setGame(game);
                    response.setX(x);
                    response.setY(y);

                    response.setResultShoot(resultShoot);

                    broadcastToGamePlayers(gameServer, response);

                    boolean gameOver = battleService.isGameOver();
                    if (gameOver) {
                        System.out.println("GAME OVER");
                        System.out.println(game);
                        battleService.winner(game);
                        System.out.println(game);
                        response = new Message();
                        response.setType("GAME_OVER");
                        response.setGame(game);
                        broadcastToGamePlayers(gameServer, response);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcastToGamePlayers(GameServer gameServer, Message message) {
        if(game == null) {
            throw new NullPointerException("game is null");
        }
        String json = gson.toJson(message);

        ClientHandler handler1 = gameServer.getClientHandler(game.getCreator());
        if (handler1 != null) {
            handler1.sendMessage(json);
        }

        ClientHandler handler2 = gameServer.getClientHandler(game.getOpponent());
        if (handler2 != null) {
            handler2.sendMessage(json);
        }
    }

    private void sendMessage(Message message) {
        out.println(gson.toJson(message));
    }

    private void sendMessage(String json) {
        out.println(json);
    }
}
