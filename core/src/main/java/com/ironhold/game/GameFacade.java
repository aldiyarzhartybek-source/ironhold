package com.ironhold.game;

import com.badlogic.gdx.math.Vector2;
import com.ironhold.assets.AssetService;
import com.ironhold.events.EnemyKilledEvent;
import com.ironhold.events.EnemySpawnedEvent;
import com.ironhold.events.EventBus;
import com.ironhold.events.TowerBuiltEvent;
import com.ironhold.events.WaveCompletedEvent;
import com.ironhold.events.WaveStartedEvent;
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
 * Единая точка доступа к runtime-сервисам (фасад для экранов и будущих систем).
 */
public final class GameFacade {
    private static final float ENEMY_SPEED_MULTIPLIER = 10.0f;
    private static final float BUILD_SLOT_CLICK_RADIUS = 28f;
    private static final float MIN_RUNTIME_ENEMY_SPEED = 0.1f;
    private static final float MIN_RUNTIME_TOWER_RANGE = 16f;
    private static final int MIN_RUNTIME_TOWER_DAMAGE = 1;
    private static final float MIN_RUNTIME_TOWER_FIRE_RATE_SEC = 0.1f;

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
    private final Map<String, Tower> towersById;
    private final List<ActiveEnemy> activeEnemies;
    private final List<PlacedTower> placedTowers;
    private final List<Vector2> enemyPath;
    private int nextEnemyInstanceId;
    private BuildPlacementResult lastBuildPlacementResult;
    private int lastAwardedGold;
    private int totalKilledEnemies;
    private final GameplayEventTracker eventTracker;

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
        this.buildSlots = new ArrayList<>(Objects.requireNonNull(buildSlots, "buildSlots"));
        this.economy = Objects.requireNonNull(economy, "economy");
        this.runtimeLevelState = new RuntimeLevelState(this.waves);
        this.enemiesById = indexEnemiesById(this.enemies);
        this.towersById = indexTowersById(this.towers);
        this.activeEnemies = new ArrayList<>();
        this.placedTowers = new ArrayList<>();
        this.enemyPath = defaultEnemyPath();
        this.nextEnemyInstanceId = 1;
        this.lastBuildPlacementResult = BuildPlacementResult.SLOT_NOT_FOUND;
        this.lastAwardedGold = 0;
        this.totalKilledEnemies = 0;
        this.eventTracker = new GameplayEventTracker(getEventBus());
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
        return List.copyOf(buildSlots);
    }

    public EconomyState getEconomy() {
        return economy;
    }

    public List<ActiveEnemy> getActiveEnemies() {
        return List.copyOf(activeEnemies);
    }

    public List<PlacedTower> getPlacedTowers() {
        return List.copyOf(placedTowers);
    }

    public RuntimeLevelState getRuntimeLevelState() {
        return runtimeLevelState;
    }

    public BuildPlacementResult getLastBuildPlacementResult() {
        return lastBuildPlacementResult;
    }

    public int getLastAwardedGold() {
        return lastAwardedGold;
    }

    public int getTotalKilledEnemies() {
        return totalKilledEnemies;
    }

    public GameplayEventTracker getEventTracker() {
        return eventTracker;
    }

    public GameRuntimeView getRuntimeView() {
        return new GameRuntimeView(
            runtimeLevelState,
            buildSlots,
            placedTowers,
            activeEnemies,
            economy.getGold(),
            lastBuildPlacementResult,
            totalKilledEnemies,
            lastAwardedGold,
            eventTracker.getEnemySpawnedEvents(),
            eventTracker.getEnemyKilledEvents(),
            eventTracker.getTowerBuiltEvents(),
            eventTracker.getWaveStartedEvents(),
            eventTracker.getWaveCompletedEvents()
        );
    }

    public void dispose() {
        eventTracker.dispose();
    }

    public void handlePrimaryAction(float worldX, float worldY) {
        tryPlaceTowerAt(worldX, worldY);
    }

    public void handleDebugKillAction() {
        debugDefeatFirstEnemy();
    }

    public boolean tryPlaceTowerAt(float worldX, float worldY) {
        if (towers.isEmpty()) {
            lastBuildPlacementResult = BuildPlacementResult.NO_TOWERS_AVAILABLE;
            return false;
        }
        return tryPlaceTower(worldX, worldY, towers.get(0).getId()) == BuildPlacementResult.OK;
    }

    public BuildPlacementResult tryPlaceTower(float worldX, float worldY, String towerId) {
        Tower tower = towersById.get(towerId);
        if (tower == null) {
            lastBuildPlacementResult = BuildPlacementResult.TOWER_NOT_FOUND;
            return lastBuildPlacementResult;
        }
        int slotIndex = findNearestBuildSlotIndex(worldX, worldY, BUILD_SLOT_CLICK_RADIUS);
        if (slotIndex < 0) {
            lastBuildPlacementResult = BuildPlacementResult.SLOT_NOT_FOUND;
            return lastBuildPlacementResult;
        }
        BuildSlot slot = buildSlots.get(slotIndex);
        if (slot.isOccupied()) {
            lastBuildPlacementResult = BuildPlacementResult.SLOT_OCCUPIED;
            return lastBuildPlacementResult;
        }
        if (!economy.trySpend(tower.getCost())) {
            lastBuildPlacementResult = BuildPlacementResult.NOT_ENOUGH_GOLD;
            return lastBuildPlacementResult;
        }
        buildSlots.set(slotIndex, slot.withTower(towerId));
        placedTowers.add(new PlacedTower(
            slot.getSlotId(),
            tower.getId(),
            slot.getX(),
            slot.getY(),
            Math.max(MIN_RUNTIME_TOWER_RANGE, tower.getRange()),
            Math.max(MIN_RUNTIME_TOWER_DAMAGE, tower.getDamage()),
            Math.max(MIN_RUNTIME_TOWER_FIRE_RATE_SEC, tower.getFireRateSec())
        ));
        lastBuildPlacementResult = BuildPlacementResult.OK;
        getEventBus().publish(new TowerBuiltEvent(tower.getId(), slot.getSlotId(), tower.getCost()));
        return lastBuildPlacementResult;
    }

    public boolean debugDefeatFirstEnemy() {
        if (activeEnemies.isEmpty()) {
            return false;
        }
        ActiveEnemy defeated = activeEnemies.remove(0);
        awardKill(defeated);
        return true;
    }

    public void startLevel() {
        activeEnemies.clear();
        placedTowers.clear();
        resetBuildSlots();
        nextEnemyInstanceId = 1;
        lastAwardedGold = 0;
        totalKilledEnemies = 0;
        runtimeLevelState.start();
        publishPendingWaveEvents();
    }

    public void updateLevel(float deltaSec) {
        float safeDeltaSec = Math.max(0f, deltaSec);
        runtimeLevelState.update(safeDeltaSec);
        publishPendingWaveEvents();
        for (String enemyId : runtimeLevelState.consumePendingSpawnEnemyIds()) {
            spawnEnemy(enemyId);
        }
        updateEnemyMovement(safeDeltaSec);
        updateTowerCombat(safeDeltaSec);
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
        getEventBus().publish(new EnemySpawnedEvent(
            enemy.getRuntimeId(),
            enemy.getEnemyId(),
            runtimeLevelState.getCurrentWaveNumber()
        ));
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
        float safeSpeed = Math.max(MIN_RUNTIME_ENEMY_SPEED, enemy.getSpeed());
        float remainingDistance = safeSpeed * ENEMY_SPEED_MULTIPLIER * deltaSec;
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

    private void updateTowerCombat(float deltaSec) {
        if (deltaSec <= 0f || placedTowers.isEmpty() || activeEnemies.isEmpty()) {
            return;
        }
        List<ActiveEnemy> killed = new ArrayList<>();
        for (PlacedTower tower : placedTowers) {
            float cooldown = Math.max(0f, tower.getCooldownSec() - deltaSec);
            tower.setCooldownSec(cooldown);
            if (cooldown > 0f) {
                continue;
            }
            ActiveEnemy target = findNearestTargetInRange(tower);
            if (target == null) {
                continue;
            }
            target.setCurrentHp(target.getCurrentHp() - tower.getDamage());
            tower.setCooldownSec(tower.getFireRateSec());
            if (target.getCurrentHp() <= 0 && !killed.contains(target)) {
                killed.add(target);
            }
        }
        if (!killed.isEmpty()) {
            for (ActiveEnemy enemy : killed) {
                awardKill(enemy);
            }
            activeEnemies.removeAll(killed);
        }
    }

    private ActiveEnemy findNearestTargetInRange(PlacedTower tower) {
        ActiveEnemy best = null;
        float bestDistanceSq = Float.MAX_VALUE;
        float rangeSq = tower.getRange() * tower.getRange();
        for (ActiveEnemy enemy : activeEnemies) {
            float dx = enemy.getX() - tower.getX();
            float dy = enemy.getY() - tower.getY();
            float distanceSq = dx * dx + dy * dy;
            if (distanceSq > rangeSq) {
                continue;
            }
            if (distanceSq < bestDistanceSq) {
                bestDistanceSq = distanceSq;
                best = enemy;
            }
        }
        return best;
    }

    private void awardKill(ActiveEnemy enemy) {
        int reward = economy.calculateKillReward(enemy.getReward());
        economy.addGold(reward);
        lastAwardedGold = reward;
        totalKilledEnemies++;
        getEventBus().publish(new EnemyKilledEvent(enemy.getRuntimeId(), enemy.getEnemyId(), reward));
    }

    private void publishPendingWaveEvents() {
        for (int waveNumber : runtimeLevelState.consumePendingWaveStartedNumbers()) {
            getEventBus().publish(new WaveStartedEvent(waveNumber, runtimeLevelState.getTotalWaves()));
        }
        for (int waveNumber : runtimeLevelState.consumePendingWaveCompletedNumbers()) {
            getEventBus().publish(new WaveCompletedEvent(waveNumber, runtimeLevelState.getTotalWaves()));
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

    private int findNearestBuildSlotIndex(float worldX, float worldY, float radius) {
        float bestDistanceSq = radius * radius;
        int bestIndex = -1;
        for (int i = 0; i < buildSlots.size(); i++) {
            BuildSlot slot = buildSlots.get(i);
            float dx = worldX - slot.getX();
            float dy = worldY - slot.getY();
            float distanceSq = dx * dx + dy * dy;
            if (distanceSq <= bestDistanceSq) {
                bestDistanceSq = distanceSq;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private void resetBuildSlots() {
        for (int i = 0; i < buildSlots.size(); i++) {
            BuildSlot slot = buildSlots.get(i);
            buildSlots.set(i, new BuildSlot(slot.getSlotId(), slot.getX(), slot.getY(), false, null));
        }
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
