package com.ironhold.game.model;

/**
 * Runtime projectile emitted by a tower and flying toward an enemy.
 */
public final class ActiveProjectile {

    private final String runtimeId;
    private final String targetEnemyRuntimeId;
    private final int damage;
    private float x;
    private float y;
    private final float speed;

    // ── Trail-direction tracking (snapshot of last frame's position) ──
    /** Position one frame ago — used by the renderer to compute the trail vector. */
    private float prevX;
    /** Position one frame ago — used by the renderer to compute the trail vector. */
    private float prevY;

    public ActiveProjectile(
        String runtimeId,
        String targetEnemyRuntimeId,
        int damage,
        float x,
        float y,
        float speed
    ) {
        this.runtimeId = runtimeId;
        this.targetEnemyRuntimeId = targetEnemyRuntimeId;
        this.damage = damage;
        this.x = x;
        this.y = y;
        this.speed = speed;
        // First frame: trail collapses to a point (no streak before the spawn).
        this.prevX = x;
        this.prevY = y;
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public String getTargetEnemyRuntimeId() {
        return targetEnemyRuntimeId;
    }

    public int getDamage() {
        return damage;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getSpeed() {
        return speed;
    }

    public float getPrevX() {
        return prevX;
    }

    public float getPrevY() {
        return prevY;
    }

    public void setPosition(float x, float y) {
        // Snapshot the previous frame's position so the renderer can draw a
        // velocity-aligned trail without storing a history buffer.
        this.prevX = this.x;
        this.prevY = this.y;
        this.x = x;
        this.y = y;
    }
}
