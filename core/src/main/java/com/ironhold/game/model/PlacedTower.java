package com.ironhold.game.model;

/**
 * Runtime tower instance placed on a build slot.
 */
public final class PlacedTower {

    private final String slotId;
    private final String towerId;
    private final float x;
    private final float y;
    private final float range;
    private final int damage;
    private final float fireRateSec;
    private TowerTargetingPriority targetingPriority;
    private float cooldownSec;
    private String lockedTargetRuntimeId;

    /** Free-running accumulator that drives the idle core pulse. Wraps modulo PI*2. */
    private float pulsePhaseSec;

    public PlacedTower(
        String slotId,
        String towerId,
        float x,
        float y,
        float range,
        int damage,
        float fireRateSec,
        TowerTargetingPriority targetingPriority
    ) {
        this.slotId = slotId;
        this.towerId = towerId;
        this.x = x;
        this.y = y;
        this.range = range;
        this.damage = damage;
        this.fireRateSec = fireRateSec;
        this.targetingPriority = targetingPriority != null ? targetingPriority : TowerTargetingPriority.FIRST;
        this.cooldownSec = 0f;
        this.lockedTargetRuntimeId = null;
    }

    public String getSlotId() {
        return slotId;
    }

    public String getTowerId() {
        return towerId;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getRange() {
        return range;
    }

    public int getDamage() {
        return damage;
    }

    public float getFireRateSec() {
        return fireRateSec;
    }

    public float getCooldownSec() {
        return cooldownSec;
    }

    public void setCooldownSec(float cooldownSec) {
        this.cooldownSec = cooldownSec;
    }

    public TowerTargetingPriority getTargetingPriority() {
        return targetingPriority;
    }

    public void setTargetingPriority(TowerTargetingPriority targetingPriority) {
        this.targetingPriority = targetingPriority != null ? targetingPriority : TowerTargetingPriority.FIRST;
        this.lockedTargetRuntimeId = null;
    }

    public String getLockedTargetRuntimeId() {
        return lockedTargetRuntimeId;
    }

    public void setLockedTargetRuntimeId(String lockedTargetRuntimeId) {
        this.lockedTargetRuntimeId = lockedTargetRuntimeId;
    }

    // ── Visual state ────────────────────────────────────────────────────────

    public float getPulsePhaseSec() {
        return pulsePhaseSec;
    }

    /** Advances the free-running pulse clock; wraps to keep the value bounded. */
    public void tickPulse(float deltaSec) {
        this.pulsePhaseSec = (this.pulsePhaseSec + deltaSec) % (float) (Math.PI * 2.0);
    }
}
