package com.ironhold.game.model;

/**
 * Build slot skeleton on map for future placement mechanics.
 */
public final class BuildSlot {

    private final String slotId;
    private final float x;
    private final float y;
    private final boolean occupied;
    private final String towerId;

    public BuildSlot(String slotId, float x, float y, boolean occupied, String towerId) {
        this.slotId = slotId;
        this.x = x;
        this.y = y;
        this.occupied = occupied;
        this.towerId = towerId;
    }

    public String getSlotId() {
        return slotId;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public String getTowerId() {
        return towerId;
    }
}
