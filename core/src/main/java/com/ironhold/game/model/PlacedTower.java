package com.ironhold.game.model;

import com.ironhold.combat.AttackStrategy;

/**
 * Runtime tower instance placed on a build slot.
 */
public final class PlacedTower {

    private static final float MIN_RUNTIME_RANGE = 16f;
    private static final int MIN_RUNTIME_DAMAGE = 1;
    private static final float MIN_RUNTIME_FIRE_RATE_SEC = 0.1f;
    private static final float UPGRADE_DAMAGE_MUL = 1.25f;
    private static final float UPGRADE_RANGE_MUL = 1.10f;
    private static final float UPGRADE_FIRE_RATE_MUL = 0.92f;

    public static final int MAX_LEVEL = 3;

    private final String slotId;
    private final String towerId;
    private final float x;
    private final float y;
    private final int baseCost;
    /** Build cost + all upgrade costs spent on this tower. */
    private int totalInvestedGold;
    private int level = 1;
    private float range;
    private int damage;
    private float fireRateSec;
    private TowerTargetingPriority targetingPriority;
    private final AttackStrategy attackStrategy;
    private float cooldownSec;
    private String lockedTargetRuntimeId;

    /** Free-running accumulator that drives the idle core pulse. Wraps modulo PI*2. */
    private float pulsePhaseSec;

    public PlacedTower(
        String slotId,
        String towerId,
        float x,
        float y,
        int baseCost,
        float range,
        int damage,
        float fireRateSec,
        TowerTargetingPriority targetingPriority,
        AttackStrategy attackStrategy
    ) {
        this.slotId = slotId;
        this.towerId = towerId;
        this.x = x;
        this.y = y;
        this.baseCost = baseCost;
        this.totalInvestedGold = baseCost;
        this.range = Math.max(MIN_RUNTIME_RANGE, range);
        this.damage = Math.max(MIN_RUNTIME_DAMAGE, damage);
        this.fireRateSec = Math.max(MIN_RUNTIME_FIRE_RATE_SEC, fireRateSec);
        this.targetingPriority = targetingPriority != null ? targetingPriority : TowerTargetingPriority.FIRST;
        this.attackStrategy = java.util.Objects.requireNonNull(attackStrategy, "attackStrategy");
        this.cooldownSec = 0f;
        this.lockedTargetRuntimeId = null;
    }

    public AttackStrategy getAttackStrategy() {
        return attackStrategy;
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

    public int getLevel() {
        return level;
    }

    public int getTotalInvestedGold() {
        return totalInvestedGold;
    }

    public boolean isMaxLevel() {
        return level >= MAX_LEVEL;
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

    public int getUpgradeCost() {
        if (level >= MAX_LEVEL) {
            return 0;
        }
        return Math.max(1, Math.round(baseCost * level * 0.6f));
    }

    /**
     * Upgrades exactly one level when the player has enough gold.
     *
     * @return {@code true} if gold was spent and stats were updated
     */
    public boolean upgrade(EconomyState economy) {
        if (economy == null || level >= MAX_LEVEL) {
            return false;
        }
        int cost = getUpgradeCost();
        if (!economy.trySpend(cost)) {
            return false;
        }
        totalInvestedGold += cost;
        level++;
        damage = Math.max(MIN_RUNTIME_DAMAGE, Math.round(damage * UPGRADE_DAMAGE_MUL));
        range = Math.max(MIN_RUNTIME_RANGE, range * UPGRADE_RANGE_MUL);
        fireRateSec = Math.max(MIN_RUNTIME_FIRE_RATE_SEC, fireRateSec * UPGRADE_FIRE_RATE_MUL);
        lockedTargetRuntimeId = null;
        return true;
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

    public float getPulsePhaseSec() {
        return pulsePhaseSec;
    }

    public void tickPulse(float deltaSec) {
        this.pulsePhaseSec = (this.pulsePhaseSec + deltaSec) % (float) (Math.PI * 2.0);
    }
}
