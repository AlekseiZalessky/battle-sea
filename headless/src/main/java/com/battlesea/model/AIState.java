package com.battlesea.model;

public class AIState {
    private boolean hasHit;
    private Coordinate coordinateHit;
    private boolean hasOrientation;
    private boolean isHorizontal;

    public AIState() {
        this.hasHit = false;
        this.coordinateHit = null;
        this.hasOrientation = false;
        this.isHorizontal = false;
    }

    public boolean isHasHit() {
        return hasHit;
    }

    public void setHasHit(boolean hasHit) {
        this.hasHit = hasHit;
    }

    public Coordinate getCoordinateHit() {
        return coordinateHit;
    }

    public void setCoordinateHit(Coordinate coordinateHit) {
        this.coordinateHit = coordinateHit;
    }

    public boolean isHasOrientation() {
        return hasOrientation;
    }

    public void setHasOrientation(boolean hasOrientation) {
        this.hasOrientation = hasOrientation;
    }

    public boolean isHorizontal() {
        return isHorizontal;
    }

    public void setHorizontal(boolean horizontal) {
        isHorizontal = horizontal;
    }
}
