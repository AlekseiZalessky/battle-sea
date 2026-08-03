package com.battlesea.service;

import com.battlesea.enums.Cell;
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
    private List<Coordinate> coorForMoveAI = new ArrayList<>();
    private final int SIZE_BOARD = Board.SIZE;
    private Board targetBoard;
    private boolean hasHit;
    private Coordinate coordinateHit;
    private boolean orientation;
    private boolean horizontal;
    private final Random random = new Random();

    public AIService(Game game) {
        this.targetBoard = game.getBoardCreator();
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
            coordinateForNextShoot = randomCoordinate(coorForMoveAI);
            coorForMoveAI.remove(coordinateForNextShoot);//вынести в вызывающий метод
            return coordinateForNextShoot;
        }

        int x = coordinateHit.x();
        int y = coordinateHit.y();
        List<Coordinate> coordinatesForNextShoot = new ArrayList<>();
        Cell[][] cells = targetBoard.getCells();
        if (!orientation) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (Math.abs(dx) == 1 && Math.abs(dy) == 1) {
                        continue;
                    }

                    int newX = x + dx;
                    int newY = y + dy;
                    if (validateCoordinate(newX, newY) && cells[newX][newY] == Cell.EMPTY) {
                        coordinatesForNextShoot.add(new Coordinate(newX, newY));
                    }
                }
            }

            coordinateForNextShoot = randomCoordinate(coordinatesForNextShoot);
            coorForMoveAI.remove(coordinateForNextShoot); //вынести в вызывающий метод
            return coordinateForNextShoot;
        } else {

            checkCoordinate(coordinatesForNextShoot, cells);
            coordinateForNextShoot = randomCoordinate(coordinatesForNextShoot);
            coorForMoveAI.remove(coordinateForNextShoot); //вынести в вызывающий метод
            return coordinateForNextShoot;

        }
    }

    private void checkCoordinate(List<Coordinate> coordinatesForNextShoot, Cell[][] cells) {

        int x = coordinateHit.x();
        int y = coordinateHit.y();

        while (true) {
            if (horizontal) {
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
                    break;
                case HIT:
                    continue;
                case MISS:
                case HALO:
                    break;
            }
        }

        x = coordinateHit.x();
        y = coordinateHit.y();

        while (true) {
            if (horizontal) {
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
                    break;
                case HIT:
                    continue;
                case MISS:
                case HALO:
                    break;
            }
        }
    }

    private Coordinate randomCoordinate(List<Coordinate> coordinates) {
        log.debug("List coordinatesForNextShoot: {}", coordinates);
        return coordinates.get(random.nextInt(coordinates.size()));
    }

    private boolean validateCoordinate(int x, int y) {
        log.debug("Validating coordinate x: {}, y: {}", x, y);
        return x >= 0 && y >= 0 && x < SIZE_BOARD && y < SIZE_BOARD;
    }

    private Ship getShip(Coordinate coordinate) {
        List<Ship> ships = targetBoard.getShips();
        for (Ship ship : ships) {
            if (ship.getCoordinates().contains(coordinate)) {
                return ship;
            }
        }
        return null;
    }
}
