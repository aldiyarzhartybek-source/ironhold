package com.ironhold.game.model;

import com.ironhold.config.GameConfig;
import com.ironhold.config.dto.EnemyConfigDto;
import com.ironhold.config.dto.TowerConfigDto;
import com.ironhold.config.dto.WaveEntryDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Maps JSON DTO config objects to runtime domain skeletons.
 */
public final class GameModelMapper {

    private GameModelMapper() {
    }

    public static List<Enemy> mapEnemies(GameConfig config) {
        Objects.requireNonNull(config, "config");
        List<Enemy> result = new ArrayList<>();
        for (EnemyConfigDto enemy : config.getEnemies().enemies) {
            result.add(new Enemy(enemy.id, enemy.hp, enemy.hp, enemy.speed, enemy.reward));
        }
        return result;
    }

    public static List<Tower> mapTowers(GameConfig config) {
        Objects.requireNonNull(config, "config");
        List<Tower> result = new ArrayList<>();
        for (TowerConfigDto tower : config.getTowers().towers) {
            result.add(new Tower(tower.id, tower.cost, tower.range, tower.damage, tower.fireRateSec));
        }
        return result;
    }

    public static List<WaveDefinition> mapWaves(GameConfig config) {
        Objects.requireNonNull(config, "config");
        List<WaveDefinition> result = new ArrayList<>();
        for (WaveEntryDto wave : config.getWaves().waves) {
            result.add(new WaveDefinition(wave.enemyId, wave.count, wave.spawnIntervalSec));
        }
        return result;
    }

    public static EconomyState mapEconomy(GameConfig config) {
        Objects.requireNonNull(config, "config");
        return new EconomyState(
            config.getEconomy().startingGold,
            config.getEconomy().killRewardMultiplier,
            config.getEconomy().buildRefundRate
        );
    }

    public static List<BuildSlot> defaultBuildSlots() {
        List<BuildSlot> slots = new ArrayList<>();
        slots.add(new BuildSlot("slot-1", 220f, 180f, false, null));
        slots.add(new BuildSlot("slot-2", 360f, 180f, false, null));
        slots.add(new BuildSlot("slot-3", 500f, 180f, false, null));
        return slots;
    }
}
