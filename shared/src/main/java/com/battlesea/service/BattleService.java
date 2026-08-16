package com.battlesea.service;

import com.battlesea.enums.Cell;
import com.battlesea.enums.GameStatus;
import com.battlesea.enums.TypeShip;
import com.battlesea.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
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
        if (game == null) {
            throw new IllegalArgumentException("Game must not be null");
        }
        this.game = game;
        turnPlayer = game.getTurnPlayer();
    }

    public Cell shoot(Coordinate coordinate) {
        if (game == null) {
            throw new IllegalStateException("Game is null");
        }
        if (coordinate == null) {
            throw new IllegalArgumentException("Coordinate is null");
        }
        if (!validateCoordinate(coordinate)) {
            throw new IllegalArgumentException("Coordinates are invalid");
        }

        int x = coordinate.x();
        int y = coordinate.y();

        turnPlayer = game.getTurnPlayer();

        if (turnPlayer == null) {
            throw new NullPointerException("Turn player is null");
        }
        if (turnPlayer.equals(game.getCreator())) {
            targetBoard = game.getBoardOpponent();
        } else {
            targetBoard = game.getBoardCreator();
        }

        if (targetBoard == null) {
            throw new IllegalStateException("Target board is null");
        }

        Cell[][] cells = targetBoard.getCells();

        if (cells == null) {
            throw new IllegalStateException("Cells array is null");
        }

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
                boolean isSunk = isSunk(coordinate, cells, targetBoard);
                addHalo(coordinate, isSunk, cells);
                break;
        }

        Cell cellResult = cells[x][y];
        counter++;
        return cellResult;
    }

    private boolean isSunk(Coordinate coordinate, Cell[][] cells, Board targetBoard) {
        if (coordinate == null) {
            throw new IllegalArgumentException("Coordinate is null");
        }
        if (targetBoard == null) {
            throw new IllegalArgumentException("Board is null");
        }
        if (cells == null) {
            throw new IllegalArgumentException("Cells array is null");
        }
        Ship ship = getShip(coordinate);
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

        for (Coordinate coord : coordinates) {
            if (cells[coord.x()][coord.y()] == Cell.SHIP) {
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
        if (targetBoard == null) {
            throw new IllegalArgumentException("Target board is null");
        }
        List<Ship> ships = targetBoard.getShips();
        for (Ship ship : ships) {
            if (!ship.isSunk()) {
                return false;
            }
        }
        return true;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    private Ship getShip(Coordinate coordinate) {
        if (coordinate == null) {
            throw new IllegalArgumentException("Coordinate is null");
        }
        if (targetBoard == null) {
            throw new IllegalArgumentException("Board is null");
        }
        List<Ship> ships = targetBoard.getShips();
        for (Ship ship : ships) {
            if (ship.getCoordinates().contains(coordinate)) {
                return ship;
            }
        }
        return null;
    }

    private boolean validateCoordinate(Coordinate coordinate) {
        if (coordinate == null) {
            return false;
        }
        int x = coordinate.x();
        int y = coordinate.y();
        return x >= 0 && x < SIZE_BOARD && y >= 0 && y < SIZE_BOARD;
    }

    private void addHalo(Coordinate coordinate, boolean isSunk, Cell[][] cells) {
        if (coordinate == null) {
            throw new IllegalArgumentException("Coordinate is null");
        }
        if (cells == null) {
            throw new IllegalArgumentException("Cells array is null");
        }
        Ship ship = getShip(coordinate);
        if (ship == null) {
            throw new IllegalStateException("Ship is null");
        }

        if (isSunk) {
            List<Coordinate> coordinates = ship.getCoordinates();
            for (Coordinate coord : coordinates) {
                addHaloAroundCell(coord, true, cells);
            }
        } else {
            addHaloAroundCell(coordinate, false, cells);
        }
    }

    private void addHaloAroundCell(Coordinate coordinate, boolean isSunk, Cell[][] cells) {
        if (coordinate == null) {
            throw new IllegalArgumentException("Coordinate is null");
        }
        int x = coordinate.x();
        int y = coordinate.y();

        for (int dx = -1; dx <= 1; dx++) {
            if (dx == 0 && !isSunk) {
                continue;
            }
            for (int dy = -1; dy <= 1; dy++) {
                if (dy == 0 && !isSunk) {
                    continue;
                }
                if (validateCoordinate(new Coordinate(x + dx, y + dy)) && cells[x + dx][y + dy] == Cell.EMPTY) {
                    cells[x + dx][y + dy] = Cell.HALO;
                }
            }
        }
    }

    public Game getGame() {
        return game;
    }

    public void endGame() {
        if (game == null) {
            throw new IllegalStateException("Game is null");
        }
        if (game.getTurnPlayer() == null) {
            throw new IllegalStateException("Turn player is null");
        }

        game.setWinner(game.getTurnPlayer());
        game.setGameStatus(GameStatus.ENDED);
        game.setEndTime(LocalDateTime.now());
    }

    public void switchTurnPlayer() {
        if (game == null) {
            throw new IllegalStateException("Game is null");
        }
        if (turnPlayer == null) {
            throw new IllegalStateException("Turn player is null");
        }

        log.debug("=== SWITCH TURN ===");
        log.debug("Current turnPlayer before switch: {}", turnPlayer);
        if (turnPlayer.equals(game.getCreator())) {
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
        if (game == null) {
            throw new IllegalArgumentException("Game is null");
        }
        this.turnPlayer = turnPlayer;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
}
