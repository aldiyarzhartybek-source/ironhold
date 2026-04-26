package com.ironhold.game;

import com.ironhold.game.model.ActiveEnemy;
import com.ironhold.game.model.BuildSlot;
import com.ironhold.game.model.PlacedTower;
import com.ironhold.level.RuntimeLevelState;

import java.util.List;
import java.util.Objects;

/**
 * Immutable snapshot of gameplay runtime data for UI/screens.
 */
public final class GameRuntimeView {

    private final RuntimeLevelState levelState;
    private final List<BuildSlot> buildSlots;
    private final List<PlacedTower> placedTowers;
    private final List<ActiveEnemy> activeEnemies;
    private final int gold;
    private final GameFacade.BuildPlacementResult lastBuildPlacementResult;
    private final int totalKilledEnemies;
    private final int lastAwardedGold;
    private final int enemySpawnedEvents;
    private final int enemyKilledEvents;
    private final int towerBuiltEvents;
    private final int waveStartedEvents;
    private final int waveCompletedEvents;

    public GameRuntimeView(
        RuntimeLevelState levelState,
        List<BuildSlot> buildSlots,
        List<PlacedTower> placedTowers,
        List<ActiveEnemy> activeEnemies,
        int gold,
        GameFacade.BuildPlacementResult lastBuildPlacementResult,
        int totalKilledEnemies,
        int lastAwardedGold,
        int enemySpawnedEvents,
        int enemyKilledEvents,
        int towerBuiltEvents,
        int waveStartedEvents,
        int waveCompletedEvents
    ) {
        this.levelState = Objects.requireNonNull(levelState, "levelState");
        this.buildSlots = List.copyOf(Objects.requireNonNull(buildSlots, "buildSlots"));
        this.placedTowers = List.copyOf(Objects.requireNonNull(placedTowers, "placedTowers"));
        this.activeEnemies = List.copyOf(Objects.requireNonNull(activeEnemies, "activeEnemies"));
        this.gold = gold;
        this.lastBuildPlacementResult = Objects.requireNonNull(lastBuildPlacementResult, "lastBuildPlacementResult");
        this.totalKilledEnemies = totalKilledEnemies;
        this.lastAwardedGold = lastAwardedGold;
        this.enemySpawnedEvents = enemySpawnedEvents;
        this.enemyKilledEvents = enemyKilledEvents;
        this.towerBuiltEvents = towerBuiltEvents;
        this.waveStartedEvents = waveStartedEvents;
        this.waveCompletedEvents = waveCompletedEvents;
    }

    public RuntimeLevelState getLevelState() {
        return levelState;
    }

    public List<BuildSlot> getBuildSlots() {
        return buildSlots;
    }

    public List<PlacedTower> getPlacedTowers() {
        return placedTowers;
    }

    public List<ActiveEnemy> getActiveEnemies() {
        return activeEnemies;
    }

    public int getGold() {
        return gold;
    }

    public GameFacade.BuildPlacementResult getLastBuildPlacementResult() {
        return lastBuildPlacementResult;
    }

    public int getTotalKilledEnemies() {
        return totalKilledEnemies;
    }

    public int getLastAwardedGold() {
        return lastAwardedGold;
    }

    public int getEnemySpawnedEvents() {
        return enemySpawnedEvents;
    }

    public int getEnemyKilledEvents() {
        return enemyKilledEvents;
    }

    public int getTowerBuiltEvents() {
        return towerBuiltEvents;
    }

    public int getWaveStartedEvents() {
        return waveStartedEvents;
    }

    public int getWaveCompletedEvents() {
        return waveCompletedEvents;
    }
}
