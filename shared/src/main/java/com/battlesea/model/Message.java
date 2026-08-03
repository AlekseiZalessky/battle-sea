package com.battlesea.model;

import com.battlesea.enums.Cell;

public class Message {
    private String type;
    private String username;
    private int x;
    private int y;
    private Board boardPlayer1;
    private Board boardPlayer2;
    private Game game;
    private Cell resultShoot;
    private Player turnPlayer;
    private Player creator;
    private Player opponent;


    public Message() {

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Board getBoardPlayer1() {
        return boardPlayer1;
    }

    public void setBoardPlayer1(Board boardPlayer1) {
        this.boardPlayer1 = boardPlayer1;
    }

    public Board getBoardPlayer2() {
        return boardPlayer2;
    }

    public void setBoardPlayer2(Board boardPlayer2) {
        this.boardPlayer2 = boardPlayer2;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Cell getResultShoot() {
        return resultShoot;
    }

    public void setResultShoot(Cell resultShoot) {
        this.resultShoot = resultShoot;
    }

    public Player getTurnPlayer() {
        return turnPlayer;
    }

    public void setTurnPlayer(Player turnPlayer) {
        this.turnPlayer = turnPlayer;
    }

    public Player getCreator() {
        return creator;
    }

    public void setCreator(Player creator) {
        this.creator = creator;
    }

    public Player getOpponent() {
        return opponent;
    }

    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }

    @Override
    public String toString() {
        return "Message{" +
            "type='" + type + '\'' +
            ", username='" + username + '\'' +
            ", x=" + x +
            ", y=" + y +
            ", boardPlayer1=" + boardPlayer1 +
            ", boardPlayer2=" + boardPlayer2 +
            ", game=" + game +
            ", resultShoot=" + resultShoot +
            ", turnPlayer=" + turnPlayer +
            ", creator=" + creator +
            ", opponent=" + opponent +
            '}';
    }
}
