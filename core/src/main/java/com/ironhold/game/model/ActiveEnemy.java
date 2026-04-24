package com.ironhold.game.model;

/**
 * Enemy instance currently spawned on a level.
 */
public final class ActiveEnemy {

    private final String runtimeId;
    private final String enemyId;
    private final int maxHp;
    private int currentHp;
    private final float speed;
    private final int reward;
    private float x;
    private float y;
    private int targetWaypointIndex;

    public ActiveEnemy(
        String runtimeId,
        String enemyId,
        int maxHp,
        int currentHp,
        float speed,
        int reward,
        float x,
        float y,
        int targetWaypointIndex
    ) {
        this.runtimeId = runtimeId;
        this.enemyId = enemyId;
        this.maxHp = maxHp;
        this.currentHp = currentHp;
        this.speed = speed;
        this.reward = reward;
        this.x = x;
        this.y = y;
        this.targetWaypointIndex = targetWaypointIndex;
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

    public int getTargetWaypointIndex() {
        return targetWaypointIndex;
    }

    public void setCurrentHp(int currentHp) {
        this.currentHp = currentHp;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setTargetWaypointIndex(int targetWaypointIndex) {
        this.targetWaypointIndex = targetWaypointIndex;
    }
}
