package com.ironhold.game.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Expanding flame cone burst from a Flamethrower tower.
 *
 * <p>Damage is applied once per enemy when they enter the cone while it grows.
 */
public final class FlameConeEffect {

    private final float originX;
    private final float originY;
    private final float aimAngleRad;
    private final float maxRange;
    private final float halfAngleRad;
    private final int damage;
    private final float maxTtlSec;
    private float ttlSec;
    private final Set<String> damagedEnemyIds = new HashSet<>();

    public FlameConeEffect(
        float originX,
        float originY,
        float aimAngleRad,
        float maxRange,
        float halfAngleRad,
        int damage,
        float ttlSec
    ) {
        this.originX = originX;
        this.originY = originY;
        this.aimAngleRad = aimAngleRad;
        this.maxRange = maxRange;
        this.halfAngleRad = halfAngleRad;
        this.damage = damage;
        this.maxTtlSec = ttlSec;
        this.ttlSec = ttlSec;
    }

    public float getOriginX() { return originX; }
    public float getOriginY() { return originY; }
    public float getAimAngleRad() { return aimAngleRad; }
    public float getMaxRange() { return maxRange; }
    public float getHalfAngleRad() { return halfAngleRad; }
    public int getDamage() { return damage; }
    public float getTtlSec() { return ttlSec; }

    public void setTtlSec(float v) { this.ttlSec = v; }

    public boolean hasDamaged(String enemyRuntimeId) {
        return damagedEnemyIds.contains(enemyRuntimeId);
    }

    public void markDamaged(String enemyRuntimeId) {
        damagedEnemyIds.add(enemyRuntimeId);
    }

    /** Current cone reach along the aim axis. */
    public float getReachDistance() {
        return maxRange * getProgress();
    }

    /** {@code 0} at spawn → {@code 1} at extinction. */
    public float getProgress() {
        if (maxTtlSec <= 0f) return 1f;
        float p = 1f - ttlSec / maxTtlSec;
        return Math.max(0f, Math.min(1f, p));
    }
}
