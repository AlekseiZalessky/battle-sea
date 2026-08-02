package com.battlesea.model;

import com.battlesea.enums.TypeShip;

import java.util.ArrayList;
import java.util.List;

public class Ship {
    private final int x;
    private final int y;
    private boolean isSunk;
    private final boolean horizontal;
    private final TypeShip type;
    private final List<List<Integer>> coordinates = new ArrayList<>();

    public Ship(int x, int y, boolean horizontal, TypeShip type) {
        this.x = x;
        this.y = y;
        this.horizontal = horizontal;
        this.type = type;
    }

    public void setSunk(boolean sunk) {
        this.isSunk = sunk;
    }

    public void addCoordinates(List<Integer> coordinates) {
        this.coordinates.add(coordinates);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isSunk() {
        return isSunk;
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public TypeShip getType() {
        return type;
    }

    public List<List<Integer>> getCoordinates() {
        return coordinates;
    }

    @Override
    public String toString() {
        return "Ship{" +
            "x=" + x +
            ", y=" + y +
            ", isSunk=" + isSunk +
            ", horizontal=" + horizontal +
            ", type=" + type +
            ", coordinates=" + coordinates +
            '}';
    }
}
