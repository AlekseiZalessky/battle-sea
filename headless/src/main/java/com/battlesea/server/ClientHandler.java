package com.battlesea.server;

import com.battlesea.constants.Commands;
import com.battlesea.enums.Cell;
import com.battlesea.enums.GameMode;
import com.battlesea.enums.GameStatus;
import com.battlesea.enums.TypeShip;
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
import java.util.List;
import java.util.concurrent.*;

public class ClientHandler {
    private final Socket socket;
    private final GameServer gameServer;
    private final BufferedReader in;
    private final PrintWriter out;
    private final Gson gson;
    private Board playerBoard;
    private Board opponentBoard;
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
    private ShipPlacementService service;
    private List<Coordinate> coorForMoveAI;
    private AIState aiState;
    private BattleState battleState;


    public ClientHandler(Socket socket, GameServer gameServer) throws Exception {
        this.socket = socket;
        this.gameServer = gameServer;
        this.gameService = new GameService();
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.gson = initGson();

        log.debug("Player connected to server");
        String json;

        try {
            authentication();

            while (true) {
                json = in.readLine();
                if (json == null) {
                    break;
                }

                Message input = gson.fromJson(json, Message.class);

                if (Commands.UPDATE_FIELDS.equals(input.getType())) {
                    log.debug(Commands.UPDATE_FIELDS);
                    updateFields();
                }

                if (Commands.EXIT.equals(input.getType())) {
                    log.debug(Commands.EXIT);
                    gameServer.removePlayer(player);
                    break;
                }

                if (Commands.AUTO_PLACE.equals(input.getType())) {
                    log.debug(Commands.AUTO_PLACE);
                    autoPlace();
                }

                if (Commands.PLACE_SHIP.equals(input.getType())) {
                    log.debug(Commands.PLACE_SHIP);
                    placeShip(input);
                }

                if (Commands.CHANGE_ORIENTATION.equals(input.getType())) {
                    log.debug(Commands.CHANGE_ORIENTATION);
                    changeOrientation(input);
                }

                if (Commands.START_GAME_PVE.equals(input.getType())) {
                    log.debug(Commands.START_GAME_PVE);
                    startGamePVE();
                }

                if (Commands.START_GAME_PVP_ONLINE.equals(input.getType())) {
                    log.debug(Commands.START_GAME_PVP_ONLINE);
                    startGamePVPOnline();
                }

                if (Commands.ABORTING.equals(input.getType())) {
                    log.debug(Commands.ABORTING);
                    aborting();
                }

                if (Commands.ABORT_WAITING.equals(input.getType())) {
                    log.debug(Commands.ABORT_WAITING);
                    gameService.deleteGameFromCreatedGames(game);
                    updateFields();
                }

                if (Commands.SHOOT.equals(input.getType())) {
                    shoot(input);
                }

                if (turnAI && !gameOver) {
                    cancelTimer();
                    if (battleState.isFirstShoot()) {
//                        Thread.sleep(3000);
                    }

                    while (turnAI) {
                       moveAI();
                    }
                }

                if (Commands.SWITCH_TURN.equals(input.getType())) {
                    log.debug(Commands.SWITCH_TURN);
                    if (game.getGameMode() == GameMode.PVE && game.getTurnPlayer().equals(game.getOpponent())) {
                        turnAI = true;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error in ClientHandler: {}", e.getMessage(), e);
        } finally {
            disconnect();
        }
    }

    private void shoot(Message input) {
        if (gameOver) {
            return;
        }
        log.debug(Commands.SHOOT);
        Message response = new Message();
        Coordinate target = input.getCoordinate();
        battleService = gameSession.getBattleService();
        Cell resultShoot = battleService.shoot(game, target, opponentBoard, battleState);
        if (resultShoot == null) {
            return;
        }

        response.setType(Commands.RESULT_SHOOT);
        response.setGame(game);
        response.setCoordinate(target);
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

    private void aborting() {
        if (player.equals(game.getCreator())) {
            game.setWinner(game.getOpponent());
        } else {
            game.setWinner(game.getCreator());
        }
        game.setEndTime(LocalDateTime.now());
        game.setGameStatus(GameStatus.ABORTED);
        battleState.setGameOver(true);
        turnAI = false;
        Message response = new Message();
        response.setType(Commands.ABORTING_SUCCESS);
        response.setGame(game);
        broadcastToGamePlayers(gameServer, response);
        gameServer.removeGameSession(game.getId());
        cancelTimers();
    }

    private void startGamePVPOnline() {
        game = gameService.startGame(player, playerBoard, GameMode.PVP_ONLINE);
        if (player.equals(game.getCreator())) {
            gameSession = gameServer.createSession(game, this);
            waitingOpponent();
        } else {
            gameSession = gameServer.getSession(game.getId());
        }
        battleService = gameSession.getBattleService();
        battleState = new BattleState();
//        battleState.setTurnPlayer(game.getTurnPlayer());
    }

    private void startGamePVE() {
        game = gameService.startGame(player, playerBoard, GameMode.PVE);
        gameSession = gameServer.createSession(game, this);
        battleService = gameSession.getBattleService();
        battleState = new BattleState();
        if (game.getTurnPlayer().equals(game.getOpponent())) {
            turnAI = true;
        } else {
            lastActionTime = System.currentTimeMillis();
            timer();
        }
        if (player.equals(game.getCreator())) {
            opponentBoard = game.getBoardOpponent();
        } else {
            opponentBoard = game.getBoardOpponent();
        }
        Message response = new Message();
        response.setType(Commands.START_GAME_PVE_SUCCESS);
        response.setGame(game);
        response.setTimeStartTurn(System.currentTimeMillis());
        String responseJson = gson.toJson(response);
        out.println(responseJson);
    }

    private void changeOrientation(Message input) {
        Coordinate coordinate = input.getCoordinate();
        boolean result = service.changeOrientationShip(playerBoard, coordinate);
        Message response = new Message();
        if (result) {
            response.setType(Commands.CHANGE_ORIENTATION_SUCCESS);
        } else {
            response.setType(Commands.CHANGE_ORIENTATION_FAIL);
        }
        response.setBoardCreator(playerBoard);
        sendMessage(response);
    }

    private void placeShip(Message input) {
        if (playerBoard == null) {
            playerBoard = new Board();
            playerBoard.setPlayer(player);
        }
        if (service == null) {
            service = new ShipPlacementService();
        }
        Coordinate coordinate = input.getCoordinate();
        Coordinate oldCoordinate = input.getOldCoordinate();
        boolean horizontalShip = input.isHorizontalShip();
        TypeShip typeShip = input.getTypeShip();
        Message response = new Message();
        boolean result;
        if (oldCoordinate == null) {
            result = service.placeShipManually(playerBoard, coordinate, horizontalShip, typeShip);
        } else {
            result = service.relocateShip(playerBoard, coordinate, oldCoordinate);
        }
        if (result) {
            response.setType(Commands.PLACE_SHIP_SUCCESS);
            if (playerBoard.getShips().size() == 10) {
                response.setAllShipPlaced(true);
                service.clearHalo(playerBoard.getCells());
            }
        } else {
            response.setType(Commands.PLACE_SHIP_FAIL);
        }
        response.setBoardCreator(playerBoard);
        sendMessage(response);
    }

    private void autoPlace() {
        service = new ShipPlacementService();
        if (playerBoard == null) {
            playerBoard = new Board();
        } else {
            playerBoard.init();
            playerBoard.getShips().clear();
        }

        playerBoard = service.generateRandomShips(player, playerBoard);

        Message response = new Message();
        response.setType(Commands.AUTO_PLACE_SUCCESS);
        response.setBoardCreator(playerBoard);
        sendMessage(response);
    }

    private void authentication() throws IOException {
        String json;
        while (true) {
            json = in.readLine();
            if (json == null) {
                break;
            }

            Message input = gson.fromJson(json, Message.class);
            log.debug("input: {}", input);
            if (Commands.AUTH.equals(input.getType())) {
                player = new Player();
                String username = player.getName();
                gameServer.registerPlayer(player, this);
                log.debug("Player authorized: {}", username);
                Message response = new Message();
                response.setType(Commands.AUTH_SUCCESS);
                response.setCurrentPlayer(player);
                sendMessage(response);
                break;
            }
        }
    }

    private static Gson initGson() {
        return new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class,
                (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                    src == null ? null : new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class,
                (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                    LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .create();
    }

    private void moveAI() {
        aiService = gameSession.getAiService();

        if (coorForMoveAI == null) {
            coorForMoveAI = aiService.initListCoorForMoveAI(game.getBoardOpponent().getCells());
        }
        if (aiState == null) {
            aiState = new AIState();
        }
        Coordinate coordinate = aiService.chooseCoordinate(playerBoard, coorForMoveAI, aiState);
        Cell resultShoot = battleService.shoot(game, coordinate, playerBoard, battleState);

        if (resultShoot == Cell.HIT && !aiService.isSunk(coordinate, playerBoard, aiState)) {
            if (aiState.isHasHit()) {
                aiService.setOrientation(coordinate, playerBoard, aiState);
            }

            if (!aiState.isHasHit()) {
                aiState.setCoordinateHit(coordinate);
            }
            aiState.setHasHit(true);
        }
        aiService.removeCoordinate(coordinate, coorForMoveAI);
        aiService.updateFreeCoordinates(coorForMoveAI, playerBoard.getCells());

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
        }

        isGameOver();
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

                if (player.equals(game.getCreator())) {
                    opponentBoard = game.getBoardOpponent();
                    opponentHandler.setOpponentBoard(game.getBoardCreator());
                } else {
                    opponentBoard = game.getBoardCreator();
                    opponentHandler.setOpponentBoard(game.getBoardOpponent());
                }

                response.setType(Commands.START_GAME_PVP_ONLINE_SUCCESS);
                response.setGame(game);
                response.setTimeStartTurn(System.currentTimeMillis());
                if (player.equals(game.getTurnPlayer())) {
                    lastActionTime = System.currentTimeMillis();
                    timer();
                } else {
                    ClientHandler nextPlayerHandler = gameServer.getClientHandler(game.getTurnPlayer());
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
                battleService.switchTurnPlayer(game);
                if (game.getGameMode() == GameMode.PVE) {
                    turnAI = true;
                }
                Message response = new Message();
                response.setType(Commands.TURN_TIMEOUT);
                response.setGame(game);
                response.setTimeStartTurn(System.currentTimeMillis());
                cancelTimer();

                if (game.getGameMode() != GameMode.PVE) {
                    ClientHandler nextPlayerHandler = gameServer.getClientHandler(game.getTurnPlayer());
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
        aiState = null;
        turnAI = false;
        gameOver = false;
        battleService = null;
        battleState = null;
        service = null;
        coorForMoveAI = null;
    }

    private void isGameOver() {
        gameOver = battleState.isGameOver();
        if (gameOver) {
            log.info(Commands.GAME_OVER);
            cancelTimers();
            battleService.endGame(game);
            turnAI = false;
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
        if (nextPlayerHandler != null) {
            nextPlayerHandler.cancelTimer();
        }
    }

    public void setOpponentBoard(Board opponentBoard) {
        this.opponentBoard = opponentBoard;
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

    public void disconnect() {

        if (in != null) {
            try {
                in.close();
                log.debug("BufferedReader закрыт для игрока {}", player.getName());
            } catch (Exception e) {
                log.error("Не удалось закрыть BufferedReader для игрока {}", player.getName(), e);
            }
        }

        if (out != null) {
            try {
                out.close();
                log.debug("PrintWriter закрыт для игрока {}", player.getName());
            } catch (Exception e) {
                log.error("Не удалось закрыть PrintWriter для игрока {}", player.getName(), e);
            }
        }

        if (socket != null) {
            try {
                socket.close();
                log.debug("Socket  закрыт для игрока {}", player.getName());
            } catch (Exception e) {
                log.error("Не удалось закрыть Socket для игрока {}", player.getName(), e);
            }
        }
    }
}
