package com.ironhold.game.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.ironhold.config.GameConfig;
import com.ironhold.config.dto.BuildSlotDto;
import com.ironhold.config.dto.EnemyConfigDto;
import com.ironhold.config.dto.TowerConfigDto;
import com.ironhold.config.dto.Vec2Dto;
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
            config.getEconomy().sellRecoveryRate
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
        return List.copyOf(slots);
    }

    public static List<Vector2> defaultEnemyPath() {
        List<Vector2> path = new ArrayList<>();
        path.add(new Vector2(64f, 332f));
        path.add(new Vector2(220f, 332f));
        path.add(new Vector2(220f, 220f));
        path.add(new Vector2(460f, 220f));
        path.add(new Vector2(460f, 360f));
        path.add(new Vector2(760f, 360f));
        return List.copyOf(path);
    }

    public static List<Vector2> mapEnemyPath(List<Vec2Dto> points) {
        if (points == null || points.size() < 2) {
            return defaultEnemyPath();
        }
        List<Vector2> path = new ArrayList<>(points.size());
        for (Vec2Dto point : points) {
            if (point == null) {
                continue;
            }
            path.add(new Vector2(point.x, point.y));
        }
        return path.size() >= 2 ? List.copyOf(path) : defaultEnemyPath();
    }

    public static List<BuildSlot> mapBuildSlots(List<BuildSlotDto> slots) {
        if (slots == null || slots.isEmpty()) {
            return defaultBuildSlots();
        }
        List<BuildSlot> mapped = new ArrayList<>();
        int index = 1;
        for (BuildSlotDto slot : slots) {
            if (slot == null) {
                continue;
            }
            String slotId = slot.slotId;
            if (slotId == null || slotId.isBlank()) {
                slotId = "slot-" + index;
            }
            mapped.add(new BuildSlot(slotId.trim(), slot.x, slot.y, false, null));
            index++;
        }
        return mapped.isEmpty() ? defaultBuildSlots() : List.copyOf(mapped);
    }
}
