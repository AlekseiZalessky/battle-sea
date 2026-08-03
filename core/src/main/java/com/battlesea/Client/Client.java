package com.battlesea.Client;

import com.battlesea.enums.Cell;
import com.battlesea.model.Board;
import com.battlesea.model.Game;
import com.battlesea.model.Message;
import com.battlesea.model.Player;
import com.google.gson.*;

import java.io.*;
import java.net.Socket;
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
    private boolean resultShoot;
    private boolean gameOver;
    private Player currnetPlayer;
    private Player creator;
    private Player opponent;
    private boolean isStartingGame;
    private boolean timeOut;

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
                    message.setType("AUTH");
                    Random rand = new Random();
                    message.setUsername("player" + rand.nextInt(10000));

                    out.println(gson.toJson(message));

                    String json = in.readLine();
                    if (json == null) {
                        break;
                    }

                    message = gson.fromJson(json, Message.class);

                    if ("AUTH_SUCCESS".equals(message.getType())) {
                        currnetPlayer = message.getTurnPlayer();
                        System.out.println("AUTH_SUCCESS");
                        break;
                    }
                }

                while(true){
                    String json = in.readLine();
                    if (json == null) {
                        break;
                    }
                    Message message = gson.fromJson(json, Message.class);
                    if ("PVE_SUCCESS".equals(message.getType())) {
                        currnetPlayer = message.getCreator();
                        boardCreator = message.getBoardPlayer1();
                        opponent = message.getOpponent();
                        continue;
                    }

                    if("START_GAME_PVE_SUCCESS".equals(message.getType())) {
                        System.out.println("START_GAME_PVE_SUCCESS");
                        System.out.println("message: " + message);
                        game = message.getGame();
                        update(game);
                        continue;
                    }

                    if("START_GAME_PVP_ONLINE_SUCCESS".equals(message.getType())) {
                        System.out.println("START_GAME_PVP_ONLINE_SUCCESS");
                        game = message.getGame();
                        System.out.println("poluchili igru: " + game);
                        update(game);
                        isStartingGame = true;
                        continue;
                    }

                    if("RESULT_SHOOT".equals(message.getType())) {
                        Cell result = message.getResultShoot();
                        if(result == null){
                            continue;
                        }
                        resultShoot = result != null;
                        game = message.getGame();
                        update(game);
                        continue;
                    }

                    if("GAME_OVER".equals(message.getType())) {
                        Thread.sleep(2000);
                        System.out.println("GAME_OVER");
                        game = message.getGame();
                        update(game);
                        System.out.println(game);
                        gameOver = true;
                    }

                    if("TIMEOUT".equals(message.getType())) {
                        timeOut = true;
                        System.out.println("TIMEOUT");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void update(Game game) {
        creator = game.getCreator();
        opponent = game.getOpponent();
        boardCreator = game.getBoardCreator();
        boardOpponent = game.getBoardOpponent();

    }

    public Board getBoardCreator() {
        return boardCreator;
    }

    public Board getBoardOpponent() {
        return boardOpponent;
    }

    public boolean isResultShoot() {
        return resultShoot;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isStartingGame() {
        return isStartingGame;
    }

    public Player getCreatorPlayer(){
        return creator;
    }

    public Player getOpponentPlayer(){
        return opponent;
    }

    public Player getCurrentPlayer(){
        return currnetPlayer;
    }

    public boolean isTimeOut() {
        return timeOut;
    }

    public Player winner(){
        return game.getWinner();
    }

    public Game getGame() {
        return game;
    }

//    public void sendAutoPlaceRequest() {
//        Message message = new Message();
//        message.setType("PVE_AUTO");
//        out.println(gson.toJson(message));
//    }

//    public void sendStartGame() {
//        Message message = new Message();
//        message.setType("START_GAME_PVE");
//        out.println(gson.toJson(message));
//    }

    public void sendMessage(String type){
        Message message = new Message();
        message.setType(type);
        out.println(gson.toJson(message));
    }

    public void sendAttack(int x, int y){
        Message message = new Message();
        message.setType("SHOOT");
        message.setX(x);
        message.setY(y);
        message.setTurnPlayer(currnetPlayer);
        out.println(gson.toJson(message));
    }
}
