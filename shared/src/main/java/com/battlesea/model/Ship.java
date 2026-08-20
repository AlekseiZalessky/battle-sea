package com.battlesea.model;

import com.battlesea.enums.Cell;
import com.battlesea.enums.TypeShip;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Setter
@Getter
public class Ship {
    private final String id = UUID.randomUUID().toString();
    private int x;
    private int y;
    private boolean isSunk;
    private boolean horizontal;
    private final TypeShip type;
    private final List<Coordinate> coordinates = new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(Ship.class);

    public Ship(int x, int y, boolean horizontal, TypeShip type) {
        this.x = x;
        this.y = y;
        this.horizontal = horizontal;
        this.type = type;
    }

    public boolean checkIsSunk(Cell[][] cells) {
        log.debug("checkIsSunk");
        for (Coordinate coordinate : coordinates) {
            if (cells[coordinate.x()][coordinate.y()] == Cell.SHIP){
                return false;
            }
        }
        isSunk = true;
        return true;
    }

    public void addCoordinates(Coordinate coordinates) {
        this.coordinates.add(coordinates);
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
