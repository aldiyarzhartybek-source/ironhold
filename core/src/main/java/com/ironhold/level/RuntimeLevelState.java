package com.ironhold.level;

import com.ironhold.game.model.WaveDefinition;

import java.util.List;
import java.util.Objects;

public final class RuntimeLevelState {

    private final List<WaveDefinition> waves;
    private LevelStatus status;
    private int currentWaveIndex;
    private int spawnedInCurrentWave;
    private int totalSpawnedEnemies;
    private float spawnTimerSec;
    private String lastSpawnedEnemyId;

    public RuntimeLevelState(List<WaveDefinition> waves) {
        this.waves = List.copyOf(Objects.requireNonNull(waves, "waves"));
        this.status = LevelStatus.IDLE;
        this.currentWaveIndex = 0;
        this.spawnedInCurrentWave = 0;
        this.totalSpawnedEnemies = 0;
        this.spawnTimerSec = 0f;
        this.lastSpawnedEnemyId = "";
    }

    public void start() {
        if (status != LevelStatus.IDLE) {
            return;
        }
        if (waves.isEmpty()) {
            status = LevelStatus.COMPLETED;
            return;
        }
        status = LevelStatus.RUNNING;
        currentWaveIndex = 0;
        spawnedInCurrentWave = 0;
        totalSpawnedEnemies = 0;
        spawnTimerSec = 0f;
        lastSpawnedEnemyId = "";
    }

    public void update(float deltaSec) {
        if (status != LevelStatus.RUNNING) {
            return;
        }
        if (!hasCurrentWave()) {
            status = LevelStatus.COMPLETED;
            return;
        }

        WaveDefinition currentWave = waves.get(currentWaveIndex);
        spawnTimerSec += Math.max(0f, deltaSec);

        while (spawnedInCurrentWave < currentWave.getCount()
            && spawnTimerSec >= currentWave.getSpawnIntervalSec()) {
            spawnTimerSec -= currentWave.getSpawnIntervalSec();
            spawnedInCurrentWave++;
            totalSpawnedEnemies++;
            lastSpawnedEnemyId = currentWave.getEnemyId();
        }

        if (spawnedInCurrentWave >= currentWave.getCount()) {
            currentWaveIndex++;
            spawnedInCurrentWave = 0;
            spawnTimerSec = 0f;
            if (!hasCurrentWave()) {
                status = LevelStatus.COMPLETED;
            }
        }
    }

    public LevelStatus getStatus() {
        return status;
    }

    public int getCurrentWaveNumber() {
        if (waves.isEmpty()) {
            return 0;
        }
        return Math.min(currentWaveIndex + 1, waves.size());
    }

    public int getTotalWaves() {
        return waves.size();
    }

    public int getSpawnedInCurrentWave() {
        return spawnedInCurrentWave;
    }

    public int getTotalSpawnedEnemies() {
        return totalSpawnedEnemies;
    }

    public float getSpawnTimerSec() {
        return spawnTimerSec;
    }

    public String getLastSpawnedEnemyId() {
        return lastSpawnedEnemyId;
    }

    private boolean hasCurrentWave() {
        return currentWaveIndex < waves.size();
    }
}
