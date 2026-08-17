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
    private static final Logger log = LoggerFactory.getLogger(BattleService.class);

    public BattleService() {
    }

    public Cell shoot(Game game, Coordinate coordinate, Board targetBoard, BattleState battleState) {
        if (game == null) {
            log.error("Game is null");
            throw new IllegalArgumentException("Game is null");
        }
        if (targetBoard == null) {
            log.error("TargetBoard is null ");
            throw new IllegalArgumentException("Target board is null");
        }
        if (coordinate == null) {
            log.error("Coordinate is null ");
            throw new IllegalArgumentException("Coordinate is null");
        }
        if (!validateCoordinate(coordinate)) {
            log.error("Coordinate is not valid");
            throw new IllegalArgumentException("Coordinates are invalid");
        }

        int x = coordinate.x();
        int y = coordinate.y();

        Player turnPlayer = game.getTurnPlayer();

        if (turnPlayer == null) {
            log.error("turnPlayer is null");
            throw new NullPointerException("Turn player is null");
        }

        Cell[][] cells = targetBoard.getCells();

        if (cells == null) {
            log.error("cells is null");
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
                switchTurnPlayer(game);
                break;
            case SHIP:
                cells[x][y] = Cell.HIT;
                boolean isSunk = isSunk(coordinate, targetBoard, battleState);
                addHalo(coordinate, isSunk, targetBoard);
                break;
        }

        Cell cellResult = cells[x][y];
        battleState.setFirstShoot(true);
        return cellResult;
    }

    private boolean isSunk(Coordinate coordinate, Board targetBoard, BattleState battleState) {
        Cell[][] cells = targetBoard.getCells();
        Ship ship = getShip(coordinate, targetBoard);
        if (ship == null) {
            return false;
        }
        if (ship.getType() == TypeShip.OneDeckShip) {
            ship.setSunk(true);
            battleState.setGameOver(isGameOver(targetBoard));
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
            battleState.setGameOver(isGameOver(targetBoard));
        }
        return isSunk;
    }

    private boolean isGameOver(Board targetBoard) {
        List<Ship> ships = targetBoard.getShips();
        for (Ship ship : ships) {
            if (!ship.isSunk()) {
                return false;
            }
        }
        log.info("GAME OVER! All ships sunk");
        return true;
    }

    private Ship getShip(Coordinate coordinate, Board targetBoard) {
        List<Ship> ships = targetBoard.getShips();
        for (Ship ship : ships) {
            if (ship.getCoordinates().contains(coordinate)) {
                return ship;
            }
        }
        return null;
    }

    private boolean validateCoordinate(Coordinate coordinate) {
        int x = coordinate.x();
        int y = coordinate.y();
        return x >= 0 && x < Board.SIZE && y >= 0 && y < Board.SIZE;
    }

    private void addHalo(Coordinate coordinate, boolean isSunk, Board targetBoard) {
        Cell[][] cells =  targetBoard.getCells();
        Ship ship = getShip(coordinate, targetBoard);

        if (ship == null) {
            throw new IllegalStateException("Ship not found");
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

    public void endGame(Game game) {
        if (game == null) {
            log.error("Game is null");
            throw new IllegalArgumentException("Game is null");
        }
        if (game.getTurnPlayer() == null) {
            log.error("Turn player is null");
            throw new IllegalArgumentException("Turn player is null");
        }

        game.setWinner(game.getTurnPlayer());
        game.setGameStatus(GameStatus.ENDED);
        game.setEndTime(LocalDateTime.now());
    }

    public void switchTurnPlayer(Game game) {
        if (game == null) {
            log.error("Game is null");
            throw new IllegalArgumentException("Game is null");
        }
        Player turnPlayer = game.getTurnPlayer();
        if (turnPlayer.equals(game.getCreator())) {
            turnPlayer = game.getOpponent();
        } else {
            turnPlayer = game.getCreator();
        }
        game.setTurnPlayer(turnPlayer);
        log.info("Turn switched to: {}", turnPlayer.getName());
    }
}
