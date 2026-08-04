package com.battlesea.service;

import com.battlesea.enums.Cell;
import com.battlesea.enums.TypeShip;
import com.battlesea.model.Board;
import com.battlesea.model.Coordinate;
import com.battlesea.model.Game;
import com.battlesea.model.Ship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIService {
    private static final Logger log = LoggerFactory.getLogger(AIService.class);
    private Game game;
    private List<Coordinate> coorForMoveAI = new ArrayList<>();
    private final int SIZE_BOARD = Board.SIZE;
    private Board targetBoard;
    private Cell[][] cells;
    private boolean hasHit;
    private Coordinate coordinateHit;
    private boolean hasOrientation;
    private boolean isHorizontal;
    private final Random random = new Random();

    public AIService(Game game) {
        this.game = game;
        this.targetBoard = game.getBoardCreator();
        this.cells = targetBoard.getCells();
    }

    {
        for (int i = 0; i < SIZE_BOARD; i++) {
            for (int j = 0; j < SIZE_BOARD; j++) {
                coorForMoveAI.add(new Coordinate(i, j));
            }
        }
    }

    public Coordinate chooseCoordinate() {
        log.debug("Choose coordinate");
        Coordinate coordinateForNextShoot;
        if (!hasHit) {
            log.debug("hasHit false");
            coordinateForNextShoot = randomCoordinate(coorForMoveAI);
            return coordinateForNextShoot;
        }

        int x = coordinateHit.x();
        int y = coordinateHit.y();
        List<Coordinate> coordinatesForNextShoot = new ArrayList<>();

        if (!hasOrientation) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (Math.abs(dx) == 1 && Math.abs(dy) == 1) {
                        continue;
                    }

                    int newX = x + dx;
                    int newY = y + dy;
                    if (x == newX && y == newY) {
                        continue;
                    }
                    if (validateCoordinate(newX, newY) && (cells[newX][newY] == Cell.EMPTY || cells[newX][newY] == Cell.SHIP)) {
                        coordinatesForNextShoot.add(new Coordinate(newX, newY));
                    }
                }
            }
            coordinateForNextShoot = randomCoordinate(coordinatesForNextShoot);
            return coordinateForNextShoot;
        } else {
            log.debug("hasOrientation true");
            checkCoordinate(coordinatesForNextShoot);
            coordinateForNextShoot = randomCoordinate(coordinatesForNextShoot);
            log.debug("coordinateForNextShoot: {}", coordinateForNextShoot);
            return coordinateForNextShoot;

        }
    }

    private void checkCoordinate(List<Coordinate> coordinatesForNextShoot) {

        int x = coordinateHit.x();
        int y = coordinateHit.y();
        boolean flag = false;

        while (!flag) {
            if (isHorizontal) {
                x--;
            } else {
                y--;
            }
            if (!validateCoordinate(x, y)) {
                break;
            }
            switch (cells[x][y]) {
                case EMPTY:
                case SHIP:
                    coordinatesForNextShoot.add(new Coordinate(x, y));
                    flag = true;
                    break;
                case HIT:
                    continue;
                case MISS:
                case HALO:
                    flag = true;
                    break;
            }
        }

        x = coordinateHit.x();
        y = coordinateHit.y();
        flag = false;

        while (!flag) {
            if (isHorizontal) {
                x++;
            } else {
                y++;
            }
            if (!validateCoordinate(x, y)) {
                break;
            }
            switch (cells[x][y]) {
                case EMPTY:
                case SHIP:
                    coordinatesForNextShoot.add(new Coordinate(x, y));
                    flag = true;
                    break;
                case HIT:
                    continue;
                case MISS:
                case HALO:
                    flag = true;
                    break;
            }
        }
    }

    private Coordinate randomCoordinate(List<Coordinate> coordinates) {
        if (coordinates == null || coordinates.isEmpty()) {
            throw new IllegalArgumentException("Invalid list of coordinates");
        }
        log.debug("List coordinates: {}", coordinates);

        return coordinates.get(random.nextInt(coordinates.size()));
    }

    private boolean validateCoordinate(int x, int y) {
        return x >= 0 && y >= 0 && x < SIZE_BOARD && y < SIZE_BOARD;
    }

    public void removeCoordinate(Coordinate coordinate) {
        if (coordinate == null) {
            throw new IllegalArgumentException("Invalid coordinate");
        }
        coorForMoveAI.remove(coordinate);
    }

    public void setHasHit(boolean hasHit) {
        this.hasHit = hasHit;
    }

    public void setCoordinateHit(Coordinate coordinateHit) {
        if (coordinateHit == null) {
            throw new IllegalArgumentException("Invalid coordinate");
        }
        this.coordinateHit = coordinateHit;
    }

    public boolean isHasHit() {
        return hasHit;
    }

    private Ship getShip(Coordinate coordinate) {
        if (coordinate == null) {
            throw new IllegalArgumentException("Invalid coordinate");
        }
        List<Ship> ships = targetBoard.getShips();
        for (Ship ship : ships) {
            if (ship.getCoordinates().contains(coordinate)) {
                log.debug("Found ship: {}", ship);
                return ship;
            }
        }
        log.debug("Ship not found");
        return null;
    }

    public void setOrientation(Coordinate coordinate) {
        if (coordinate == null) {
            throw new IllegalArgumentException("Invalid coordinate");
        }
        Ship ship = getShip(coordinate);
        if (ship == null) {
            throw new NullPointerException("Ship not found");
        }
        if (ship.getType() == TypeShip.OneDeckShip) {
            return;
        }
        hasOrientation = true;
        isHorizontal = ship.isHorizontal();
    }

    public boolean isSunk(Coordinate coordinate) {
        if (coordinate == null) {
            throw new IllegalArgumentException("Invalid coordinate");
        }
        Ship ship = getShip(coordinate);
        if (ship == null) {
            throw new NullPointerException("Ship not found");
        }
        if (ship.getType() == TypeShip.OneDeckShip) {
            log.debug("Found ship is sunk: {}", ship);
            return true;
        }
        if (ship.isSunk()) {
            update();
            return true;
        }
        return false;
    }

    private void update() {
        log.debug("Ship is Sunk. Updating...");
        hasHit = false;
        coordinateHit = null;
        hasOrientation = false;
        isHorizontal = false;
    }
}
