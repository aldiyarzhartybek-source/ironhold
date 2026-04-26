package com.ironhold.events;

public final class EnemyKilledEvent implements GameEvent {

    private final String runtimeEnemyId;
    private final String enemyId;
    private final int reward;

    public EnemyKilledEvent(String runtimeEnemyId, String enemyId, int reward) {
        this.runtimeEnemyId = runtimeEnemyId;
        this.enemyId = enemyId;
        this.reward = reward;
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
}
