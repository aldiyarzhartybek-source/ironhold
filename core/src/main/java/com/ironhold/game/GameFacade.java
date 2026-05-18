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
import com.ironhold.level.LevelStatus;
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
    private final LevelUpdateTemplate levelUpdateTemplate;
    private static final float TIME_SCALE_NORMAL = 1f;
    private static final float TIME_SCALE_FAST = 2f;

    private GameMode gameMode;
    private float timeScale = TIME_SCALE_NORMAL;
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
        this.levelUpdateTemplate = new DefaultLevelUpdateTemplate(
            this.waveEventSystem,
            this.spawnSystem,
            this.combatSystem,
            this::handlePostCombatFrame
        );
        this.gameMode = GameMode.CLASSIC;
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
        return runtimeState.getSessionStats().getKills();
    }

    public int getTotalGoldSpent() {
        return runtimeState.getSessionStats().getGoldSpent();
    }

    public GameplayEventTracker getEventTracker() {
        return eventTracker;
    }

    public GameRuntimeView getRuntimeView() {
        return GameRuntimeView.builder()
            .fromRuntime(runtimeState, eventTracker, towers)
            .gold(economy.getGold())
            .lastBuildPlacementResult(lastBuildPlacementResult)
            .gameMode(gameMode)
            .timeScale(timeScale)
            .build();
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

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = Objects.requireNonNull(gameMode, "gameMode");
    }

    public void startLevel() {
        startLevel(gameMode);
    }

    public void startLevel(GameMode mode) {
        this.gameMode = Objects.requireNonNull(mode, "mode");
        timeScale = TIME_SCALE_NORMAL;
        runtimeState.resetForNewLevel(initialBuildSlots);
        runtimeState.getSessionStats().markStarted();
        runtimeState.getRuntimeLevelState().start(mode);
        waveEventSystem.publishPendingWaveEvents(runtimeState);
        if (gameMode == GameMode.RUSH) {
            maybeAutoStartNextWaveInRush();
        }
    }

    public boolean canStartNextWave() {
        RuntimeLevelState levelState = runtimeState.getRuntimeLevelState();
        return runtimeState.getActiveEnemies().isEmpty() && levelState.canStartNextWave();
    }

    public boolean startNextWave() {
        if (!canStartNextWave()) {
            return false;
        }
        boolean started = runtimeState.getRuntimeLevelState().startNextWave();
        if (started) {
            waveEventSystem.publishPendingWaveEvents(runtimeState);
        }
        return started;
    }

    public float getTimeScale() {
        return timeScale;
    }

    public boolean isFastTimeScale() {
        return timeScale >= TIME_SCALE_FAST;
    }

    public void toggleTimeScale() {
        timeScale = isFastTimeScale() ? TIME_SCALE_NORMAL : TIME_SCALE_FAST;
    }

    public void updateLevel(float deltaSec) {
        float safeDeltaSec = Math.max(0f, deltaSec);
        float scaledDeltaSec = safeDeltaSec * timeScale;
        levelUpdateTemplate.updateFrame(new LevelUpdateFrameContext(runtimeState, scaledDeltaSec));
    }

    private void handlePostCombatFrame(GameRuntimeState state, RuntimeLevelState levelState) {
        maybeAutoStartNextWaveInRush();
        if (levelState.areAllWavesSpawned() && state.getActiveEnemies().isEmpty()) {
            levelState.markCompletedIfRunning();
        }
        syncLevelTimerEnd(levelState);
    }

    public float getElapsedLevelTimeSec() {
        return runtimeState.getSessionStats().getElapsedSec();
    }

    public String getElapsedLevelTimeFormatted() {
        return runtimeState.getSessionStats().getElapsedFormatted();
    }

    private void syncLevelTimerEnd(RuntimeLevelState levelState) {
        if (levelState.getStatus() != LevelStatus.RUNNING) {
            runtimeState.getSessionStats().markEnded();
        }
    }

    private void maybeAutoStartNextWaveInRush() {
        if (gameMode != GameMode.RUSH) {
            return;
        }
        if (canStartNextWave()) {
            startNextWave();
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
