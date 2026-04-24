package com.ironhold.game;

import com.badlogic.gdx.math.Vector2;
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
    private static final float ENEMY_SPEED_MULTIPLIER = 20.0f;

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
    private final List<Vector2> enemyPath;
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
        this.enemyPath = defaultEnemyPath();
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
        float safeDeltaSec = Math.max(0f, deltaSec);
        runtimeLevelState.update(safeDeltaSec);
        for (String enemyId : runtimeLevelState.consumePendingSpawnEnemyIds()) {
            spawnEnemy(enemyId);
        }
        updateEnemyMovement(safeDeltaSec);
        if (runtimeLevelState.areAllWavesSpawned()
            && activeEnemies.isEmpty()) {
            runtimeLevelState.markCompletedIfRunning();
        }
    }

    private void spawnEnemy(String enemyId) {
        Enemy template = enemiesById.get(enemyId);
        if (template == null || enemyPath.isEmpty()) {
            return;
        }
        Vector2 spawn = enemyPath.get(0);
        ActiveEnemy enemy = new ActiveEnemy(
            "enemy-" + nextEnemyInstanceId++,
            template.getId(),
            template.getMaxHp(),
            template.getCurrentHp(),
            template.getSpeed(),
            template.getReward(),
            spawn.x,
            spawn.y,
            1
        );
        activeEnemies.add(enemy);
    }

    private void updateEnemyMovement(float deltaSec) {
        if (deltaSec <= 0f || activeEnemies.isEmpty() || enemyPath.size() < 2) {
            return;
        }
        List<ActiveEnemy> escapedEnemies = new ArrayList<>();
        for (ActiveEnemy enemy : activeEnemies) {
            if (advanceEnemyAlongPath(enemy, deltaSec)) {
                escapedEnemies.add(enemy);
            }
        }
        if (!escapedEnemies.isEmpty()) {
            activeEnemies.removeAll(escapedEnemies);
            for (int i = 0; i < escapedEnemies.size(); i++) {
                runtimeLevelState.onEnemyEscaped();
            }
        }
    }

    private boolean advanceEnemyAlongPath(ActiveEnemy enemy, float deltaSec) {
        float remainingDistance = enemy.getSpeed() * ENEMY_SPEED_MULTIPLIER * deltaSec;
        while (remainingDistance > 0f) {
            int targetIndex = enemy.getTargetWaypointIndex();
            if (targetIndex >= enemyPath.size()) {
                return true;
            }
            Vector2 target = enemyPath.get(targetIndex);
            float dx = target.x - enemy.getX();
            float dy = target.y - enemy.getY();
            float distanceToTarget = (float) Math.sqrt(dx * dx + dy * dy);
            if (distanceToTarget <= 0.001f) {
                enemy.setPosition(target.x, target.y);
                enemy.setTargetWaypointIndex(targetIndex + 1);
                continue;
            }
            if (remainingDistance >= distanceToTarget) {
                enemy.setPosition(target.x, target.y);
                enemy.setTargetWaypointIndex(targetIndex + 1);
                remainingDistance -= distanceToTarget;
            } else {
                float ratio = remainingDistance / distanceToTarget;
                enemy.setPosition(
                    enemy.getX() + dx * ratio,
                    enemy.getY() + dy * ratio
                );
                remainingDistance = 0f;
            }
        }
        return enemy.getTargetWaypointIndex() >= enemyPath.size();
    }

    private static Map<String, Enemy> indexEnemiesById(List<Enemy> enemies) {
        Map<String, Enemy> indexed = new HashMap<>();
        for (Enemy enemy : enemies) {
            indexed.put(enemy.getId(), enemy);
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
