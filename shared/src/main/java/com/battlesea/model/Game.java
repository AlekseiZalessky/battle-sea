package com.battlesea.model;

import com.battlesea.enums.GameMode;
import com.battlesea.enums.GameStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class Game {
    private final UUID id;
    private final Player player1;
    private Player player2;
    private final LocalDateTime creationTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private GameMode gameMode;
    private GameStatus gameStatus;
    private Board boardPlayer1;
    private Board boardPlayer2;

    public Game(Player player1, Player player2, GameMode gameMode) {
        this.id = UUID.randomUUID();
        this.player1 = player1;
        this.player2 = player2;
        this.creationTime = LocalDateTime.now();
        this.gameMode = gameMode;
    }

    public Game(Player player1, GameMode gameMode) {
        this(player1, null, gameMode);
    }

    public UUID getId() {
        return id;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
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

    public void setPlayer2(Player player2) {
        this.player2 = player2;
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

    @Override
    public String toString() {
        return "Game{" +
            "id=" + id +
            ", player1=" + player1 +
            ", player2=" + player2 +
            ", creationTime=" + creationTime +
            ", startTime=" + startTime +
            ", endTime=" + endTime +
            ", gameMode=" + gameMode +
            ", gameStatus=" + gameStatus +
            '}';
    }
}
