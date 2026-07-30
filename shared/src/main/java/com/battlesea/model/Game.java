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
}
