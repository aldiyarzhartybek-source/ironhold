package com.ironhold.game;

import com.ironhold.game.model.Tower;

import java.util.List;
import java.util.Objects;

/**
 * Assembles immutable runtime view snapshots for UI/screens.
 */
public final class GameRuntimeViewAssembler {
    private final GameplayEventTracker eventTracker;
    private final List<Tower> availableTowers;

    public GameRuntimeViewAssembler(GameplayEventTracker eventTracker, List<Tower> availableTowers) {
        this.eventTracker = Objects.requireNonNull(eventTracker, "eventTracker");
        this.availableTowers = List.copyOf(Objects.requireNonNull(availableTowers, "availableTowers"));
    }

    public GameRuntimeView assemble(
        GameRuntimeState state,
        int gold,
        GameFacade.BuildPlacementResult lastBuildPlacementResult,
        GameMode gameMode,
        float timeScale
    ) {
        return new GameRuntimeView(
            state.getRuntimeLevelState(),
            state.getBuildSlots(),
            state.getPlacedTowers(),
            state.getActiveEnemies(),
            state.getActiveProjectiles(),
            state.getHitEffects(),
            gold,
            lastBuildPlacementResult,
            state.getTotalKilledEnemies(),
            state.getLastAwardedGold(),
            state.getTotalGoldSpent(),
            state.getTotalGoldEarned(),
            state.getRuntimeLevelState().getCurrentWaveNumber(),
            eventTracker.getEnemySpawnedEvents(),
            eventTracker.getEnemyKilledEvents(),
            eventTracker.getTowerBuiltEvents(),
            eventTracker.getWaveStartedEvents(),
            eventTracker.getWaveCompletedEvents(),
            state.getEnemyPath(),
            availableTowers,
            state.getSelectedTowerId(),
            gameMode,
            state.getSessionStats().getElapsedSec(),
            state.getSessionStats().getElapsedFormatted(),
            timeScale
        );
    }
}
