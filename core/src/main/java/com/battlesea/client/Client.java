package com.battlesea.client;

import com.battlesea.constants.Commands;
import com.battlesea.enums.Cell;
import com.battlesea.enums.GameMode;
import com.battlesea.enums.TypeShip;
import com.battlesea.model.*;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class Client {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final Gson gson;
    private Board boardCreator;
    private Board boardOpponent;
    private Game game;
    private boolean gameOver;
    private Player currnetPlayer;
    private Player creator;
//    private Player opponent;
    private boolean isStartingGame;
    private boolean timeOut;
    private static final Logger log = LoggerFactory.getLogger(Client.class);
    private final int TURN_TIME = (Game.TURN_TIME + 1) * 1000;
    private long timeStartTimer;
    private boolean allShipPlaced;
    private boolean placeShipSuccess;
    private boolean changeOrientationSuccess;

    public void updateOnStartGame(){
        boardCreator = null;
        boardOpponent = null;
        game = null;
        gameOver = false;
        creator = null;
//        opponent = null;
        isStartingGame = false;
        timeOut = false;
        allShipPlaced = false;
        placeShipSuccess = false;
        changeOrientationSuccess = false;
    }

    public Client(String host, int port) throws Exception {
        this.socket = new Socket(host, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class,
                (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                    src == null ? null : new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class,
                (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                    LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .create();

        new Thread(() -> {
            try {
                while (true) {
                    Message message = new Message();
                    message.setType(Commands.AUTH);
                    Random rand = new Random();
                    message.setUsername("player" + rand.nextInt(10000));

                    out.println(gson.toJson(message));

                    String json = in.readLine();
                    if (json == null) {
                        break;
                    }

                    message = gson.fromJson(json, Message.class);

                    if (Commands.AUTH_SUCCESS.equals(message.getType())) {
                        currnetPlayer = message.getCurrentPlayer();
                        log.info(Commands.AUTH_SUCCESS);
                        log.info("Current Player : {}", currnetPlayer.getName());
                        break;
                    }
                }

                while (true) {
                    String json = in.readLine();
                    log.debug("Получено сообщение: {}", json);
                    if (json == null) {
                        continue;
                    }
                    Message message = gson.fromJson(json, Message.class);
                    if (Commands.AUTO_PLACE_SUCCESS.equals(message.getType())) {
                        log.info(Commands.AUTO_PLACE_SUCCESS);
                        boardCreator = message.getBoardCreator();
                        allShipPlaced = true;
                        continue;
                    }

                    if (Commands.PLACE_SHIP_SUCCESS.equals(message.getType())) {
                        log.info(Commands.PLACE_SHIP_SUCCESS);
                        placeShipSuccess = true;
                        boardCreator = message.getBoardCreator();
                        System.out.println(boardCreator);
                        allShipPlaced = message.isAllShipPlaced();
                        continue;
                    }

                    if (Commands.PLACE_SHIP_FAIL.equals(message.getType())) {
                        log.info(Commands.PLACE_SHIP_FAIL);
                        placeShipSuccess = false;
                        boardCreator = message.getBoardCreator();
                        continue;
                    }

                    if (Commands.CHANGE_ORIENTATION_SUCCESS.equals(message.getType())) {
                        log.info(Commands.CHANGE_ORIENTATION_SUCCESS);
                        boardCreator = message.getBoardCreator();
                        changeOrientationSuccess =  true;
                        continue;
                    }

                    if (Commands.CHANGE_ORIENTATION_FAIL.equals(message.getType())) {
                        log.info(Commands.CHANGE_ORIENTATION_FAIL);
                        boardCreator = message.getBoardCreator();
                        changeOrientationSuccess = false;
                        continue;
                    }

                    if (Commands.START_GAME_PVE_SUCCESS.equals(message.getType())) {
                        timeStartTimer = message.getTimeStartTurn();
                        log.info(Commands.START_GAME_PVE_SUCCESS);
                        isStartingGame = true;
                        game = message.getGame();
                        log.debug("game: {}", game);
                        update();
                        continue;
                    }

                    if (Commands.START_GAME_PVP_ONLINE_SUCCESS.equals(message.getType())) {
                        log.info(Commands.START_GAME_PVP_ONLINE_SUCCESS);
                        timeStartTimer = message.getTimeStartTurn();
                        game = message.getGame();
                        update();
                        isStartingGame = true;
                        continue;
                    }

                    if (Commands.RESULT_SHOOT.equals(message.getType())) {
                        if(game.getGameMode() == GameMode.PVE && game.getTurnPlayer().equals(game.getOpponent())){
                            Thread.sleep(500);
                        }
                        timeStartTimer = message.getTimeStartTurn();
                        Cell result = message.getResultShoot();
                        if (result == null) {
                            continue;
                        }
//                        playShootSound();
                        game = message.getGame();
                        update();
                        continue;
                    }

                    if (Commands.GAME_OVER.equals(message.getType())) {
                        Thread.sleep(2000);
                        log.info(Commands.GAME_OVER);
                        game = message.getGame();
                        update();
                        log.debug("game: {}", game);
                        gameOver = true;
                    }

                    if (Commands.ABORTING_SUCCESS.equals(message.getType())) {
                        Thread.sleep(2000);
                        log.info(Commands.ABORTING_SUCCESS);
                        game = message.getGame();
                        update();
                        log.debug("game: {}", game);
                        log.debug("game.getWinner(): {}", game.getWinner());
                        gameOver = true;
                    }

                    if (Commands.TIMEOUT.equals(message.getType())) {
                        timeOut = true;
                        log.info(Commands.TIMEOUT);
                    }

                    if (Commands.TURN_TIMEOUT.equals(message.getType())) {
                        log.debug(Commands.TURN_TIMEOUT);
                        game = message.getGame();
                        timeStartTimer = message.getTimeStartTurn();
                        if (game.getTurnPlayer().equals(game.getOpponent())) {
                            sendMessage(Commands.SWITCH_TURN);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error in Client: {}", e.getMessage(), e);
            }
        }).start();
    }

    private void update() {
        creator = game.getCreator();
//        opponent = game.getOpponent();
        boardCreator = game.getBoardCreator();
        boardOpponent = game.getBoardOpponent();
    }

    public Board getBoardCreator() {
        return boardCreator;
    }

    public Board getBoardOpponent() {
        return boardOpponent;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public boolean isStartingGame() {
        return isStartingGame;
    }

    public Player getCreatorPlayer() {
        return creator;
    }

//    public Player getOpponentPlayer() {
//        return opponent;
//    }

    public Player getCurrentPlayer() {
        return currnetPlayer;
    }

    public boolean isTimeOut() {
        return timeOut;
    }

    public Player winner() {
        return game.getWinner();
    }

//    public Game getGame() {
//        return game;
//    }

    public long getTurnTime() {
        return TURN_TIME + (timeStartTimer - System.currentTimeMillis());
    }

    public void sendMessage(String type) {
        log.debug("sendMessage: {}", type);
        Message message = new Message();
        message.setType(type);
        out.println(gson.toJson(message));
    }

    public void sendAttack(Coordinate coordinate) {
        Message message = new Message();
        message.setType(Commands.SHOOT);
        message.setCoordinate(coordinate);
        message.setCurrentPlayer(currnetPlayer);
        out.println(gson.toJson(message));
    }

    public void sendPlaceShip(Coordinate coordinate, Coordinate oldCoordinate, TypeShip typeShip, boolean horizontalShip) {
        Message message = new Message();
        message.setType(Commands.PLACE_SHIP);
        message.setBoardCreator(boardCreator);
        message.setCoordinate(coordinate);
        message.setOldCoordinate(oldCoordinate);
        message.setHorizontalShip(horizontalShip);
        message.setTypeShip(typeShip);
        out.println(gson.toJson(message));
        log.debug("send message: {}", message);
    }

    public void sendChangeOrientation(Coordinate coordinate) {
        Message message = new Message();
        message.setType(Commands.CHANGE_ORIENTATION);
        message.setCoordinate(coordinate);
        out.println(gson.toJson(message));
    }

    public Player getTurnPlayer() {
        return game.getTurnPlayer();
    }

    public boolean isPlaceShipSuccess() {
        return placeShipSuccess;
    }

    public boolean isChangeOrientationSuccess() {
        return changeOrientationSuccess;
    }

    public boolean isAllShipPlaced() {
        return allShipPlaced;
    }

    public void setPlaceShipSuccess(boolean placeShipSuccess) {
        this.placeShipSuccess = placeShipSuccess;
    }

    public void playShootSound() {
        try {
            log.debug("playShootSound ");
            URL soundUrl = getClass().getResource("/shoot.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundUrl);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            log.error("Error in Client.playShootSound(): {}", e.getMessage(), e);
        }
    }
}
