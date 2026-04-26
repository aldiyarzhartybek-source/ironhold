package com.ironhold.events;

public final class EnemySpawnedEvent implements GameEvent {

    private final String runtimeEnemyId;
    private final String enemyId;
    private final int waveNumber;

    public EnemySpawnedEvent(String runtimeEnemyId, String enemyId, int waveNumber) {
        this.runtimeEnemyId = runtimeEnemyId;
        this.enemyId = enemyId;
        this.waveNumber = waveNumber;
    }

    public String getRuntimeEnemyId() {
        return runtimeEnemyId;
    }

    public String getEnemyId() {
        return enemyId;
    }

    public int getWaveNumber() {
        return waveNumber;
    }
}
