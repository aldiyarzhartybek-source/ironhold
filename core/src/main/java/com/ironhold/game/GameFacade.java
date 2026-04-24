package com.ironhold.game;

import com.ironhold.assets.AssetService;
import com.ironhold.events.EventBus;
import com.ironhold.game.model.ActiveEnemy;
import com.ironhold.game.model.BuildSlot;
import com.ironhold.game.model.EconomyState;
import com.ironhold.game.model.Enemy;
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
 * Единая точка доступа к runtime-сервисам (фасад для экранов и будущих систем).
 */
public final class GameFacade {

    private final GameContext context;
    private final AssetService assets;
    private final ScreenNavigator screens;
    private final List<Enemy> enemies;
    private final List<Tower> towers;
    private final List<WaveDefinition> waves;
    private final List<BuildSlot> buildSlots;
    private final EconomyState economy;
    private final RuntimeLevelState runtimeLevelState;
    private final Map<String, Enemy> enemiesById;
    private final List<ActiveEnemy> activeEnemies;
    private int nextEnemyInstanceId;

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
        this.buildSlots = List.copyOf(Objects.requireNonNull(buildSlots, "buildSlots"));
        this.economy = Objects.requireNonNull(economy, "economy");
        this.runtimeLevelState = new RuntimeLevelState(this.waves);
        this.enemiesById = indexEnemiesById(this.enemies);
        this.activeEnemies = new ArrayList<>();
        this.nextEnemyInstanceId = 1;
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
        return buildSlots;
    }

    public EconomyState getEconomy() {
        return economy;
    }

    public List<ActiveEnemy> getActiveEnemies() {
        return List.copyOf(activeEnemies);
    }

    public RuntimeLevelState getRuntimeLevelState() {
        return runtimeLevelState;
    }

    public void startLevel() {
        activeEnemies.clear();
        nextEnemyInstanceId = 1;
        runtimeLevelState.start();
    }

    public void updateLevel(float deltaSec) {
        runtimeLevelState.update(deltaSec);
        for (String enemyId : runtimeLevelState.consumePendingSpawnEnemyIds()) {
            spawnEnemy(enemyId);
        }
    }

    private void spawnEnemy(String enemyId) {
        Enemy template = enemiesById.get(enemyId);
        if (template == null) {
            return;
        }
        float spawnX = 48f + (activeEnemies.size() % 10) * 40f;
        float spawnY = 240f + (activeEnemies.size() / 10) * 40f;
        ActiveEnemy enemy = new ActiveEnemy(
            "enemy-" + nextEnemyInstanceId++,
            template.getId(),
            template.getMaxHp(),
            template.getCurrentHp(),
            template.getSpeed(),
            template.getReward(),
            spawnX,
            spawnY
        );
        activeEnemies.add(enemy);
    }

    private static Map<String, Enemy> indexEnemiesById(List<Enemy> enemies) {
        Map<String, Enemy> indexed = new HashMap<>();
        for (Enemy enemy : enemies) {
            indexed.put(enemy.getId(), enemy);
        }
        return indexed;
    }
}
