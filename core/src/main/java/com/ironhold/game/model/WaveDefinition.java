package com.ironhold.game.model;

/**
 * Runtime wave definition skeleton.
 */
public final class WaveDefinition {

    private final String enemyId;
    private final int count;
    private final float spawnIntervalSec;
    private final boolean bossWave;

    public WaveDefinition(String enemyId, int count, float spawnIntervalSec) {
        this(enemyId, count, spawnIntervalSec, false);
    }

    public WaveDefinition(String enemyId, int count, float spawnIntervalSec, boolean bossWave) {
        this.enemyId = enemyId;
        this.count = count;
        this.spawnIntervalSec = spawnIntervalSec;
        this.bossWave = bossWave;
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

    public boolean isBossWave() {
        return bossWave;
    }
}
