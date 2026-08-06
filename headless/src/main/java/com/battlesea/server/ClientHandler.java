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
import java.util.concurrent.*;

public class ClientHandler {
    private final Socket socket;
    private final GameServer gameServer;
    private final BufferedReader in;
    private final PrintWriter out;
    private final Gson gson;
    private Board playerBoard;
    private Game game;
    private final GameService gameService;
    private GameSession gameSession;
    private AIService aiService;
    private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);
    private boolean turnAI;
    private BattleService battleService;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> timeoutTask;
    private boolean gameOver;
    private final int TURN_TIME = Game.TURN_TIME;

    private void updateFields(){
        playerBoard = null;
        game = null;
        gameSession = null;
        aiService = null;
        turnAI = false;
        gameOver = false;
        battleService = null;
    }

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
                log.debug("input: {}", input);
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

                if("UPDATE_FIELDS".equals(input.getType())) {
                    log.debug("UPDATE_FIELDS");
                    updateFields();
                }

                if ("AUTO_PLACE".equals(input.getType())) {
                    log.debug("AUTO_PLACE");
                    ShipPlacementService service = new ShipPlacementService();
                    playerBoard = service.generateRandomShips(player);

                    Message response = new Message();
                    response.setType("AUTO_PLACE_SUCCESS");
                    response.setBoardCreator(playerBoard);
                    String responseJson = gson.toJson(response);
                    out.println(responseJson);
                }

                if ("START_GAME_PVE".equals(input.getType())) {

                    log.debug("START_GAME_PVE");
                    game = gameService.startGame(player, playerBoard, GameMode.PVE);
                    log.debug("game: {}", game);
                    gameSession = gameServer.createSession(game);
                    battleService = gameSession.getBattleService();
                    log.debug("game.getTurnPlayer(): {}", game.getTurnPlayer());
                    log.debug("game.getOpponent(): {}", game.getOpponent());
                    log.debug("game.getTurnPlayer().equals(game.getOpponent()): {}", game.getTurnPlayer().equals(game.getOpponent()));
                    if (game.getTurnPlayer().equals(game.getOpponent())) {
                        turnAI = true;
                    } else {
                        timer();
                    }
                    Message response = new Message();
                    response.setType("START_GAME_PVE_SUCCESS");
                    response.setGame(game);
                    response.setTimeStartTurn(System.currentTimeMillis());
                    String responseJson = gson.toJson(response);
                    out.println(responseJson);
                }

                if ("START_GAME_PVP_ONLINE".equals(input.getType())) {
                    log.debug("START_GAME_PVP_ONLINE");
                    Message response = new Message();
                    game = gameService.startGame(player, playerBoard, GameMode.PVP_ONLINE);
                    gameSession = new GameSession(game, this);
//                    aiService = gameSession.getAiService();
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
                    if(gameOver ){
                        continue;
                    }
                    log.debug("SHOOT");
                    Message response = new Message();
                    int x = input.getX();
                    int y = input.getY();
                    log.debug("player shoot coordinate: {},{}", x, y);
                    battleService = gameSession.getBattleService();
                    Cell resultShoot = battleService.shoot(new Coordinate(x, y));
                    if (resultShoot == null) {
                        continue;
                    }

                    response.setType("RESULT_SHOOT");
                    response.setGame(game);
                    response.setX(x);
                    response.setY(y);
                    response.setResultShoot(resultShoot);
                    response.setTimeStartTurn(System.currentTimeMillis());
                    broadcastToGamePlayers(gameServer, response);

                    if (game.getGameMode() == GameMode.PVE && resultShoot == Cell.MISS) {
                        turnAI = true;
                    }

                    isGameOver();
                    timer();
                }

                log.debug("turnAI: {}", turnAI);

                if (turnAI) {
                    if (gameOver) {
                        log.debug("turnAI gameOver");
                        continue;
                    }
                    if (battleService.getCounter() == 0) {
                        Thread.sleep(3000);
                    }
                    log.debug("TurnAI");
                    while (turnAI) {
                        aiService = gameSession.getAiService();
                        Coordinate coordinate = aiService.chooseCoordinate();
                        log.debug("ClientHandler    coordinate: {}", coordinate);
                        Cell resultShoot = battleService.shoot(coordinate);
                        log.debug("ClientHandler   resultShoot: {}", resultShoot);

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
                        aiService.updateFreeCoordinates();
                        Message response = new Message();
                        response.setType("RESULT_SHOOT");
                        response.setGame(game);
                        response.setResultShoot(resultShoot);
                        response.setTimeStartTurn(System.currentTimeMillis());
                        broadcastToGamePlayers(gameServer, response);

                        if (resultShoot == Cell.MISS) {
                            turnAI = false;
                            timer();
                            continue;
                        }

                        isGameOver();
                    }

                    if ("SWITCH_TURN".equals(input.getType())) {
                        log.debug("SWITCH_TURN");
                        if (game.getGameMode() == GameMode.PVE && game.getTurnPlayer().equals(game.getOpponent())) {
                            turnAI = true;
                        }
                    }

                }
            }
        } catch (Exception e) {
            log.error("Error in ClientHandler: {}", e.getMessage(), e);
        }
    }

    private void timer() {
        cancelTimer();

        int counter = battleService.getCounter();

        timeoutTask = scheduler.schedule(() -> {
            if (counter == battleService.getCounter()) {
                log.debug("TURN_TIMEOUT");
                battleService.switchTurnPlayer();
                if (game.getGameMode() == GameMode.PVE) {
                    turnAI = true;
                }
                Message response = new Message();
                response.setType("TURN_TIMEOUT");
                response.setGame(game);
                broadcastToGamePlayers(gameServer, response);
            }
        }, TURN_TIME, TimeUnit.SECONDS);
    }

    private void cancelTimer() {
        if (timeoutTask != null) {
            timeoutTask.cancel(false);
            timeoutTask = null;
        }
    }

    private void isGameOver() {
        gameOver = battleService.isGameOver();
        if (gameOver) {
            log.info("Game Over");
            cancelTimer();
            battleService.winner();
            turnAI = false;
            log.debug("game: {}", game);
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
