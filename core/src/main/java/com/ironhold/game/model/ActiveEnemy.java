package com.ironhold.game.model;

/**
 * Enemy instance currently spawned on a level.
 */
public final class ActiveEnemy {

    private final String runtimeId;
    private final String enemyId;
    private final int maxHp;
    private final int currentHp;
    private final float speed;
    private final int reward;
    private final float x;
    private final float y;

    public ActiveEnemy(
        String runtimeId,
        String enemyId,
        int maxHp,
        int currentHp,
        float speed,
        int reward,
        float x,
        float y
    ) {
        this.runtimeId = runtimeId;
        this.enemyId = enemyId;
        this.maxHp = maxHp;
        this.currentHp = currentHp;
        this.speed = speed;
        this.reward = reward;
        this.x = x;
        this.y = y;
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public String getEnemyId() {
        return enemyId;
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

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
