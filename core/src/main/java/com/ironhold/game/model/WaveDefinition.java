package com.ironhold.game.model;

/**
 * Runtime wave definition skeleton.
 */
public final class WaveDefinition {

    private final String enemyId;
    private final int count;
    private final float spawnIntervalSec;

    public WaveDefinition(String enemyId, int count, float spawnIntervalSec) {
        this.enemyId = enemyId;
        this.count = count;
        this.spawnIntervalSec = spawnIntervalSec;
    }

    public String getEnemyId() {
        return enemyId;
    }

    public int getCount() {
        return count;
    }

    public float getSpawnIntervalSec() {
        return spawnIntervalSec;
    }
}
