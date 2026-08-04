package com.battlesea.server;

import com.battlesea.enums.Cell;
import com.battlesea.enums.GameMode;
import com.battlesea.model.*;
import com.battlesea.service.AIService;
import com.battlesea.service.BattleService;
import com.battlesea.service.GameService;
import com.battlesea.service.ShipPlacementService;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private GameSession gameSession;
    private AIService aiService;
    private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);
    private boolean turnAI;
    private BattleService battleService = new BattleService();

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

        log.debug("Player connected to server");
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
                    log.debug("avtorizovan igrok: {}", username);
                    Message response = new Message();
                    response.setType("AUTH_SUCCESS");
                    response.setCurrentPlayer(player);
                    out.println(gson.toJson(response));
                    break;
                }
            }

            while (true) {
                json = in.readLine();
                Message input = gson.fromJson(json, Message.class);

                if ("PVE_AUTO".equals(input.getType())) {
                    log.debug("PVE_AUTO");
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
                    log.debug("START_GAME_PVE");
                    game = gameService.startGame(player, playerBoard1, GameMode.PVE);
                    log.debug("game: {}", game);
                    gameSession = new GameSession(game, this);
                    log.debug("gameSession: {}", gameSession);
                    if (game.getTurnPlayer().equals(game.getOpponent())) {
                        turnAI = true;
                    }
                    Message response = new Message();
                    response.setType("START_GAME_PVE_SUCCESS");
                    response.setGame(game);
                    String responseJson = gson.toJson(response);
                    out.println(responseJson);
                }

                if ("START_GAME_PVP_ONLINE".equals(input.getType())) {
                    log.debug("START_GAME_PVP_ONLINE");
                    Message response = new Message();
                    game = gameService.startGame(player, playerBoard1, GameMode.PVP_ONLINE);
                    gameSession = new GameSession(game, this);
                    aiService = gameSession.getAiService();
                    int waitingTime = 0;
                    int maxWaitingTime = 60000;
                    while (true) {
                        if (waitingTime >= maxWaitingTime) {
                            gameService.deleteGameFromCreatedGames(game);
                            response.setType("TIMEOUT");
                            log.info("TIMEOUT");
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
                    gameSession.setOpponentHandler(gameServer.getClientHandler(game.getOpponent()));
                    response.setType("START_GAME_PVP_ONLINE_SUCCESS");
                    response.setGame(game);
                    broadcastToGamePlayers(gameServer, response);
                }

                if ("SHOOT".equals(input.getType())) {
                    log.debug("SHOOT");
                    Message response = new Message();
                    int x = input.getX();
                    int y = input.getY();
                    game.setTurnPlayer(player);
                    Cell resultShoot = battleService.shoot(game, new Coordinate(x, y));
                    if (resultShoot == null) {
                        continue;
                    }
                    game = battleService.getGame();

                    response.setType("RESULT_SHOOT");
                    response.setGame(game);
                    response.setX(x);
                    response.setY(y);

                    response.setResultShoot(resultShoot);

                    log.debug("response: {}", response);

                    broadcastToGamePlayers(gameServer, response);

                    if (resultShoot == Cell.MISS) {
                        turnAI = true;
                    }

                    isGameOver(gameServer);
                }

                if (turnAI) {
                    log.debug("TurnAI={}", turnAI);
                    while (turnAI) {
                        log.debug("ClientHandler    start move ai");
                        aiService = gameSession.getAiService();
                        Coordinate coordinate = aiService.chooseCoordinate();
                        log.debug("ClientHandler    coordinate: {}", coordinate);
                        Cell resultShoot = battleService.shoot(game, coordinate);
                        log.debug("ClientHandler   resultShoot: {}", resultShoot );
                        if (resultShoot == Cell.MISS) {
                            turnAI = false;
                        }
                        if (resultShoot == Cell.HIT && !aiService.isSunk(coordinate)) {
                            if (aiService.isHasHit()) {
                                aiService.setOrientation(coordinate);
                            }

                            if (!aiService.isHasHit()) {
                                aiService.setCoordinateHit(coordinate);
                            }
                            aiService.setHasHit(true);
                        }
                        aiService.removeCoordinate(coordinate);
                        Message response = new Message();
                        response.setType("RESULT_SHOOT");
                        response.setGame(game);

                        response.setResultShoot(resultShoot);

                        broadcastToGamePlayers(gameServer, response);
                        if (resultShoot == Cell.MISS) {
                            turnAI = false;
                        }

                        isGameOver(gameServer);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error in ClientHandler: {}", e.getMessage(), e);
        }
    }

    private void isGameOver(GameServer gameServer) {
        boolean gameOver = battleService.isGameOver();
        if (gameOver) {
            log.info("Game Over");
            battleService.winner(game);
            System.out.println(game);
            Message response = new Message();
            response.setType("GAME_OVER");
            response.setGame(game);
            broadcastToGamePlayers(gameServer, response);
        }
    }

    private void broadcastToGamePlayers(GameServer gameServer, Message message) {
        if (game == null) {
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
