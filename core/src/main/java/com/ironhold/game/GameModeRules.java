package com.ironhold.game;

import java.util.Objects;

/**
 * Per-mode gameplay rules for a level session.
 */
public final class GameModeRules {

    private static final int CLASSIC_AND_RUSH_LIVES = 100;
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
}
