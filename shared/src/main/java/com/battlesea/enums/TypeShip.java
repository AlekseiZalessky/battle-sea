package com.battlesea.enums;

public enum TypeShip {
    FourDeckShip(4, 1),
    ThreeDeckShip(3, 2),
    TwoDeckShip(2, 3),
    OneDeckShip(1, 4); //(количество палуб, количество кораблей)

    private final int size;
    private final int countShips;

    TypeShip(int size, int countShips) {
        this.size = size;
        this.countShips = countShips;
    }

    public int getSize() {
        return size;
    }

    public int getCountShips() {
        return countShips;
    }
}
