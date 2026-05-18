package com.ironhold.save;

import java.util.Objects;

/**
 * Application service for campaign level unlocks; delegates persistence to {@link ProgressRepository}.
 */
public final class ProgressService {

    public static final int MAX_LEVELS = 5;

    private final ProgressRepository repository;
    private PlayerProgress progress;

    public ProgressService(ProgressRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.progress = PlayerProgress.defaultProgress();
    }

    public void load() {
        progress = repository.load();
    }

    public PlayerProgress getProgress() {
        return progress;
    }

    public int getHighestUnlockedLevel() {
        return progress.getHighestUnlockedLevel();
    }

    public boolean isLevelUnlocked(int levelNumber) {
        if (levelNumber < 1 || levelNumber > MAX_LEVELS) {
            return false;
        }
        return progress.isLevelUnlocked(levelNumber);
    }

    public boolean isLevelCompleted(int levelNumber) {
        return progress.getCompletedLevels().contains(levelNumber);
    }

    /**
     * Records a victorious run on {@code levelNumber} and persists immediately.
     */
    public void recordLevelCompleted(int levelNumber) {
        if (levelNumber < 1 || levelNumber > MAX_LEVELS) {
            return;
        }
        if (progress.getCompletedLevels().contains(levelNumber)
            && progress.getHighestUnlockedLevel() >= Math.min(levelNumber + 1, MAX_LEVELS)) {
            return;
        }
        progress = progress.withLevelCompleted(levelNumber);
        repository.save(progress);
    }
}
