package com.battlesea.model;

public class BattleState {
    private boolean firstShoot;
    private boolean gameOver;

    public BattleState() {
        this.firstShoot = false;
        this.gameOver = false;
    }

    public boolean isFirstShoot() {
        return firstShoot;
    }

    public void setFirstShoot(boolean firstShoot) {
        this.firstShoot = firstShoot;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
}
