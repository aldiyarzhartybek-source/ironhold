package com.ironhold.game.model;

import com.badlogic.gdx.graphics.Color;
import com.ironhold.config.GameConfig;
import com.ironhold.config.dto.EnemyConfigDto;
import com.ironhold.config.dto.TowerConfigDto;
import com.ironhold.ui.GameTheme;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Maps JSON DTO config objects to runtime domain skeletons.
 */
public final class GameModelMapper {
    private static final float TILE_SIZE_PX = 64f;
    private GameModelMapper() {
    }

    public static List<Enemy> mapEnemies(GameConfig config) {
        Objects.requireNonNull(config, "config");
        List<Enemy> result = new ArrayList<>();
        for (EnemyConfigDto enemy : config.getEnemies().enemies) {
            result.add(new Enemy(
                enemy.id,
                enemy.hp,
                enemy.hp,
                enemy.speed,
                enemy.reward,
                EnemyVisualShape.fromConfig(enemy.visualShape),
                parseFillColor(enemy.fillColor),
                enemy.visualScale
            ));
        }
        return result;
    }

    public static List<Tower> mapTowers(GameConfig config) {
        Objects.requireNonNull(config, "config");
        List<Tower> result = new ArrayList<>();
        for (TowerConfigDto tower : config.getTowers().towers) {
            result.add(new Tower(
                tower.id,
                tower.cost,
                tower.range * TILE_SIZE_PX,
                tower.damage,
                tower.fireRateSec,
                TowerTargetingPriority.fromConfig(tower.targeting)
            ));
        }
        return result;
    }

    public static List<WaveDefinition> mapWaves(GameConfig config) {
        Objects.requireNonNull(config, "config");
        return WaveDefinitionFactory.fromLevelWaves(config.getWaves());
    }

    public static EconomyState mapEconomy(GameConfig config) {
        Objects.requireNonNull(config, "config");
        return new EconomyState(
            config.getEconomy().startingGold,
            config.getEconomy().killRewardMultiplier,
            config.getEconomy().buildRefundRate
        );
    }

    private static Color parseFillColor(String raw) {
        if (raw == null || raw.isBlank()) {
            return new Color(GameTheme.ENEMY_ACCENT);
        }
        try {
            return Color.valueOf(raw.trim());
        } catch (Exception ignored) {
            return new Color(GameTheme.ENEMY_ACCENT);
        }
    }

    public static List<BuildSlot> defaultBuildSlots() {
        List<BuildSlot> slots = new ArrayList<>();
        slots.add(new BuildSlot("slot-1", 150f, 288f, false, null));
        slots.add(new BuildSlot("slot-2", 340f, 180f, false, null));
        slots.add(new BuildSlot("slot-3", 580f, 320f, false, null));
        return slots;
    }
}
