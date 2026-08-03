package com.battlesea.model;

import com.battlesea.enums.GameMode;
import com.battlesea.enums.GameStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class Game {
    private final UUID id;
    private final Player creator;
    private Player opponent;
    private final LocalDateTime creationTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private GameMode gameMode;
    private GameStatus gameStatus;
    private Board boardCreator;
    private Board boardOpponent;
    private Player turnPlayer;
    private Player winner;

    public Game(Player creator, Board boardCreator, Player opponent, GameMode gameMode) {
        this.id = UUID.randomUUID();
        this.creator = creator;
        this.boardCreator = boardCreator;
        this.opponent = opponent;
        this.creationTime = LocalDateTime.now();
        this.gameMode = gameMode;
    }

    public Game(Player creator, Board board, GameMode gameMode) {
        this(creator, board, null, gameMode);
    }

    public UUID getId() {
        return id;
    }

    public Player getCreator() {
        return creator;
    }

    public Player getOpponent() {
        return opponent;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public Board getBoardCreator() {
        return boardCreator;
    }

    public void setBoardCreator(Board boardCreator) {
        this.boardCreator = boardCreator;
    }

    public Board getBoardOpponent() {
        return boardOpponent;
    }

    public void setBoardOpponent(Board boardOpponent) {
        this.boardOpponent = boardOpponent;
    }

    public Player getTurnPlayer() {
        return turnPlayer;
    }

    public void setTurnPlayer(Player turnPlayer) {
        this.turnPlayer = turnPlayer;
    }

    public Player getWinner() {
        return winner;
    }

    public void setWinner(Player winner) {
        this.winner = winner;
    }

    @Override
    public String toString() {
        return "Game{" +
            "id=" + id +
            ", creator=" + creator +
            ", opponent=" + opponent +
            ", creationTime=" + creationTime +
            ", startTime=" + startTime +
            ", endTime=" + endTime +
            ", gameMode=" + gameMode +
            ", gameStatus=" + gameStatus +
            ", boardCreator=" + boardCreator +
            ", boardOpponent=" + boardOpponent +
            ", turnPlayer=" + turnPlayer +
            ", winner=" + winner +
            '}';
    }
}
