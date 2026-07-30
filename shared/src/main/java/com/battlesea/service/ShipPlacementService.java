package com.battlesea.service;

import java.util.*;

import com.battlesea.enums.Cell;
import com.battlesea.enums.TypeShip;
import com.battlesea.model.Board;
import com.battlesea.model.Player;
import com.battlesea.model.Ship;

public class ShipPlacementService {
    private Random random = new Random();
    private final int SIZE = Board.SIZE;
    private List<List<Integer>> freeCoordinates = new ArrayList<>();
    private List<Ship> ships = new ArrayList<>();

    {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                freeCoordinates.add(List.of(i, j));
            }
        }
    }

    public Board generateRandomShips(Player player) {
        Board board = new Board();
        board.init();

        Cell[][] cells = board.getCells();

        for (TypeShip type : TypeShip.values()) {

            int countShip = type.getCountShips();
            int sizeShip = type.getSize();

            while (countShip > 0) {
                if (placeShip(cells, type, sizeShip)) {
                    countShip--;
                }
            }
        }

        board.setPlayer(player);
        board.setShips(ships);

        return board;
    }

    private boolean placeShip(Cell[][] cells, TypeShip type, int sizeShip) {
        List<Integer> nextCoordinate = freeCoordinates.get(random.nextInt(freeCoordinates.size()));

        int x = nextCoordinate.get(0);
        int y = nextCoordinate.get(1);
        boolean horizontal = random.nextBoolean();

        if (!canPlaceShip(cells, horizontal, x, y, sizeShip)) {
            return false;
        }

        Ship ship = new Ship(x, y, horizontal, type);
        for (int i = 0; i < sizeShip; i++) {
            if (horizontal) {
                cells[x + i][y] = Cell.SHIP;
                ship.addCoordinates(List.of(x + i, y));
            } else {
                cells[x][y + i] = Cell.SHIP;
                ship.addCoordinates(List.of(x, y + i));
            }
        }

        ships.add(ship);
        addFullHalo(ship, cells);
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
                    List<Integer> coorForDel = new ArrayList<>();
                    coorForDel.add(x);
                    coorForDel.add(y);
                    freeCoordinates.remove(coorForDel);
                }
            }
        }
    }

    private void addFullHalo(Ship ship, Cell[][] cells) {
        int sizeShip = ship.getType().getSize();
        int x = ship.getX();
        int y = ship.getY();
        boolean horizontal = ship.isHorizontal();

        for (int i = 0; i < sizeShip; i++) {
            int currentX = horizontal ? x + i : x;
            int currentY = horizontal ? y  : y + i ;

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (coordinateIsEmpty(currentX + dx, currentY + dy, cells)) {
                        cells[currentX + dx][currentY + dy] = Cell.HALO;
                    }
                }
            }
        }
    }

    private boolean coordinateIsEmpty(int x, int y, Cell[][] cells) {
        return validateCoordinates(x, y) && cells[x][y] == Cell.EMPTY;
    }

    private boolean validateCoordinates(int x, int y) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
    }
}
