package com.battlesea.model;

import com.battlesea.enums.Cell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Board {
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

    public List<Ship> getShips() {
        return ships;
    }

    public void setShips(List<Ship> ships) {
        this.ships = ships;
    }

    public Cell[][] getCells() {
        return cells;
    }

    public void setCells(Cell[][] cells) {
        this.cells = cells;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public String toString() {
        return "Board{" +
            "ships=" + ships +
            ", cells=" + Arrays.toString(cells) +
            ", player=" + player +
            '}';
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
