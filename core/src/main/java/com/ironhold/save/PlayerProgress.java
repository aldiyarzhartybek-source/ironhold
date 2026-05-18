package com.ironhold.save;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Immutable campaign unlock state (levels 1..{@link ProgressService#MAX_LEVELS}).
 */
public final class PlayerProgress {

    public static final int SCHEMA_VERSION = 1;

    private final int highestUnlockedLevel;
    private final List<Integer> completedLevels;

    public PlayerProgress(int highestUnlockedLevel, List<Integer> completedLevels) {
        this.highestUnlockedLevel = clampUnlocked(highestUnlockedLevel);
        SortedSet<Integer> normalized = new TreeSet<>();
        if (completedLevels != null) {
            for (Integer level : completedLevels) {
                if (level != null) {
                    addLevel(normalized, level);
                }
            }
        }
        this.completedLevels = List.copyOf(normalized);
    }

    public static PlayerProgress defaultProgress() {
        return new PlayerProgress(1, List.of());
    }

    public int getHighestUnlockedLevel() {
        return highestUnlockedLevel;
    }

    public List<Integer> getCompletedLevels() {
        return completedLevels;
    }

    public boolean isLevelUnlocked(int levelNumber) {
        if (levelNumber <= 1) {
            return levelNumber >= 1;
        }
        return levelNumber <= highestUnlockedLevel;
    }

    public PlayerProgress withLevelCompleted(int levelNumber) {
        Objects.checkIndex(levelNumber - 1, ProgressService.MAX_LEVELS);
        SortedSet<Integer> nextCompleted = new TreeSet<>(completedLevels);
        nextCompleted.add(levelNumber);
        int nextUnlocked = Math.max(highestUnlockedLevel, Math.min(levelNumber + 1, ProgressService.MAX_LEVELS));
        return new PlayerProgress(nextUnlocked, List.copyOf(nextCompleted));
    }

    ProgressSaveDto toDto() {
        ProgressSaveDto dto = new ProgressSaveDto();
        dto.schemaVersion = SCHEMA_VERSION;
        dto.highestUnlockedLevel = highestUnlockedLevel;
        dto.completedLevels = new ArrayList<>(completedLevels);
        return dto;
    }

    static PlayerProgress fromDto(ProgressSaveDto dto) {
        if (dto == null) {
            return defaultProgress();
        }
        return new PlayerProgress(dto.highestUnlockedLevel, dto.completedLevels);
    }

    private static int clampUnlocked(int value) {
        if (value < 1) {
            return 1;
        }
        return Math.min(value, ProgressService.MAX_LEVELS);
    }

    private static void addLevel(SortedSet<Integer> target, int levelNumber) {
        if (levelNumber >= 1 && levelNumber <= ProgressService.MAX_LEVELS) {
            target.add(levelNumber);
        }
    }
}
