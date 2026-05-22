package com.ironhold.game.model;

import java.util.Objects;

/** One enemy line inside a mixed wave. */
public final class WaveSpawnGroup {

    /** Added to configured interval so waves spawn slightly slower at the gate. */
    private static final float SPAWN_INTERVAL_BONUS_SEC = 0.2f;
    private static final float MIN_SPAWN_INTERVAL_SEC = 0.35f;

    private final String enemyId;
    private final int count;
    private final float spawnIntervalSec;

    public WaveSpawnGroup(String enemyId, int count, float spawnIntervalSec) {
        this.enemyId = Objects.requireNonNull(enemyId, "enemyId");
        this.count = Math.max(0, count);
        this.spawnIntervalSec = Math.max(
            MIN_SPAWN_INTERVAL_SEC,
            spawnIntervalSec + SPAWN_INTERVAL_BONUS_SEC
        );
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
