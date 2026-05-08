package com.ironhold.game;

import com.badlogic.gdx.math.Vector2;
import com.ironhold.assets.AssetService;
import com.ironhold.events.EventBus;
import com.ironhold.game.model.ActiveEnemy;
import com.ironhold.game.model.BuildSlot;
import com.ironhold.game.model.EconomyState;
import com.ironhold.game.model.Enemy;
import com.ironhold.game.model.PlacedTower;
import com.ironhold.game.model.Tower;
import com.ironhold.game.model.WaveDefinition;
import com.ironhold.game.screen.ScreenNavigator;
import com.ironhold.level.RuntimeLevelState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Facade API for screens; gameplay details are delegated to orchestration subsystems.
 */
public final class GameFacade {

    private final GameContext context;
    private final AssetService assets;
    private final ScreenNavigator screens;
    private final List<Enemy> enemies;
    private final List<Tower> towers;
    private final List<WaveDefinition> waves;
    private final List<BuildSlot> initialBuildSlots;
    private final EconomyState economy;
    private final Map<String, Enemy> enemiesById;
    private final Map<String, Tower> towersById;
    private final GameplayEventTracker eventTracker;
    private final GameRuntimeState runtimeState;
    private final BuildSystem buildSystem;
    private final SpawnSystem spawnSystem;
    private final CombatRuntimeSystem combatSystem;
    private final WaveEventSystem waveEventSystem;
    private final GameRuntimeViewAssembler viewAssembler;
    private BuildPlacementResult lastBuildPlacementResult;

    public enum BuildPlacementResult {
        OK,
        NO_TOWERS_AVAILABLE,
        TOWER_NOT_FOUND,
        SLOT_NOT_FOUND,
        SLOT_OCCUPIED,
        NOT_ENOUGH_GOLD
    }

    public GameFacade(
        GameContext context,
        AssetService assets,
        ScreenNavigator screens,
        List<Enemy> enemies,
        List<Tower> towers,
        List<WaveDefinition> waves,
        List<BuildSlot> buildSlots,
        EconomyState economy
    ) {
        this.context = Objects.requireNonNull(context, "context");
        this.assets = Objects.requireNonNull(assets, "assets");
        this.screens = Objects.requireNonNull(screens, "screens");
        this.enemies = List.copyOf(Objects.requireNonNull(enemies, "enemies"));
        this.towers = List.copyOf(Objects.requireNonNull(towers, "towers"));
        this.waves = List.copyOf(Objects.requireNonNull(waves, "waves"));
        this.initialBuildSlots = List.copyOf(Objects.requireNonNull(buildSlots, "buildSlots"));
        this.economy = Objects.requireNonNull(economy, "economy");
        this.enemiesById = indexEnemiesById(this.enemies);
        this.towersById = indexTowersById(this.towers);
        this.eventTracker = new GameplayEventTracker(getEventBus());
        this.runtimeState = new GameRuntimeState(
            new RuntimeLevelState(this.waves),
            this.initialBuildSlots,
            defaultEnemyPath(),
            this.towers.isEmpty() ? null : this.towers.get(0).getId()
        );
        this.buildSystem = new BuildSystem(getEventBus(), this.economy, this.towersById);
        this.spawnSystem = new SpawnSystem(getEventBus(), this.enemiesById);
        this.combatSystem = new CombatRuntimeSystem(getEventBus(), this.economy);
        this.waveEventSystem = new WaveEventSystem(getEventBus());
        this.viewAssembler = new GameRuntimeViewAssembler(this.eventTracker, this.towers);
        this.lastBuildPlacementResult = BuildPlacementResult.SLOT_NOT_FOUND;
    }

    public GameContext getContext() {
        return context;
    }

    public EventBus getEventBus() {
        return context.getEventBus();
    }

    public AssetService getAssets() {
        return assets;
    }

    public ScreenNavigator getScreens() {
        return screens;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Tower> getTowers() {
        return towers;
    }

    public List<WaveDefinition> getWaves() {
        return waves;
    }

    public List<BuildSlot> getBuildSlots() {
        return List.copyOf(runtimeState.getBuildSlots());
    }

    public EconomyState getEconomy() {
        return economy;
    }

    public List<ActiveEnemy> getActiveEnemies() {
        return List.copyOf(runtimeState.getActiveEnemies());
    }

    public List<PlacedTower> getPlacedTowers() {
        return List.copyOf(runtimeState.getPlacedTowers());
    }

    public RuntimeLevelState getRuntimeLevelState() {
        return runtimeState.getRuntimeLevelState();
    }

    public BuildPlacementResult getLastBuildPlacementResult() {
        return lastBuildPlacementResult;
    }

    public int getLastAwardedGold() {
        return runtimeState.getLastAwardedGold();
    }

    public int getTotalKilledEnemies() {
        return runtimeState.getTotalKilledEnemies();
    }

    public GameplayEventTracker getEventTracker() {
        return eventTracker;
    }

    public GameRuntimeView getRuntimeView() {
        return viewAssembler.assemble(runtimeState, economy.getGold(), lastBuildPlacementResult);
    }

    public void dispose() {
        eventTracker.dispose();
    }

    public void handlePrimaryAction(float worldX, float worldY) {
        tryPlaceTowerAt(worldX, worldY);
    }

    public void handleDebugKillAction() {
        combatSystem.debugDefeatFirstEnemy(runtimeState);
    }

    public boolean tryPlaceTowerAt(float worldX, float worldY) {
        if (towers.isEmpty() || runtimeState.getSelectedTowerId() == null) {
            lastBuildPlacementResult = buildSystem.failNoTowers(worldX, worldY);
            return false;
        }
        lastBuildPlacementResult = buildSystem.tryPlaceTower(
            runtimeState,
            runtimeState.getSelectedTowerId(),
            worldX,
            worldY
        );
        return lastBuildPlacementResult == BuildPlacementResult.OK;
    }

    public void selectTower(String towerId) {
        if (towerId == null || !towersById.containsKey(towerId)) {
            return;
        }
        runtimeState.setSelectedTowerId(towerId);
    }

    public void selectTowerByIndex(int index) {
        if (index < 0 || index >= towers.size()) {
            return;
        }
        runtimeState.setSelectedTowerId(towers.get(index).getId());
    }

    public BuildPlacementResult tryPlaceTower(float worldX, float worldY, String towerId) {
        lastBuildPlacementResult = buildSystem.tryPlaceTower(runtimeState, towerId, worldX, worldY);
        return lastBuildPlacementResult;
    }

    public boolean debugDefeatFirstEnemy() {
        return combatSystem.debugDefeatFirstEnemy(runtimeState);
    }

    public void startLevel() {
        runtimeState.resetForNewLevel(initialBuildSlots);
        runtimeState.getRuntimeLevelState().start();
        waveEventSystem.publishPendingWaveEvents(runtimeState);
    }

    public void updateLevel(float deltaSec) {
        float safeDeltaSec = Math.max(0f, deltaSec);
        runtimeState.getRuntimeLevelState().update(safeDeltaSec);
        waveEventSystem.publishPendingWaveEvents(runtimeState);
        spawnSystem.processPendingSpawns(runtimeState);
        combatSystem.update(runtimeState, safeDeltaSec);
        if (runtimeState.getRuntimeLevelState().areAllWavesSpawned()
            && runtimeState.getActiveEnemies().isEmpty()) {
            runtimeState.getRuntimeLevelState().markCompletedIfRunning();
        }
    }

    private static Map<String, Enemy> indexEnemiesById(List<Enemy> enemies) {
        Map<String, Enemy> indexed = new HashMap<>();
        for (Enemy enemy : enemies) {
            indexed.put(enemy.getId(), enemy);
        }
        return indexed;
    }

    private static Map<String, Tower> indexTowersById(List<Tower> towers) {
        Map<String, Tower> indexed = new HashMap<>();
        for (Tower tower : towers) {
            indexed.put(tower.getId(), tower);
        }
        return indexed;
    }

    private static List<Vector2> defaultEnemyPath() {
        List<Vector2> path = new ArrayList<>();
        path.add(new Vector2(64f, 332f));
        path.add(new Vector2(220f, 332f));
        path.add(new Vector2(220f, 220f));
        path.add(new Vector2(460f, 220f));
        path.add(new Vector2(460f, 360f));
        path.add(new Vector2(760f, 360f));
        return List.copyOf(path);
    }
}
