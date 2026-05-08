package com.ironhold.game;

import com.badlogic.gdx.math.Vector2;
import com.ironhold.game.model.ActiveEnemy;
import com.ironhold.game.model.ActiveProjectile;
import com.ironhold.game.model.BuildSlot;
import com.ironhold.game.model.HitEffect;
import com.ironhold.game.model.PlacedTower;
import com.ironhold.level.RuntimeLevelState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Mutable runtime state shared by gameplay subsystems.
 */
public final class GameRuntimeState {
    private final RuntimeLevelState runtimeLevelState;
    private final List<BuildSlot> buildSlots;
    private final List<ActiveEnemy> activeEnemies;
    private final List<PlacedTower> placedTowers;
    private final List<ActiveProjectile> activeProjectiles;
    private final List<HitEffect> hitEffects;
    private final List<Vector2> enemyPath;
    private String selectedTowerId;
    private int nextEnemyInstanceId;
    private int nextProjectileInstanceId;
    private int lastAwardedGold;
    private int totalKilledEnemies;
    private int totalGoldSpent;
    private int totalGoldEarned;

    public GameRuntimeState(
        RuntimeLevelState runtimeLevelState,
        List<BuildSlot> initialBuildSlots,
        List<Vector2> enemyPath,
        String selectedTowerId
    ) {
        this.runtimeLevelState = Objects.requireNonNull(runtimeLevelState, "runtimeLevelState");
        this.buildSlots = new ArrayList<>(Objects.requireNonNull(initialBuildSlots, "initialBuildSlots"));
        this.activeEnemies = new ArrayList<>();
        this.placedTowers = new ArrayList<>();
        this.activeProjectiles = new ArrayList<>();
        this.hitEffects = new ArrayList<>();
        this.enemyPath = List.copyOf(Objects.requireNonNull(enemyPath, "enemyPath"));
        this.selectedTowerId = selectedTowerId;
        this.nextEnemyInstanceId = 1;
        this.nextProjectileInstanceId = 1;
        this.lastAwardedGold = 0;
        this.totalKilledEnemies = 0;
        this.totalGoldSpent = 0;
        this.totalGoldEarned = 0;
    }

    public void resetForNewLevel(List<BuildSlot> initialBuildSlots) {
        activeEnemies.clear();
        placedTowers.clear();
        activeProjectiles.clear();
        hitEffects.clear();
        buildSlots.clear();
        buildSlots.addAll(initialBuildSlots);
        nextEnemyInstanceId = 1;
        nextProjectileInstanceId = 1;
        lastAwardedGold = 0;
        totalKilledEnemies = 0;
        totalGoldSpent = 0;
        totalGoldEarned = 0;
    }

    public RuntimeLevelState getRuntimeLevelState() {
        return runtimeLevelState;
    }

    public List<BuildSlot> getBuildSlots() {
        return buildSlots;
    }

    public List<ActiveEnemy> getActiveEnemies() {
        return activeEnemies;
    }

    public List<PlacedTower> getPlacedTowers() {
        return placedTowers;
    }

    public List<ActiveProjectile> getActiveProjectiles() {
        return activeProjectiles;
    }

    public List<HitEffect> getHitEffects() {
        return hitEffects;
    }

    public List<Vector2> getEnemyPath() {
        return enemyPath;
    }

    public String getSelectedTowerId() {
        return selectedTowerId;
    }

    public void setSelectedTowerId(String selectedTowerId) {
        this.selectedTowerId = selectedTowerId;
    }

    public int getNextEnemyInstanceId() {
        return nextEnemyInstanceId++;
    }

    public int getNextProjectileInstanceId() {
        return nextProjectileInstanceId++;
    }

    public int getLastAwardedGold() {
        return lastAwardedGold;
    }

    public void setLastAwardedGold(int lastAwardedGold) {
        this.lastAwardedGold = lastAwardedGold;
    }

    public int getTotalKilledEnemies() {
        return totalKilledEnemies;
    }

    public void incrementTotalKilledEnemies() {
        this.totalKilledEnemies++;
    }

    public int getTotalGoldSpent() {
        return totalGoldSpent;
    }

    public void addGoldSpent(int amount) {
        if (amount <= 0) {
            return;
        }
        totalGoldSpent += amount;
    }

    public int getTotalGoldEarned() {
        return totalGoldEarned;
    }

    public void addGoldEarned(int amount) {
        if (amount <= 0) {
            return;
        }
        totalGoldEarned += amount;
    }
}
