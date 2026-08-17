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

    private static final Logger log = LoggerFactory.getLogger(ShipPlacementService.class);

    public ShipPlacementService() {

    }

    /**
     * Метод ручной расстановки корабля
     *
     * @param firstCoordinate
     * @param horizontalShip
     * @param typeShip
     * @return
     */
    public boolean placeShipManually(Board board, Coordinate firstCoordinate, boolean horizontalShip, TypeShip typeShip) {
        log.debug("placeShip");
        log.debug("firstCoordinate: {}", firstCoordinate);
        if (board == null) {
            throw new IllegalArgumentException("Board is null");
        }
        if (firstCoordinate == null) {
            throw new IllegalArgumentException("First coordinate is null");
        }
        if (typeShip == null) {
            throw new IllegalArgumentException("TypeShip is null");
        }
        if (!validateCoordinate(firstCoordinate)) {
            return false;
        }

        List<Ship> ships = board.getShips();

        if (ships == null) {
            throw new IllegalStateException("Ships is not initialized");
        }

        if (!validateCountShip(ships, typeShip)) {
            return false;
        }

        int size = typeShip.getSize();
        Cell[][] cells = board.getCells();
        if (cells == null) {
            throw new IllegalStateException("Cells is not initialized");
        }

        boolean result = placeShip(ships, null, firstCoordinate, cells, typeShip, size, horizontalShip);
        log.debug("placeShip(firstCoordinate, cells, typeShip, size, horizontalShip): {}", result);

        board.setShips(ships);
        log.debug("count ship: {}", board.getShips().size());
        return result;
    }

    public boolean relocateShip(Board board, Coordinate newCoordinate, Coordinate oldCoordinate) {
        if (board == null) {
            throw new IllegalArgumentException("Board is not initialized");
        }
        if (newCoordinate == null) {
            throw new IllegalArgumentException("New coordinate is null");
        }
        if (oldCoordinate == null) {
            throw new IllegalArgumentException("Old coordinate is null");
        }
        if (!validateCoordinate(oldCoordinate) || !validateCoordinate(newCoordinate)) {
            return false;
        }

        Cell[][] cells = board.getCells();
        if (cells == null) {
            throw new IllegalStateException("Cells is not initialized");
        }

        List<Ship> ships = board.getShips();
        if (ships == null) {
            throw new IllegalStateException("Ships is not initialized");
        }

        Ship ship = getShip(ships, oldCoordinate);
        if (ship == null) {
            throw new IllegalArgumentException("Ship not found");
        }

        int size = ship.getType().getSize();
        ships.remove(ship);
        clearCells(ship, cells);
        clearHalo(cells);
        addFullHalo(cells);

        boolean result = placeShip(ships, null, newCoordinate, cells, ship.getType(), size, ship.isHorizontal());

        if (!result) {
            placeShip(ships, null, oldCoordinate, cells, ship.getType(), size, ship.isHorizontal());
        }
        board.setCells(cells);
        board.setShips(ships);

        return result;
    }

    private boolean validateCountShip(List<Ship> ships, TypeShip typeShip) {
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
     * @param coordinate
     * @return
     */
    public boolean changeOrientationShip(Board board, Coordinate coordinate) {
        if (coordinate == null) {
            throw new IllegalArgumentException("Coordinate is null");
        }
        if (board == null) {
            throw new IllegalArgumentException("Board is not initialized");
        }
        if (!validateCoordinate(coordinate)) {
            return false;
        }
        List<Ship> ships = board.getShips();
        Ship ship = getShip(ships, coordinate);
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

        List<Coordinate> freeCoordinates = initListFreeCoords(cells);

        boolean result = placeShip(ships, freeCoordinates, new Coordinate(firstX, firstY), cells, typeShip, sizeShip, !horizontal);

        board.setCells(cells);
        if (!result) {
            placeShip(ships, freeCoordinates, new Coordinate(firstX, firstY), cells, typeShip, sizeShip, horizontal);
        }
        board.setShips(ships);
        return result;
    }

    private void clearCells(Ship ship, Cell[][] cells) {
        if (ship == null || cells == null) {
            return;
        }
        List<Coordinate> shipCoordinates = ship.getCoordinates();
        for (Coordinate coordinate : shipCoordinates) {
            cells[coordinate.x()][coordinate.y()] = Cell.EMPTY;
        }
    }

    private Ship getShip(List<Ship> ships, Coordinate coordinate) {
        if (coordinate == null) {
            return null;
        }
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
    public Board generateRandomShips(Player player, Board board) {
        if (player == null) {
            throw new IllegalArgumentException("Player is null");
        }

        if (board == null) {
           throw new IllegalArgumentException("Board is not initialized");
        }

        Cell[][] cells = board.getCells();

        List<Coordinate> freeCoordinates = initListFreeCoords(cells);
        List<Ship> ships = new ArrayList<>();
        for (TypeShip type : TypeShip.values()) {

            int countShip = type.getCountShips();
            int sizeShip = type.getSize();

            while (countShip > 0) {
                Coordinate nextCoordinate = freeCoordinates.get(random.nextInt(freeCoordinates.size()));
                if (placeShip(ships, freeCoordinates, nextCoordinate, cells, type, sizeShip, random.nextBoolean())) {
                    countShip--;
                }
            }
        }

        clearHalo(cells);
        board.setPlayer(player);
        board.setShips(ships);
        board.setCells(cells);
        return board;
    }


    private boolean placeShip(List<Ship> ships, List<Coordinate> freeCoordinates, Coordinate coordinate, Cell[][] cells, TypeShip type, int sizeShip, boolean horizontal) {
        int x = coordinate.x();
        int y = coordinate.y();

        if (!canPlaceShip(cells, horizontal, coordinate, sizeShip)) {
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
        if (freeCoordinates != null) {
            deleteFromFreeCoordinates(freeCoordinates, cells);
        }
        return true;
    }

    private boolean canPlaceShip(Cell[][] cells, boolean horizontal, Coordinate coordinate, int sizeShip) {
        int x = coordinate.x();
        int y = coordinate.y();
        for (int i = 0; i < sizeShip; i++) {
            int checkX = horizontal ? x + i : x;
            int checkY = horizontal ? y : y + i;
            if (!coordinateIsEmpty(new Coordinate(checkX, checkY), cells)) {
                return false;
            }
        }
        return true;
    }

    private void deleteFromFreeCoordinates(List<Coordinate> freeCoordinates, Cell[][] cells) {
        if (cells == null) {
            return;
        }

        freeCoordinates.removeIf(coordinate -> {
            int x = coordinate.x();
            int y = coordinate.y();
            return cells[x][y] == Cell.SHIP || cells[x][y] == Cell.HALO;
        });
    }

    /**
     * Метод установки гало вокруг установленного корабля
     *
     * @param ship
     * @param cells
     */
    private void addFullHaloAroundShip(Ship ship, Cell[][] cells) {
        if (ship == null || cells == null) {
            return;
        }

        int sizeShip = ship.getType().getSize();
        int x = ship.getX();
        int y = ship.getY();
        boolean horizontal = ship.isHorizontal();

        for (int i = 0; i < sizeShip; i++) {
            int currentX = horizontal ? x + i : x;
            int currentY = horizontal ? y : y + i;

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (coordinateIsEmpty(new Coordinate(currentX + dx, currentY + dy), cells)) {
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
        if (cells == null) {
            return;
        }

        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                if (cells[i][j] == Cell.SHIP) {
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            if (coordinateIsEmpty(new Coordinate(i + dx, j + dy), cells)) {
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
        if (cells == null) {
            return;
        }
        int size = Board.SIZE;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (cells[x][y] == Cell.HALO) {
                    cells[x][y] = Cell.EMPTY;
                }
            }
        }
    }

    private boolean coordinateIsEmpty(Coordinate coordinate, Cell[][] cells) {
        if (coordinate == null || cells == null) {
            return false;
        }
        return validateCoordinate(coordinate) && cells[coordinate.x()][coordinate.y()] == com.battlesea.enums.Cell.EMPTY;
    }

    private boolean validateCoordinate(Coordinate coordinate) {
        if (coordinate == null) {
            return false;
        }
        int size = Board.SIZE;
        int x = coordinate.x();
        int y = coordinate.y();
        return x >= 0 && x < size && y >= 0 && y < size;
    }

    private List<Coordinate> initListFreeCoords(Cell[][] cells) {
        int size = Board.SIZE;
        List<Coordinate> freeCoordinates = new ArrayList<>();
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                if (cells[i][j] == null || cells[i][j] == Cell.EMPTY) {
                    freeCoordinates.add(new Coordinate(i, j));
                }
            }
        }
        return freeCoordinates;
    }

}
