package com.ironhold.game;

import com.badlogic.gdx.math.Vector2;
import com.ironhold.game.model.ActiveEnemy;
import com.ironhold.game.model.ActiveProjectile;
import com.ironhold.game.model.BuildSlot;
import com.ironhold.game.model.HitEffect;
import com.ironhold.game.model.LightningEffect;
import com.ironhold.game.model.MortarExplosionEffect;
import com.ironhold.game.model.PlacedTower;
import com.ironhold.game.model.Tower;
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
    private final List<ActiveProjectile> activeProjectiles;
    private final List<HitEffect> hitEffects;
    private final List<LightningEffect> lightningEffects;
    private final List<MortarExplosionEffect> mortarExplosions;
    private final int gold;
    private final GameFacade.BuildPlacementResult lastBuildPlacementResult;
    private final int totalKilledEnemies;
    private final int lastAwardedGold;
    private final int totalGoldSpent;
    private final int totalGoldEarned;
    private final int wavesReached;
    private final int enemySpawnedEvents;
    private final int enemyKilledEvents;
    private final int towerBuiltEvents;
    private final int waveStartedEvents;
    private final int waveCompletedEvents;
    private final List<Vector2> enemyPath;
    private final List<Tower> availableTowers;
    private final String selectedTowerId;
    private final GameMode gameMode;
    private final float elapsedLevelTimeSec;
    private final String elapsedLevelTimeFormatted;
    private final float timeScale;

    public GameRuntimeView(
        RuntimeLevelState levelState,
        List<BuildSlot> buildSlots,
        List<PlacedTower> placedTowers,
        List<ActiveEnemy> activeEnemies,
        List<ActiveProjectile> activeProjectiles,
        List<HitEffect> hitEffects,
        List<LightningEffect> lightningEffects,
        List<MortarExplosionEffect> mortarExplosions,
        int gold,
        GameFacade.BuildPlacementResult lastBuildPlacementResult,
        int totalKilledEnemies,
        int lastAwardedGold,
        int totalGoldSpent,
        int totalGoldEarned,
        int wavesReached,
        int enemySpawnedEvents,
        int enemyKilledEvents,
        int towerBuiltEvents,
        int waveStartedEvents,
        int waveCompletedEvents,
        List<Vector2> enemyPath,
        List<Tower> availableTowers,
        String selectedTowerId,
        GameMode gameMode,
        float elapsedLevelTimeSec,
        String elapsedLevelTimeFormatted,
        float timeScale
    ) {
        this.levelState = Objects.requireNonNull(levelState, "levelState");
        this.buildSlots = List.copyOf(Objects.requireNonNull(buildSlots, "buildSlots"));
        this.placedTowers = List.copyOf(Objects.requireNonNull(placedTowers, "placedTowers"));
        this.activeEnemies = List.copyOf(Objects.requireNonNull(activeEnemies, "activeEnemies"));
        this.activeProjectiles = List.copyOf(Objects.requireNonNull(activeProjectiles, "activeProjectiles"));
        this.hitEffects = List.copyOf(Objects.requireNonNull(hitEffects, "hitEffects"));
        this.lightningEffects = List.copyOf(Objects.requireNonNull(lightningEffects, "lightningEffects"));
        this.mortarExplosions = List.copyOf(Objects.requireNonNull(mortarExplosions, "mortarExplosions"));
        this.gold = gold;
        this.lastBuildPlacementResult = Objects.requireNonNull(lastBuildPlacementResult, "lastBuildPlacementResult");
        this.totalKilledEnemies = totalKilledEnemies;
        this.lastAwardedGold = lastAwardedGold;
        this.totalGoldSpent = totalGoldSpent;
        this.totalGoldEarned = totalGoldEarned;
        this.wavesReached = wavesReached;
        this.enemySpawnedEvents = enemySpawnedEvents;
        this.enemyKilledEvents = enemyKilledEvents;
        this.towerBuiltEvents = towerBuiltEvents;
        this.waveStartedEvents = waveStartedEvents;
        this.waveCompletedEvents = waveCompletedEvents;
        this.enemyPath = List.copyOf(Objects.requireNonNull(enemyPath, "enemyPath"));
        this.availableTowers = List.copyOf(Objects.requireNonNull(availableTowers, "availableTowers"));
        this.selectedTowerId = selectedTowerId;
        this.gameMode = Objects.requireNonNull(gameMode, "gameMode");
        this.elapsedLevelTimeSec = Math.max(0f, elapsedLevelTimeSec);
        this.elapsedLevelTimeFormatted = Objects.requireNonNull(elapsedLevelTimeFormatted, "elapsedLevelTimeFormatted");
        this.timeScale = Math.max(0f, timeScale);
    }

    public float getTimeScale() {
        return timeScale;
    }

    public float getElapsedLevelTimeSec() {
        return elapsedLevelTimeSec;
    }

    public String getElapsedLevelTimeFormatted() {
        return elapsedLevelTimeFormatted;
    }

    public GameMode getGameMode() {
        return gameMode;
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

    public List<ActiveProjectile> getActiveProjectiles() {
        return activeProjectiles;
    }

    public List<HitEffect> getHitEffects() {
        return hitEffects;
    }

    public List<LightningEffect> getLightningEffects() {
        return lightningEffects;
    }

    public List<MortarExplosionEffect> getMortarExplosions() {
        return mortarExplosions;
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

    public int getTotalGoldSpent() {
        return totalGoldSpent;
    }

    public int getTotalGoldEarned() {
        return totalGoldEarned;
    }

    public int getWavesReached() {
        return wavesReached;
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

    public List<Vector2> getEnemyPath() {
        return enemyPath;
    }

    public List<Tower> getAvailableTowers() {
        return availableTowers;
    }

    public String getSelectedTowerId() {
        return selectedTowerId;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for immutable {@link GameRuntimeView} snapshots (GoF Builder).
     */
    public static final class Builder {

        private RuntimeLevelState levelState;
        private List<BuildSlot> buildSlots;
        private List<PlacedTower> placedTowers;
        private List<ActiveEnemy> activeEnemies;
        private List<ActiveProjectile> activeProjectiles;
        private List<HitEffect> hitEffects;
        private List<LightningEffect> lightningEffects;
        private List<MortarExplosionEffect> mortarExplosions;
        private int gold;
        private GameFacade.BuildPlacementResult lastBuildPlacementResult;
        private int totalKilledEnemies;
        private int lastAwardedGold;
        private int totalGoldSpent;
        private int totalGoldEarned;
        private int wavesReached;
        private int enemySpawnedEvents;
        private int enemyKilledEvents;
        private int towerBuiltEvents;
        private int waveStartedEvents;
        private int waveCompletedEvents;
        private List<Vector2> enemyPath;
        private List<Tower> availableTowers;
        private String selectedTowerId;
        private GameMode gameMode;
        private float elapsedLevelTimeSec;
        private String elapsedLevelTimeFormatted;
        private float timeScale;

        private Builder() {
        }

        public Builder fromRuntime(
            GameRuntimeState state,
            GameplayEventTracker eventTracker,
            List<Tower> availableTowers
        ) {
            Objects.requireNonNull(state, "state");
            Objects.requireNonNull(eventTracker, "eventTracker");
            Objects.requireNonNull(availableTowers, "availableTowers");

            levelState = state.getRuntimeLevelState();
            buildSlots = state.getBuildSlots();
            placedTowers = state.getPlacedTowers();
            activeEnemies = state.getActiveEnemies();
            activeProjectiles = state.getActiveProjectiles();
            hitEffects = state.getHitEffects();
            lightningEffects = state.getLightningEffects();
            mortarExplosions = state.getMortarExplosions();
            totalKilledEnemies = state.getSessionStats().getKills();
            lastAwardedGold = state.getLastAwardedGold();
            totalGoldSpent = state.getSessionStats().getGoldSpent();
            totalGoldEarned = state.getTotalGoldEarned();
            wavesReached = levelState.getCurrentWaveNumber();
            enemySpawnedEvents = eventTracker.getEnemySpawnedEvents();
            enemyKilledEvents = eventTracker.getEnemyKilledEvents();
            towerBuiltEvents = eventTracker.getTowerBuiltEvents();
            waveStartedEvents = eventTracker.getWaveStartedEvents();
            waveCompletedEvents = eventTracker.getWaveCompletedEvents();
            enemyPath = state.getEnemyPath();
            this.availableTowers = availableTowers;
            selectedTowerId = state.getSelectedTowerId();
            elapsedLevelTimeSec = state.getSessionStats().getElapsedSec();
            elapsedLevelTimeFormatted = state.getSessionStats().getElapsedFormatted();
            return this;
        }

        public Builder gold(int gold) {
            this.gold = gold;
            return this;
        }

        public Builder lastBuildPlacementResult(GameFacade.BuildPlacementResult lastBuildPlacementResult) {
            this.lastBuildPlacementResult = lastBuildPlacementResult;
            return this;
        }

        public Builder gameMode(GameMode gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        public Builder timeScale(float timeScale) {
            this.timeScale = timeScale;
            return this;
        }

        public GameRuntimeView build() {
            Objects.requireNonNull(levelState, "levelState");
            Objects.requireNonNull(buildSlots, "buildSlots");
            Objects.requireNonNull(placedTowers, "placedTowers");
            Objects.requireNonNull(activeEnemies, "activeEnemies");
            Objects.requireNonNull(activeProjectiles, "activeProjectiles");
            Objects.requireNonNull(hitEffects, "hitEffects");
            Objects.requireNonNull(lightningEffects, "lightningEffects");
            Objects.requireNonNull(mortarExplosions, "mortarExplosions");
            Objects.requireNonNull(lastBuildPlacementResult, "lastBuildPlacementResult");
            Objects.requireNonNull(enemyPath, "enemyPath");
            Objects.requireNonNull(availableTowers, "availableTowers");
            Objects.requireNonNull(gameMode, "gameMode");
            Objects.requireNonNull(elapsedLevelTimeFormatted, "elapsedLevelTimeFormatted");

            return new GameRuntimeView(
                levelState,
                buildSlots,
                placedTowers,
                activeEnemies,
                activeProjectiles,
                hitEffects,
                lightningEffects,
                mortarExplosions,
                gold,
                lastBuildPlacementResult,
                totalKilledEnemies,
                lastAwardedGold,
                totalGoldSpent,
                totalGoldEarned,
                wavesReached,
                enemySpawnedEvents,
                enemyKilledEvents,
                towerBuiltEvents,
                waveStartedEvents,
                waveCompletedEvents,
                enemyPath,
                availableTowers,
                selectedTowerId,
                gameMode,
                elapsedLevelTimeSec,
                elapsedLevelTimeFormatted,
                timeScale
            );
        }
    }
}
