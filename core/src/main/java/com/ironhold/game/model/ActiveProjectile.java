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

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
