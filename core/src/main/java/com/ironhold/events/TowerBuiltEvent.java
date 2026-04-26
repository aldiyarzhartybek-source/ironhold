package com.ironhold.events;

public final class TowerBuiltEvent implements GameEvent {

    private final String towerId;
    private final String slotId;
    private final int cost;

    public TowerBuiltEvent(String towerId, String slotId, int cost) {
        this.towerId = towerId;
        this.slotId = slotId;
        this.cost = cost;
    }

    public String getTowerId() {
        return towerId;
    }

    public String getSlotId() {
        return slotId;
    }

    public int getCost() {
        return cost;
    }
}
