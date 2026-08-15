package com.battlesea.server;

import com.battlesea.constants.Commands;
import com.battlesea.enums.Cell;
import com.battlesea.enums.GameMode;
import com.battlesea.enums.GameStatus;
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
    private ScheduledFuture<?> timeWaiting;
    private boolean gameOver;
    private final int TURN_TIME = Game.TURN_TIME;
    private long lastActionTime;
    private Player player;


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

        try {
            while (true) {
                json = in.readLine();
                Message input = gson.fromJson(json, Message.class);
                log.debug("input: {}", input);
                if (Commands.AUTH.equals(input.getType())) {
                    player = new Player();
                    String username = player.getName();
                    gameServer.registerPlayer(player, this);
                    log.debug("avtorizovan igrok: {}", username);
                    Message response = new Message();
                    response.setType(Commands.AUTH_SUCCESS);
                    response.setCurrentPlayer(player);
                    out.println(gson.toJson(response));
                    break;
                }
            }

            while (true) {
                json = in.readLine();
                Message input = gson.fromJson(json, Message.class);

                if (Commands.UPDATE_FIELDS.equals(input.getType())) {
                    log.debug(Commands.UPDATE_FIELDS);
                    updateFields();
                }

                if (Commands.AUTO_PLACE.equals(input.getType())) {
                    log.debug("""
                            playerBoard: {},
                            game: {},
                            gameSession: {},
                            aiService: {},
                            turnAI: {},
                            gameOver: {},
                            battleService: {}"""
                        , playerBoard, game, gameSession, aiService, turnAI, gameOver, battleService);
                    log.debug(Commands.AUTO_PLACE);
                    ShipPlacementService service = new ShipPlacementService();
                    playerBoard = service.generateRandomShips(player);

                    Message response = new Message();
                    response.setType(Commands.AUTO_PLACE_SUCCESS);
                    response.setBoardCreator(playerBoard);
                    String responseJson = gson.toJson(response);
                    out.println(responseJson);
                }

                if (Commands.START_GAME_PVE.equals(input.getType())) {

                    log.debug(Commands.START_GAME_PVE);
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
                        lastActionTime = System.currentTimeMillis();
                        timer();
                        log.debug(Commands.START_GAME_PVE + " start timer()");
                    }
                    Message response = new Message();
                    response.setType(Commands.START_GAME_PVE_SUCCESS);
                    response.setGame(game);
                    response.setTimeStartTurn(System.currentTimeMillis());
                    String responseJson = gson.toJson(response);
                    out.println(responseJson);
                }

                if (Commands.START_GAME_PVP_ONLINE.equals(input.getType())) {
                    log.debug(Commands.START_GAME_PVP_ONLINE);
                    game = gameService.startGame(player, playerBoard, GameMode.PVP_ONLINE);
                    log.debug("game: {}", game);
                    if (player.equals(game.getCreator())) {
                        gameSession = gameServer.createSession(game);
                        waitingOpponent();
                    } else {
                        gameSession = gameServer.getSession(game.getId());
                    }
                    battleService = gameSession.getBattleService();

                    battleService.setTurnPlayer(game.getTurnPlayer());
                    log.debug("gameSession: {}", gameSession);

                }

                if (Commands.ABORTING.equals(input.getType())) {
                    log.debug(Commands.ABORTING);
                    if (player.equals(game.getCreator())) {
                        game.setWinner(game.getOpponent());
                    } else {
                        game.setWinner(game.getCreator());
                    }
                    game.setEndTime(LocalDateTime.now());
                    game.setGameStatus(GameStatus.ABORTED);
                    battleService.setGameOver(true);
                    turnAI = false;
                    log.debug("game: {}", game);
                    Message response = new Message();
                    response.setType(Commands.ABORTING_SUCCESS);
                    response.setGame(game);
                    broadcastToGamePlayers(gameServer, response);
                    gameServer.removeGameSession(game.getId());
                    cancelTimers();
                }

                if (Commands.ABORT_WAITING.equals(input.getType())) {
                    log.debug(Commands.ABORT_WAITING);
                    gameService.deleteGameFromCreatedGames(game);
                    updateFields();
                }

                if (Commands.SHOOT.equals(input.getType())) {
                    if (gameOver) {
                        continue;
                    }
                    log.debug(Commands.SHOOT);
                    Message response = new Message();
                    int x = input.getX();
                    int y = input.getY();
                    log.debug("player shoot coordinate: {},{}", x, y);
                    battleService = gameSession.getBattleService();
                    Cell resultShoot = battleService.shoot(new Coordinate(x, y));
                    if (resultShoot == null) {
                        continue;
                    }

                    response.setType(Commands.RESULT_SHOOT);
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
                    if (!gameOver) {
                        if (game.getGameMode() != GameMode.PVE) {
                            if (resultShoot == Cell.HIT) {
                                cancelTimer();
                                lastActionTime = System.currentTimeMillis();
                                timer();
                            }
                            if (resultShoot == Cell.MISS) {
                                cancelTimer();
                                ClientHandler nextPlayerHandler = gameServer.getClientHandler(game.getTurnPlayer());
                                if (nextPlayerHandler != null) {
                                    nextPlayerHandler.cancelTimer();
                                    nextPlayerHandler.lastActionTime = System.currentTimeMillis();
                                    nextPlayerHandler.timer();
                                }
                            }
                        } else {
                            cancelTimer();
                            lastActionTime = System.currentTimeMillis();
                            timer();
                        }
                    }
                }

                log.debug("turnAI: {}", turnAI);

                if (turnAI) {
                    if (gameOver) {
                        log.debug("turnAI gameOver");
                        continue;
                    }
                    cancelTimer();
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
                        response.setType(Commands.RESULT_SHOOT);
                        response.setGame(game);
                        response.setResultShoot(resultShoot);
                        response.setTimeStartTurn(System.currentTimeMillis());
                        broadcastToGamePlayers(gameServer, response);

                        if (resultShoot == Cell.MISS) {
                            lastActionTime = System.currentTimeMillis();
                            turnAI = false;
                            timer();
                            continue;
                        }

                        isGameOver();
                    }

                    if (Commands.SWITCH_TURN.equals(input.getType())) {
                        log.debug(Commands.SWITCH_TURN);
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

    private void waitingOpponent() {
        final int[] waitingTime = {0};
        int maxWaitingTime = 60000;

        timeWaiting = scheduler.scheduleAtFixedRate(() -> {
            if (waitingTime[0] >= maxWaitingTime) {
                gameService.deleteGameFromCreatedGames(game);
                Message response = new Message();
                response.setType(Commands.TIMEOUT);
                log.info(Commands.TIMEOUT);
                out.println(gson.toJson(response));
                timeWaiting.cancel(false);
                return;
            }
            if (game.getOpponent() != null) {

                Message response = new Message();
                gameSession.setOpponentHandler(gameServer.getClientHandler(game.getOpponent()));

                cancelTimer();
                ClientHandler opponentHandler = gameServer.getClientHandler(game.getOpponent());
                if (opponentHandler != null) {
                    opponentHandler.cancelTimer();
                }

                response.setType(Commands.START_GAME_PVP_ONLINE_SUCCESS);
                response.setGame(game);
                response.setTimeStartTurn(System.currentTimeMillis());
                if (player.equals(game.getTurnPlayer())) {
                    lastActionTime = System.currentTimeMillis();
                    timer();
                } else {
                    ClientHandler nextPlayerHandler = gameServer.getClientHandler(game.getTurnPlayer());
                    log.debug("nextPlayerHandler: {}", nextPlayerHandler);
                    if (nextPlayerHandler != null) {
                        nextPlayerHandler.lastActionTime = System.currentTimeMillis();
                        nextPlayerHandler.timer();
                    }
                }
                broadcastToGamePlayers(gameServer, response);
                timeWaiting.cancel(false);
                return;
            }
            waitingTime[0] += 1000;

        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private void timer() {
        log.debug("начало метода timer()");
        if (gameOver) return;
        if (game.getGameMode() == GameMode.PVE && game.getTurnPlayer().equals(game.getOpponent())) {
            return;
        }
        if (!player.equals(game.getTurnPlayer())) {
            return;
        }

        timeoutTask = scheduler.schedule(() -> {
            if (gameOver) return;
            if (game.getGameMode() == GameMode.PVE && game.getTurnPlayer().equals(game.getOpponent())) {
                return;
            }
            if (System.currentTimeMillis() - lastActionTime >= TURN_TIME * 1000) {
                log.debug(Commands.TURN_TIMEOUT);
                log.debug("current turnPlayer: {}", game.getTurnPlayer());
                log.debug("battleService: {}", battleService);
                battleService.switchTurnPlayer();
                log.debug("new turnPlayer: {}", game.getTurnPlayer());
                if (game.getGameMode() == GameMode.PVE) {
                    turnAI = true;
                }
                Message response = new Message();
                response.setType(Commands.TURN_TIMEOUT);
                response.setGame(game);
                response.setTimeStartTurn(System.currentTimeMillis());
                cancelTimer();
                log.debug("game.getGameMode() != GameMode.PVE: {}", game.getGameMode() != GameMode.PVE);

                if (game.getGameMode() != GameMode.PVE) {
                    ClientHandler nextPlayerHandler = gameServer.getClientHandler(game.getTurnPlayer());
                    log.debug("nextPlayerHandler: {}", nextPlayerHandler);
                    if (nextPlayerHandler != null) {
                        nextPlayerHandler.cancelTimer();
                        nextPlayerHandler.lastActionTime = System.currentTimeMillis();
                        nextPlayerHandler.timer();
                    }
                }
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

    private void updateFields() {
        playerBoard = null;
        game = null;
        gameSession = null;
        aiService = null;
        turnAI = false;
        gameOver = false;
        battleService = null;
        log.debug("""
                playerBoard: {},
                game: {},
                gameSession: {},
                aiService: {},
                turnAI: {},
                gameOver: {},
                battleService: {}"""
            , playerBoard, game, gameSession, aiService, turnAI, gameOver, battleService);
    }

    private void isGameOver() {
        gameOver = battleService.isGameOver();
        if (gameOver) {
            log.info(Commands.GAME_OVER);
            cancelTimers();
            battleService.endGame();
            turnAI = false;
            log.debug("game: {}", game);
            Message response = new Message();
            response.setType(Commands.GAME_OVER);
            response.setGame(game);
            broadcastToGamePlayers(gameServer, response);
            gameServer.removeGameSession(game.getId());
        }
    }

    private void cancelTimers() {
        cancelTimer();
        ClientHandler nextPlayerHandler = gameServer.getClientHandler(game.getTurnPlayer());
        log.debug("nextPlayerHandler: {}", nextPlayerHandler);
        if (nextPlayerHandler != null) {
            nextPlayerHandler.cancelTimer();
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
