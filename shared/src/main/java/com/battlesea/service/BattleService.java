package com.battlesea.service;

import com.battlesea.enums.Cell;
import com.battlesea.enums.TypeShip;
import com.battlesea.model.Board;
import com.battlesea.model.Game;
import com.battlesea.model.Player;
import com.battlesea.model.Ship;

import java.util.List;

public class BattleService {
    private Game game;
    private final int SIZE_BOARD = Board.SIZE;
    private Board boardPlayer1;
    private Board boardPlayer2;
    private Board targetBoard;
    private boolean gameOver;

    public void startGame(Game game) {
        if (game == null) {
            throw new NullPointerException("Game is null");
        }
        this.game = game;
        boardPlayer1 = game.getBoardPlayer1();
        boardPlayer2 = game.getBoardPlayer2();
    }

    public Cell shoot(Game game, int x, int y) {
        this.game = game;
        if (!validateCoordinate(x, y)) {
            throw new IllegalArgumentException("Coordinates are invalid");
        }
        Player turnPlayer = game.getTurnPlayer();
        if (turnPlayer == null) {
            throw new NullPointerException("Turn player is null");
        }
        if (turnPlayer == game.getPlayer1()) {
            targetBoard = game.getBoardPlayer2();
        } else {
            targetBoard = game.getBoardPlayer1();
        }

        Cell[][] cells = targetBoard.getCells();

        switch (cells[x][y]) {
            case HALO:
            case HIT:
            case MISS:
                return null;
            case EMPTY:
                cells[x][y] = Cell.MISS;
                break;
            case SHIP:
                cells[x][y] = Cell.HIT;
                boolean isSunk = isSunk(x, y, cells, targetBoard);
                addHalo(x, y, isSunk, cells);
                break;
        }

        Cell cellResult = cells[x][y];
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

        List<List<Integer>> coordinates = ship.getCoordinates();

        for (List<Integer> coordinate : coordinates) {
            int coorX = coordinate.get(0);
            int coorY = coordinate.get(1);
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
            List<List<Integer>> coordinates = ship.getCoordinates();

            for (List<Integer> coordinate : coordinates) {
                if (coordinate.get(0) == x && coordinate.get(1) == y) {
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
            List<List<Integer>> coordinates = ship.getCoordinates();
            for (List<Integer> coordinate : coordinates) {
                int coorX = coordinate.get(0);
                int coorY = coordinate.get(1);

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
}
