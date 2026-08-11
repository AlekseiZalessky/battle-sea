package com.battlesea.service;

import com.battlesea.enums.Cell;
import com.battlesea.enums.TypeShip;
import com.battlesea.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BattleService {
    private final Game game;
    private final int SIZE_BOARD = Board.SIZE;
    private Board targetBoard;
    private boolean gameOver;
    private Player turnPlayer;
    private static final Logger log = LoggerFactory.getLogger(BattleService.class);
    private int counter;

    public BattleService(Game game) {
        this.game = game;
        turnPlayer = game.getTurnPlayer();
    }

    public Cell shoot(Coordinate coordinate) {
        int x = coordinate.x();
        int y = coordinate.y();
        if (!validateCoordinate(x, y)) {
            throw new IllegalArgumentException("Coordinates are invalid");
        }

        turnPlayer = game.getTurnPlayer();

        if (turnPlayer == null) {
            throw new NullPointerException("Turn player is null");
        }
        if (turnPlayer.equals(game.getCreator())) {
            targetBoard = game.getBoardOpponent();
        } else {
            targetBoard = game.getBoardCreator();
        }

        Cell[][] cells = targetBoard.getCells();

        switch (cells[x][y]) {
            case HALO:
            case HIT:
            case MISS:
                log.debug(cells[x][y].toString());
                return null;
            case EMPTY:
                cells[x][y] = Cell.MISS;
                switchTurnPlayer();
                break;
            case SHIP:
                cells[x][y] = Cell.HIT;
                boolean isSunk = isSunk(x, y, cells, targetBoard);
                addHalo(x, y, isSunk, cells);
                break;
        }

        Cell cellResult = cells[x][y];
        counter++;
        return cellResult;
    }

    private boolean isSunk(int x, int y, Cell[][] cells, Board targetBoard) {
        Ship ship = getShip(x, y);
        if (ship == null) {
            return false;
        }
        if (ship.getType() == TypeShip.OneDeckShip) {
            ship.setSunk(true);
            gameOver = isGameOver(targetBoard);
            return true;
        }
        boolean isSunk = true;

        List<Coordinate> coordinates = ship.getCoordinates();

        for (Coordinate coordinate : coordinates) {
            int coorX = coordinate.x();
            int coorY = coordinate.y();
            if (cells[coorX][coorY] == Cell.SHIP) {
                isSunk = false;
                break;
            }
        }
        if (isSunk) {
            ship.setSunk(true);
            gameOver = isGameOver(targetBoard);
        }
        return isSunk;
    }

    private boolean isGameOver(Board targetBoard) {
        List<Ship> ships = targetBoard.getShips();
        boolean gameOver = true;
        for (Ship ship : ships) {
            if (!ship.isSunk()) {
                gameOver = false;
                break;
            }
        }
        return gameOver;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    private Ship getShip(int x, int y) {
        List<Ship> ships = targetBoard.getShips();
        for (Ship ship : ships) {
            List<Coordinate> coordinates = ship.getCoordinates();

            for (Coordinate coordinate : coordinates) {
                if (coordinate.x() == x && coordinate.y() == y) {
                    return ship;
                }
            }
        }
        return null;
    }

    private boolean validateCoordinate(int x, int y) {
        return x >= 0 && x < SIZE_BOARD && y >= 0 && y < SIZE_BOARD;
    }

    private void addHalo(int x, int y, boolean isSunk, Cell[][] cells) {
        Ship ship = getShip(x, y);
        if (ship == null) {
            return;
        }
        if (isSunk) {
            List<Coordinate> coordinates = ship.getCoordinates();
            for (Coordinate coordinate : coordinates) {
                int coorX = coordinate.x();
                int coorY = coordinate.y();

                addHaloAroundCell(coorX, coorY, true, cells);
            }
        } else {
            addHaloAroundCell(x, y, false, cells);
        }
    }

    private void addHaloAroundCell(int x, int y, boolean isSunk, Cell[][] cells) {
        for (int dx = -1; dx <= 1; dx++) {
            if (dx == 0 && !isSunk) {
                continue;
            }
            for (int dy = -1; dy <= 1; dy++) {
                if (dy == 0 && !isSunk) {
                    continue;
                }
                if (validateCoordinate(x + dx, y + dy) && cells[x + dx][y + dy] == Cell.EMPTY) {
                    cells[x + dx][y + dy] = Cell.HALO;
                }
            }
        }
    }

    public Game getGame() {
        return game;
    }

    public void winner(){
        if(game.getTurnPlayer().equals(game.getCreator())){
            game.setWinner(game.getCreator());
        } else {
            game.setWinner(game.getOpponent());
        }
    }

    public void switchTurnPlayer() {
        log.debug("=== SWITCH TURN ===");
        log.debug("Current turnPlayer before switch: {}", turnPlayer);
        if(turnPlayer.equals(game.getCreator())){
            turnPlayer = game.getOpponent();
            targetBoard = game.getBoardCreator();
            log.debug("Switched: CREATOR -> OPPONENT");
        } else {
            turnPlayer = game.getCreator();
            targetBoard = game.getBoardOpponent();
            log.debug("Switched: OPPONENT -> CREATOR");
        }
        game.setTurnPlayer(turnPlayer);
        log.debug("New turnPlayer: {}", turnPlayer);
        log.debug("=== SWITCH TURN END ===");
        System.out.println();
    }

    public int getCounter() {
        return counter;
    }

    public void setTurnPlayer(Player turnPlayer) {
        this.turnPlayer = turnPlayer;
    }
}
