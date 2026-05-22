package com.ironhold.game.model;

import com.badlogic.gdx.math.Vector2;

import java.util.List;
import java.util.Objects;

/**
 * Resolved level session: layout, starting gold, and wave schedule.
 */
public final class LevelDefinition {

    private final int levelNumber;
    private final int startingGold;
    private final List<WaveDefinition> waves;
    private final List<Vector2> enemyPath;
    private final List<BuildSlot> buildSlots;

    public LevelDefinition(
        int levelNumber,
        int startingGold,
        List<WaveDefinition> waves,
        List<Vector2> enemyPath,
        List<BuildSlot> buildSlots
    ) {
        if (levelNumber < 1) {
            throw new IllegalArgumentException("levelNumber must be >= 1");
        }
        this.levelNumber = levelNumber;
        this.startingGold = startingGold;
        this.waves = List.copyOf(Objects.requireNonNull(waves, "waves"));
        this.enemyPath = List.copyOf(Objects.requireNonNull(enemyPath, "enemyPath"));
        this.buildSlots = List.copyOf(Objects.requireNonNull(buildSlots, "buildSlots"));
    }

    public int getStartingGold() {
        return startingGold;
    }

    public List<WaveDefinition> getWaves() {
        return waves;
    }

    public List<Vector2> getEnemyPath() {
        return enemyPath;
    }

    public List<BuildSlot> getBuildSlots() {
        return buildSlots;
    }
}
