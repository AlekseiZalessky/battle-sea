package com.battlesea.service;

import com.battlesea.enums.Cell;
import com.battlesea.enums.TypeShip;
import com.battlesea.model.AIState;
import com.battlesea.model.Board;
import com.battlesea.model.Coordinate;
import com.battlesea.model.Ship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIService {
    private static final Logger log = LoggerFactory.getLogger(AIService.class);
    private final Random random = new Random();

    public AIService() {

    }

    public Coordinate chooseCoordinate(Board board, List<Coordinate> coorForMoveAI, AIState aiState) {
        if (board == null) {
            log.error("Board is null");
            throw new IllegalArgumentException("Board can't be null");
        }
        if (coorForMoveAI == null) {
            log.error("coorForMoveAI is null");
            throw new IllegalArgumentException("COORDINATE can't be null");
        }
        if (aiState == null) {
            log.error("AIState is null");
            throw new IllegalArgumentException("AIState can't be null");
        }

        Cell[][] cells = board.getCells();
        Coordinate coordinateForNextShoot;
        if (!aiState.isHasHit()) {
            coordinateForNextShoot = randomCoordinate(coorForMoveAI);
            return coordinateForNextShoot;
        }

        int x = aiState.getCoordinateHit().x();
        int y = aiState.getCoordinateHit().y();
        List<Coordinate> coordinatesForNextShoot = new ArrayList<>();

        if (!aiState.isHasOrientation()) {
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
            checkCoordinate(coordinatesForNextShoot, cells, aiState);
            coordinateForNextShoot = randomCoordinate(coordinatesForNextShoot);
            return coordinateForNextShoot;
        }
    }

    private void checkCoordinate(List<Coordinate> coordinatesForNextShoot, Cell[][] cells, AIState aiState) {

        int x = aiState.getCoordinateHit().x();
        int y = aiState.getCoordinateHit().y();
        boolean flag = false;

        while (!flag) {
            if (aiState.isHorizontal()) {
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

        x = aiState.getCoordinateHit().x();
        y = aiState.getCoordinateHit().y();
        flag = false;

        while (!flag) {
            if (aiState.isHorizontal()) {
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
        return coordinates.get(random.nextInt(coordinates.size()));
    }

    private boolean validateCoordinate(int x, int y) {
        return x >= 0 && y >= 0 && x < Board.SIZE && y < Board.SIZE;
    }

    public void removeCoordinate(Coordinate coordinate, List<Coordinate> coorForMoveAI) {
        if (coordinate == null) {
            log.error("coordinate is null");
            throw new IllegalArgumentException("Invalid coordinate");
        }
        if (coorForMoveAI == null) {
            log.error("coorForMoveAI is null");
            throw new IllegalArgumentException("CoorForMoveAI is null");
        }
        coorForMoveAI.remove(coordinate);
    }

    private Ship getShip(Coordinate coordinate, Board targetBoard) {
        List<Ship> ships = targetBoard.getShips();
        log.debug("getShip: {}", ships);
        for (Ship ship : ships) {
            if (ship.getCoordinates().contains(coordinate)) {
                log.debug("Found ship: {}", ship);
                return ship;
            }
        }
        log.debug("Ship not found");
        return null;
    }

    public void setOrientation(Coordinate coordinate, Board targetBoard, AIState aiState) {
        if (coordinate == null) {
            log.error("coordinate is null");
            throw new IllegalArgumentException("Invalid coordinate");
        }
        if (targetBoard == null) {
            log.error("targetBoard is null");
            throw new IllegalArgumentException("targetBoard is null");
        }
        if (aiState == null) {
            log.error("AIState is null");
            throw new IllegalArgumentException("AIState is null");
        }
        Ship ship = getShip(coordinate, targetBoard);
        if (ship == null) {
            log.error("ship not found");
            throw new NullPointerException("Ship not found");
        }
        if (ship.getType() == TypeShip.OneDeckShip) {
            return;
        }
        aiState.setHasOrientation(true);
        aiState.setHorizontal(ship.isHorizontal());
    }

    public boolean isSunk(Coordinate coordinate, Board targetBoard, AIState aiState) {
        if (coordinate == null) {
            log.error("coordinate is null");
            throw new IllegalArgumentException("Invalid coordinate");
        }
        if (targetBoard == null) {
            log.error("targetBoard is null");
            throw new IllegalArgumentException("targetBoard is null");
        }
        if (aiState == null) {
            log.error("AIState is null");
            throw new IllegalArgumentException("AIState is null");
        }
        Ship ship = getShip(coordinate, targetBoard);
        if (ship == null) {
            throw new NullPointerException("Ship not found");
        }
        if (ship.getType() == TypeShip.OneDeckShip) {
            return true;
        }
        if (ship.isSunk()) {
            update(aiState);
            return true;
        }
        return false;
    }

    private void update(AIState aiState) {
        log.debug("Ship is Sunk. Updating...");
        aiState.setHasOrientation(false);
        aiState.setHorizontal(false);
        aiState.setHasHit(false);
        aiState.setCoordinateHit(null);
    }

    public void updateFreeCoordinates(List<Coordinate> coorForMoveAI, Cell[][] cells) {
        if (coorForMoveAI == null) {
            log.error("coorForMoveAI is null");
            throw new IllegalArgumentException("coorForMoveAI is null");
        }
        if (cells == null) {
            log.error("cells is null");
            throw new IllegalArgumentException("cells is null");
        }
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                if (cells[i][j] == Cell.HALO || cells[i][j] == Cell.MISS || cells[i][j] == Cell.HIT) {
                    coorForMoveAI.remove(new Coordinate(i, j));
                }
            }
        }
    }

    public List<Coordinate> initListCoorForMoveAI(Cell[][] cells) {
        if (cells == null) {
            log.error("cells is null");
            throw new IllegalArgumentException("Invalid cells");
        }
        List<Coordinate> coorForMoveAI = new ArrayList<>();
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                coorForMoveAI.add(new Coordinate(i, j));
            }
        }
        return coorForMoveAI;
    }
}
