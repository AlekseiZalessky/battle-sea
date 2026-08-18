package com.battlesea.model;

import com.battlesea.enums.TypeShip;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Ship {
    private final String id = UUID.randomUUID().toString();
    private int x;
    private int y;
    private boolean isSunk;
    private boolean horizontal;
    private final TypeShip type;
    private final List<Coordinate> coordinates = new ArrayList<>();

    public Ship(int x, int y, boolean horizontal, TypeShip type) {
        this.x = x;
        this.y = y;
        this.horizontal = horizontal;
        this.type = type;
    }

    public void setSunk(boolean sunk) {
        this.isSunk = sunk;
    }

    public void addCoordinates(Coordinate coordinates) {
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

    public void setHorizontal(boolean horizontal) {
        this.horizontal = horizontal;
    }

    public TypeShip getType() {
        return type;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ship ship = (Ship) o;
        return Objects.equals(id, ship.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
