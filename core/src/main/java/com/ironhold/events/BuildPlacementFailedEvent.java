package com.ironhold.events;

import java.util.Objects;

public final class BuildPlacementFailedEvent implements GameEvent {

    private final String towerId;
    private final String reasonCode;
    private final float worldX;
    private final float worldY;

    public BuildPlacementFailedEvent(String towerId, String reasonCode, float worldX, float worldY) {
        this.towerId = towerId;
        this.reasonCode = Objects.requireNonNull(reasonCode, "reasonCode");
        this.worldX = worldX;
        this.worldY = worldY;
    }

    public String getTowerId() {
        return towerId;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public float getWorldX() {
        return worldX;
    }

    public float getWorldY() {
        return worldY;
    }
}
