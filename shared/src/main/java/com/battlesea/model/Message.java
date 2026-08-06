package com.battlesea.model;

import com.battlesea.enums.Cell;

public class Message {
    private String type;
    private String username;
    private int x;
    private int y;
    private Board boardCreator;
    private Board boardOpponent;
    private Game game;
    private Cell resultShoot;
    private Player currentPlayer;
    private Player creator;
    private Player opponent;
    private long timeStartTurn;


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

    public Board getBoardCreator() {
        return boardCreator;
    }

    public void setBoardCreator(Board boardCreator) {
        this.boardCreator = boardCreator;
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

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
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

    public long getTimeStartTurn() {
        return timeStartTurn;
    }

    public void setTimeStartTurn(long timeStartTurn) {
        this.timeStartTurn = timeStartTurn;
    }

    @Override
    public String toString() {
        return "Message{" +
            "type='" + type + '\'' +
            ", username='" + username + '\'' +
            ", x=" + x +
            ", y=" + y +
            ", boardPlayer1=" + boardCreator +
            ", boardPlayer2=" + boardOpponent +
            ", game=" + game +
            ", resultShoot=" + resultShoot +
            ", turnPlayer=" + currentPlayer +
            ", creator=" + creator +
            ", opponent=" + opponent +
            '}';
    }
}
