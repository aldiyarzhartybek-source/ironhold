package com.ironhold.game;

import com.badlogic.gdx.math.Vector2;
import com.ironhold.events.EnemySpawnedEvent;
import com.ironhold.events.EventBus;
import com.ironhold.game.model.ActiveEnemy;
import com.ironhold.game.model.Enemy;
import com.ironhold.level.WavePhase;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Converts pending wave spawns into active runtime enemies.
 */
public final class SpawnSystem {

    private final EventBus eventBus;
    private final Map<String, Enemy> enemiesById;

    public SpawnSystem(EventBus eventBus, Map<String, Enemy> enemiesById) {
        this.eventBus = Objects.requireNonNull(eventBus, "eventBus");
        this.enemiesById = Objects.requireNonNull(enemiesById, "enemiesById");
    }

    public void processPendingSpawns(GameRuntimeState state) {
        if (state.getRuntimeLevelState().getWavePhase() != WavePhase.WAVE_ACTIVE) {
            return;
        }
        for (String enemyId : state.getRuntimeLevelState().consumePendingSpawnEnemyIds()) {
            spawnEnemy(state, enemyId);
        }
    }

    private void spawnEnemy(GameRuntimeState state, String enemyId) {
        Enemy template = enemiesById.get(enemyId);
        List<Vector2> path = state.getEnemyPath();
        if (template == null || path.isEmpty()) {
            return;
        }

        int scaledHp = scaleHp(template.getMaxHp(), state.getLevelHpMultiplier());
        Vector2 gate = path.get(0);
        ActiveEnemy enemy = new ActiveEnemy(
            "enemy-" + state.getNextEnemyInstanceId(),
            template.getId(),
            scaledHp,
            scaledHp,
            template.getSpeed(),
            template.getReward(),
            gate.x,
            gate.y,
            1
        );
        state.getActiveEnemies().add(enemy);
        eventBus.publish(new EnemySpawnedEvent(
            enemy.getRuntimeId(),
            enemy.getEnemyId(),
            state.getRuntimeLevelState().getCurrentWaveNumber()
        ));
    }

    private static int scaleHp(int baseHp, float multiplier) {
        return Math.max(1, Math.round(baseHp * Math.max(1f, multiplier)));
    }
}
