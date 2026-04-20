package com.ironhold.game.model;

/**
 * Runtime enemy model (skeleton for MVP mechanics).
 */
public final class Enemy {

    private final String id;
    private final int maxHp;
    private final int currentHp;
    private final float speed;
    private final int reward;

    public Enemy(String id, int maxHp, int currentHp, float speed, int reward) {
        this.id = id;
        this.maxHp = maxHp;
        this.currentHp = currentHp;
        this.speed = speed;
        this.reward = reward;
    }

    public String getId() {
        return id;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public float getSpeed() {
        return speed;
    }

    public int getReward() {
        return reward;
    }
}
