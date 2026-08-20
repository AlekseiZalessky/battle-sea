package com.battlesea.model;

import com.battlesea.enums.Cell;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Setter
@Getter
public class Board {
    private static final Logger log = LoggerFactory.getLogger(Board.class);
    public static final int SIZE = 10;
    private List<Ship> ships;
    private Cell[][] cells = new Cell[SIZE][SIZE];
    private Player player;

    public Board() {
        this.ships = new ArrayList<>();
        init();
    }

    public void init() {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                cells[x][y] = Cell.EMPTY;
            }
        }
    }

    public Ship getShip(Coordinate coordinate) {
        for (Ship ship : ships) {
            if (ship.getCoordinates().contains(coordinate)) {
                return ship;
            }
        }
        return null;
    }

    public boolean allShipsIsSunk(){
        log.debug("allShipsIsSunk");
        for (Ship ship : ships) {
            if (!ship.isSunk()) {
                log.debug("return false");
                return false;
            }
        }
        log.debug("return true");
        return true;
    }

    public void addHalo(Coordinate coordinate) {
        Ship ship = getShip(coordinate);
        boolean isSunk = ship.isSunk();

        if (isSunk) {
            List<Coordinate> coordinates = ship.getCoordinates();
            for (Coordinate coord : coordinates) {
                addHaloAroundCell(coord, true);
            }
        } else {
            addHaloAroundCell(coordinate, false);
        }
    }

    private void addHaloAroundCell(Coordinate coordinate, boolean isSunk) {
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
                if (validateCoordinate(x + dx, y + dy) && cells[x + dx][y + dy] == Cell.EMPTY) {
                    cells[x + dx][y + dy] = Cell.HALO;
                }
            }
        }
    }

    private boolean validateCoordinate(int x, int y) {
        return x >= 0 && x < Board.SIZE && y >= 0 && y < Board.SIZE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Board board = (Board) o;
        return Objects.equals(ships, board.ships) && Objects.deepEquals(cells, board.cells) && Objects.equals(player, board.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ships, Arrays.deepHashCode(cells), player);
    }
}
