package com.ironhold.level;

import com.ironhold.game.model.WaveDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RuntimeLevelState {

    private final List<WaveDefinition> waves;
    private LevelStatus status;
    private int currentWaveIndex;
    private int spawnedInCurrentWave;
    private int totalSpawnedEnemies;
    private int escapedEnemies;
    private int baseLives;
    private float spawnTimerSec;
    private String lastSpawnedEnemyId;
    private final List<String> pendingSpawnEnemyIds;
    private final List<Integer> pendingWaveStartedNumbers;
    private final List<Integer> pendingWaveCompletedNumbers;
    private boolean allWavesSpawned;

    public RuntimeLevelState(List<WaveDefinition> waves) {
        this.waves = List.copyOf(Objects.requireNonNull(waves, "waves"));
        this.status = LevelStatus.IDLE;
        this.currentWaveIndex = 0;
        this.spawnedInCurrentWave = 0;
        this.totalSpawnedEnemies = 0;
        this.escapedEnemies = 0;
        this.baseLives = 20;
        this.spawnTimerSec = 0f;
        this.lastSpawnedEnemyId = "";
        this.pendingSpawnEnemyIds = new ArrayList<>();
        this.pendingWaveStartedNumbers = new ArrayList<>();
        this.pendingWaveCompletedNumbers = new ArrayList<>();
        this.allWavesSpawned = false;
    }

    public void start() {
        resetForNewRun();
        if (waves.isEmpty()) {
            status = LevelStatus.COMPLETED;
            allWavesSpawned = true;
            return;
        }
        status = LevelStatus.RUNNING;
        pendingWaveStartedNumbers.add(1);
    }

    public void update(float deltaSec) {
        if (status != LevelStatus.RUNNING) {
            return;
        }
        if (!hasCurrentWave()) {
            allWavesSpawned = true;
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
            pendingSpawnEnemyIds.add(currentWave.getEnemyId());
        }

        if (spawnedInCurrentWave >= currentWave.getCount()) {
            int completedWaveNumber = currentWaveIndex + 1;
            currentWaveIndex++;
            spawnedInCurrentWave = 0;
            spawnTimerSec = 0f;
            pendingWaveCompletedNumbers.add(completedWaveNumber);
            if (hasCurrentWave()) {
                pendingWaveStartedNumbers.add(currentWaveIndex + 1);
            }
            if (!hasCurrentWave()) {
                allWavesSpawned = true;
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

    public int getEscapedEnemies() {
        return escapedEnemies;
    }

    public int getBaseLives() {
        return baseLives;
    }

    public float getSpawnTimerSec() {
        return spawnTimerSec;
    }

    public String getLastSpawnedEnemyId() {
        return lastSpawnedEnemyId;
    }

    public List<String> consumePendingSpawnEnemyIds() {
        if (pendingSpawnEnemyIds.isEmpty()) {
            return List.of();
        }
        List<String> spawned = List.copyOf(pendingSpawnEnemyIds);
        pendingSpawnEnemyIds.clear();
        return spawned;
    }

    public List<Integer> consumePendingWaveStartedNumbers() {
        if (pendingWaveStartedNumbers.isEmpty()) {
            return List.of();
        }
        List<Integer> started = List.copyOf(pendingWaveStartedNumbers);
        pendingWaveStartedNumbers.clear();
        return started;
    }

    public List<Integer> consumePendingWaveCompletedNumbers() {
        if (pendingWaveCompletedNumbers.isEmpty()) {
            return List.of();
        }
        List<Integer> completed = List.copyOf(pendingWaveCompletedNumbers);
        pendingWaveCompletedNumbers.clear();
        return completed;
    }

    public void onEnemyEscaped() {
        if (status != LevelStatus.RUNNING) {
            return;
        }
        escapedEnemies++;
        baseLives = Math.max(0, baseLives - 1);
        if (baseLives <= 0) {
            status = LevelStatus.FAILED;
        }
    }

    public boolean areAllWavesSpawned() {
        return allWavesSpawned;
    }

    public void markCompletedIfRunning() {
        if (status == LevelStatus.RUNNING) {
            status = LevelStatus.COMPLETED;
        }
    }

    private void resetForNewRun() {
        status = LevelStatus.IDLE;
        currentWaveIndex = 0;
        spawnedInCurrentWave = 0;
        totalSpawnedEnemies = 0;
        escapedEnemies = 0;
        baseLives = 20;
        spawnTimerSec = 0f;
        lastSpawnedEnemyId = "";
        pendingSpawnEnemyIds.clear();
        pendingWaveStartedNumbers.clear();
        pendingWaveCompletedNumbers.clear();
        allWavesSpawned = false;
    }

    private boolean hasCurrentWave() {
        return currentWaveIndex < waves.size();
    }
}
