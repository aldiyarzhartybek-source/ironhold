package com.ironhold.game;

import java.util.Objects;

/**
 * Per-mode gameplay rules for a level session.
 */
public final class GameModeRules {

    private static final int CLASSIC_AND_RUSH_LIVES = 10;
    private static final int ONE_LIFE_LIVES = 1;

    private GameModeRules() {
    }

    public static int startingLives(GameMode mode) {
        Objects.requireNonNull(mode, "mode");
        if (mode == GameMode.ONE_LIFE) {
            return ONE_LIFE_LIVES;
        }
        return CLASSIC_AND_RUSH_LIVES;
    }

    /** Base HP damage when an enemy reaches the end of the path. */
    public static int leakDamageForEnemy(String enemyId) {
        if ("runner".equals(enemyId)) {
            return 1;
        }
        if ("grunt".equals(enemyId)) {
            return 3;
        }
        if ("elite".equals(enemyId)) {
            return 5;
        }
        if ("boss".equals(enemyId)) {
            return 10;
        }
        return 1;
    }
}
