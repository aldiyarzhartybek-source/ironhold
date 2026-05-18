package com.ironhold.game.model;

import java.util.List;
import java.util.Objects;

/**
 * Resolved level session: map, starting gold, and wave schedule.
 */
public final class LevelDefinition {

    private final int levelNumber;
    private final String levelId;
    private final String mapPath;
    private final int startingGold;
    private final List<WaveDefinition> waves;

    public LevelDefinition(
        int levelNumber,
        String levelId,
        String mapPath,
        int startingGold,
        List<WaveDefinition> waves
    ) {
        if (levelNumber < 1) {
            throw new IllegalArgumentException("levelNumber must be >= 1");
        }
        this.levelNumber = levelNumber;
        this.levelId = Objects.requireNonNull(levelId, "levelId");
        this.mapPath = Objects.requireNonNull(mapPath, "mapPath");
        this.startingGold = startingGold;
        this.waves = List.copyOf(Objects.requireNonNull(waves, "waves"));
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public String getLevelId() {
        return levelId;
    }

    public String getMapPath() {
        return mapPath;
    }

    public int getStartingGold() {
        return startingGold;
    }

    public List<WaveDefinition> getWaves() {
        return waves;
    }
}
