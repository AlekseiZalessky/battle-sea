package com.battlesea.service;

import java.util.*;

import com.battlesea.enums.Cell;
import com.battlesea.enums.TypeShip;
import com.battlesea.model.Board;
import com.battlesea.model.Coordinate;
import com.battlesea.model.Player;
import com.battlesea.model.Ship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShipPlacementService {
    private Random random = new Random();
    private final int SIZE = Board.SIZE;
    private List<Coordinate> freeCoordinates = new ArrayList<>();
    private List<Ship> ships = new ArrayList<>();
    private Board board;
    private static final Logger log = LoggerFactory.getLogger(ShipPlacementService.class);

    {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                freeCoordinates.add(new Coordinate(i, j));
            }
        }
    }

    /**
     * Метод ручной расстановки корабля
     *
     * @param player
     * @param firstCoordinate
     * @param horizontalShip
     * @param typeShip
     * @return
     */
    public boolean placeShipManually(Player player, Coordinate firstCoordinate, boolean horizontalShip, TypeShip typeShip) {
        log.debug("placeShip");
        log.debug("firstCoordinate: {}", firstCoordinate);
        if (firstCoordinate == null) {
            throw new IllegalArgumentException("First coordinate is null");
        }
        if (!validateCoordinate(firstCoordinate.x(), firstCoordinate.y())) {
            return false;
        }
        if (!validateCountShip(typeShip)) {
            return false;
        }
        int size = typeShip.getSize();
        Cell[][] cells = board.getCells();

        boolean result = placeShip(firstCoordinate, cells, typeShip, size, horizontalShip);
        log.debug("placeShip(firstCoordinate, cells, typeShip, size, horizontalShip): {}", result);

//        board.setPlayer(player);
        board.setShips(ships);
        log.debug("count ship: {}", board.getShips().size());
        return result;
    }

    public boolean relocateShip(Player player, Coordinate newCoordinate, Coordinate oldCoordinate) {
        if (oldCoordinate == null || newCoordinate == null) {
            throw new IllegalArgumentException("First coordinate is null");
        }
        if (!validateCoordinate(oldCoordinate.x(), oldCoordinate.y()) || !validateCoordinate(newCoordinate.x(), newCoordinate.y())) {
            return false;
        }

        Cell[][] cells = board.getCells();
        Ship ship = getShip(oldCoordinate);
        if (ship == null) {
            throw new NullPointerException("ship is null");
        }

        int size = ship.getType().getSize();
        ships.remove(ship);
        clearCells(ship, cells);
        clearHalo(cells);
        addFullHalo(cells);

        boolean result = placeShip(newCoordinate, cells, ship.getType(), size, ship.isHorizontal());

        if (!result) {
            placeShip(oldCoordinate, cells, ship.getType(), size, ship.isHorizontal());
        }
        board.setCells(cells);
        board.setShips(ships);

        return result;
    }

    private boolean validateCountShip(TypeShip typeShip) {
        int countShip = 0;
        for (Ship ship : ships) {
            if (typeShip == ship.getType()) {
                countShip++;
            }
        }
        return typeShip.getCountShips() > countShip;
    }

    /**
     * Метод изменения ориентации(горизонт/вертикаль)
     *
     * @param player
     * @param coordinate
     * @return
     */
    public boolean changeOrientationShip(Player player, Coordinate coordinate) {
        if (!validateCoordinate(coordinate.x(), coordinate.y())) {
            return false;
        }
        Ship ship = getShip(coordinate);
        if (ship == null) {
            return false;
        }
        if (ship.getType() == TypeShip.OneDeckShip) {
            return false;
        }
        int firstX = ship.getX();
        int firstY = ship.getY();

        boolean horizontal = ship.isHorizontal();
        TypeShip typeShip = ship.getType();
        int sizeShip = typeShip.getSize();
        ships.remove(ship);

        Cell[][] cells = board.getCells();
        clearCells(ship, cells);
        clearHalo(cells);
        addFullHalo(cells);

        boolean result = placeShip(new Coordinate(firstX, firstY), cells, typeShip, sizeShip, !horizontal);

        board.setCells(cells);
        if (!result) {
            placeShip(new Coordinate(firstX, firstY), cells, typeShip, sizeShip, horizontal);
        }
        board.setShips(ships);
        return result;
    }

    private void clearCells(Ship ship, Cell[][] cells) {
        List<Coordinate> shipCoordinates = ship.getCoordinates();
        for (Coordinate coordinate : shipCoordinates) {
            cells[coordinate.x()][coordinate.y()] = Cell.EMPTY;
        }
    }

    private Ship getShip(Coordinate coordinate) {
        Ship ship;
        for (Ship sh : ships) {
            if (sh.getCoordinates().contains(coordinate)) {
                ship = sh;
                return ship;
            }
        }
        return null;
    }

    /**
     * Метод автоматической расстановки кораблей(кнопка auto)
     *
     * @param player
     * @return
     */
    public Board generateRandomShips(Player player) {
        board = new Board();

        Cell[][] cells = board.getCells();

        for (TypeShip type : TypeShip.values()) {

            int countShip = type.getCountShips();
            int sizeShip = type.getSize();

            while (countShip > 0) {
                Coordinate nextCoordinate = freeCoordinates.get(random.nextInt(freeCoordinates.size()));
                if (placeShip(nextCoordinate, cells, type, sizeShip, random.nextBoolean())) {
                    countShip--;
                }
            }
        }

        clearHalo(cells);
        board.setPlayer(player);
        board.setShips(ships);

        return board;
    }


    private boolean placeShip(Coordinate coordinate, Cell[][] cells, TypeShip type, int sizeShip, boolean horizontal) {

        int x = coordinate.x();
        int y = coordinate.y();

        if (!canPlaceShip(cells, horizontal, x, y, sizeShip)) {
            return false;
        }

        Ship ship = new Ship(x, y, horizontal, type);
        for (int i = 0; i < sizeShip; i++) {
            if (horizontal) {
                cells[x + i][y] = Cell.SHIP;
                ship.addCoordinates(new Coordinate(x + i, y));
            } else {
                cells[x][y + i] = Cell.SHIP;
                ship.addCoordinates(new Coordinate(x, y + i));
            }
        }

        ships.add(ship);
        addFullHaloAroundShip(ship, cells);
        deleteFromFreeCoordinates(cells);
        return true;
    }

    private boolean canPlaceShip(Cell[][] cells, boolean horizontal, int x, int y, int sizeShip) {
        for (int i = 0; i < sizeShip; i++) {
            int checkX = horizontal ? x + i : x;
            int checkY = horizontal ? y : y + i;
            if (!coordinateIsEmpty(checkX, checkY, cells)) {
                return false;
            }
        }
        return true;
    }

    private void deleteFromFreeCoordinates(Cell[][] cells) {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (cells[x][y] == Cell.SHIP || cells[x][y] == Cell.HALO) {
                    freeCoordinates.remove(new Coordinate(x, y));
                }
            }
        }
    }

    /**
     * Метод установки гало вокруг установленного корабля
     *
     * @param ship
     * @param cells
     */
    private void addFullHaloAroundShip(Ship ship, Cell[][] cells) {
        int sizeShip = ship.getType().getSize();
        int x = ship.getX();
        int y = ship.getY();
        boolean horizontal = ship.isHorizontal();

        for (int i = 0; i < sizeShip; i++) {
            int currentX = horizontal ? x + i : x;
            int currentY = horizontal ? y : y + i;

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (coordinateIsEmpty(currentX + dx, currentY + dy, cells)) {
                        cells[currentX + dx][currentY + dy] = Cell.HALO;
                    }
                }
            }
        }
    }

    /**
     * Метод установки гало на всем поле
     *
     * @param cells
     */
    private void addFullHalo(Cell[][] cells) {

        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                if (cells[i][j] == Cell.SHIP) {
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            if (coordinateIsEmpty(i + dx, j + dy, cells)) {
                                cells[i + dx][j + dy] = Cell.HALO;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Метод убирающий гало
     *
     * @param cells
     */
    public void clearHalo(Cell[][] cells) {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (cells[x][y] == Cell.HALO) {
                    cells[x][y] = Cell.EMPTY;
                }
            }
        }
    }

    private boolean coordinateIsEmpty(int x, int y, Cell[][] cells) {
        return validateCoordinate(x, y) && cells[x][y] == com.battlesea.enums.Cell.EMPTY;
    }

    private boolean validateCoordinate(int x, int y) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public int getCountShips() {
        return ships.size();
    }
}
