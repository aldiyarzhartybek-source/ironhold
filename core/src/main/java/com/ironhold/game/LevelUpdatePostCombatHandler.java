package com.ironhold.game;

import com.ironhold.level.RuntimeLevelState;

/**
 * Facade-specific steps after combat for one level frame (Rush chain, win, timer).
 */
@FunctionalInterface
public interface LevelUpdatePostCombatHandler {

    void afterCombat(GameRuntimeState state, RuntimeLevelState levelState);
}
