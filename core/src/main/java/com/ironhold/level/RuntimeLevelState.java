package com.ironhold.level;

import com.ironhold.game.model.WaveDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RuntimeLevelState {

    private final List<WaveDefinition> waves;
    private LevelStatus status;
    private WavePhase wavePhase;
    private int currentWaveIndex;
    private int spawnedInCurrentWave;
    private int totalSpawnedEnemies;
    private int escapedEnemies;
    private int baseLives;
    private float spawnTimerSec;
    private String lastSpawnedEnemyId;
    private boolean waveSpawnExhausted;
    private final List<String> pendingSpawnEnemyIds;
    private final List<Integer> pendingWaveStartedNumbers;
    private final List<Integer> pendingWaveCompletedNumbers;
    private boolean allWavesSpawned;

    public RuntimeLevelState(List<WaveDefinition> waves) {
        this.waves = List.copyOf(Objects.requireNonNull(waves, "waves"));
        this.status = LevelStatus.IDLE;
        this.wavePhase = WavePhase.BETWEEN_WAVES;
        this.currentWaveIndex = 0;
        this.spawnedInCurrentWave = 0;
        this.totalSpawnedEnemies = 0;
        this.escapedEnemies = 0;
        this.baseLives = 20;
        this.spawnTimerSec = 0f;
        this.lastSpawnedEnemyId = "";
        this.waveSpawnExhausted = false;
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
        wavePhase = WavePhase.BETWEEN_WAVES;
    }

    public boolean canStartNextWave() {
        return status == LevelStatus.RUNNING
            && wavePhase == WavePhase.BETWEEN_WAVES
            && hasCurrentWave();
    }

    public boolean startNextWave() {
        if (!canStartNextWave()) {
            return false;
        }
        wavePhase = WavePhase.WAVE_ACTIVE;
        spawnedInCurrentWave = 0;
        spawnTimerSec = 0f;
        waveSpawnExhausted = false;
        pendingWaveStartedNumbers.add(currentWaveIndex + 1);
        return true;
    }

    public void update(float deltaSec) {
        if (status != LevelStatus.RUNNING || wavePhase != WavePhase.WAVE_ACTIVE) {
            return;
        }
        if (!hasCurrentWave()) {
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
            waveSpawnExhausted = true;
        }
    }

    /**
     * Completes the active wave after all spawns are done and the field is clear.
     */
    public void tryCompleteActiveWave(boolean fieldIsEmpty) {
        if (status != LevelStatus.RUNNING
            || wavePhase != WavePhase.WAVE_ACTIVE
            || !waveSpawnExhausted
            || !fieldIsEmpty) {
            return;
        }

        int completedWaveNumber = currentWaveIndex + 1;
        pendingWaveCompletedNumbers.add(completedWaveNumber);

        currentWaveIndex++;
        spawnedInCurrentWave = 0;
        spawnTimerSec = 0f;
        waveSpawnExhausted = false;
        wavePhase = WavePhase.BETWEEN_WAVES;

        if (!hasCurrentWave()) {
            allWavesSpawned = true;
        }
    }

    public LevelStatus getStatus() {
        return status;
    }

    public WavePhase getWavePhase() {
        return wavePhase;
    }

    /**
     * Number of the wave in progress, or the next wave to start while between waves.
     */
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

    public boolean isWaveSpawnExhausted() {
        return waveSpawnExhausted;
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
        wavePhase = WavePhase.BETWEEN_WAVES;
        currentWaveIndex = 0;
        spawnedInCurrentWave = 0;
        totalSpawnedEnemies = 0;
        escapedEnemies = 0;
        baseLives = 20;
        spawnTimerSec = 0f;
        lastSpawnedEnemyId = "";
        waveSpawnExhausted = false;
        pendingSpawnEnemyIds.clear();
        pendingWaveStartedNumbers.clear();
        pendingWaveCompletedNumbers.clear();
        allWavesSpawned = false;
    }

    private boolean hasCurrentWave() {
        return currentWaveIndex < waves.size();
    }
}
