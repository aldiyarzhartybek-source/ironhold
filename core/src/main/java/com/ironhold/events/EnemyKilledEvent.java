package com.ironhold.events;

public final class EnemyKilledEvent implements GameEvent {

    private final String runtimeEnemyId;
    private final String enemyId;
    private final int reward;
    private final float worldX;
    private final float worldY;

    public EnemyKilledEvent(String runtimeEnemyId, String enemyId, int reward, float worldX, float worldY) {
        this.runtimeEnemyId = runtimeEnemyId;
        this.enemyId = enemyId;
        this.reward = reward;
        this.worldX = worldX;
        this.worldY = worldY;
    }

    public String getRuntimeEnemyId() {
        return runtimeEnemyId;
    }

    public String getEnemyId() {
        return enemyId;
    }

    public int getReward() {
        return reward;
    }

    public float getWorldX() {
        return worldX;
    }

    public float getWorldY() {
        return worldY;
    }
}
