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

public class Client {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final Gson gson;
    private Board boardPlayer1;
    private Board boardPlayer2;
    private Game game;
    private boolean resultShoot;
    private boolean gameOver;
    private Player currnetPlayer;

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
                    message.setUsername("qwerty");

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
                    System.out.println(message);
                    if ("PVE_SUCCESS".equals(message.getType())) {
                        boardPlayer1 = message.getBoardPlayer1();
                        continue;
                    }

                    if("START_GAME_PVE_SUCCESS".equals(message.getType())) {
                        game = message.getGame();
                        boardPlayer2 = game.getBoardPlayer2();
                        continue;
                    }

                    if("RESULT_SHOOT".equals(message.getType())) {
                        Cell result = message.getResultShoot();
                        if(result == null){
                            continue;
                        }
                        resultShoot = result != null;
                        game = message.getGame();

                        Cell[][] cell = game.getBoardPlayer2().getCells();
                        boardPlayer2.setCells(cell);
                        cell[message.getX()][message.getY()] = result;
                    }

                    if("GAME_OVER".equals(message.getType())) {
                        System.out.println("GAME_OVER");
                        gameOver = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public Board getBoardPlayer1() {
        return boardPlayer1;
    }

    public Board getBoardPlayer2() {
        return boardPlayer2;
    }

    public boolean isResultShoot() {
        return resultShoot;
    }

    public boolean isGameOver() {
        return gameOver;
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
