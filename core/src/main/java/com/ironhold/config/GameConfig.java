package com.ironhold.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.ironhold.config.dto.EconomyConfigDto;
import com.ironhold.config.dto.EnemiesConfigDto;
import com.ironhold.config.dto.EnemyConfigDto;
import com.ironhold.config.dto.TowerConfigDto;
import com.ironhold.config.dto.TowersConfigDto;
import com.ironhold.config.dto.WaveEntryDto;
import com.ironhold.config.dto.WavesConfigDto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Aggregates balance configs loaded from JSON files into DTOs.
 */
public final class GameConfig {
    private static final String TAG = "GameConfig";

    private static final String ENEMIES_PATH = "config/enemies.json";
    private static final String TOWERS_PATH = "config/towers.json";
    private static final String WAVES_PATH = "config/waves.json";
    private static final String ECONOMY_PATH = "config/economy.json";

    private static final int MIN_ENEMY_HP = 1;
    private static final int MAX_ENEMY_HP = 10_000;
    private static final float MIN_ENEMY_SPEED = 0.1f;
    private static final float MAX_ENEMY_SPEED = 20f;
    private static final int MIN_ENEMY_REWARD = 0;
    private static final int MAX_ENEMY_REWARD = 1_000;

    private static final int MIN_TOWER_COST = 0;
    private static final int MAX_TOWER_COST = 2_000;
    private static final float MIN_TOWER_RANGE = 0.75f;
    private static final float MAX_TOWER_RANGE = 8.0f;
    private static final int MIN_TOWER_DAMAGE = 1;
    private static final int MAX_TOWER_DAMAGE = 500;
    private static final float MIN_TOWER_FIRE_RATE_SEC = 0.15f;
    private static final float MAX_TOWER_FIRE_RATE_SEC = 5.0f;

    private static final int MIN_WAVE_COUNT = 1;
    private static final int MAX_WAVE_COUNT = 100;
    private static final float MIN_WAVE_SPAWN_INTERVAL_SEC = 0.15f;
    private static final float MAX_WAVE_SPAWN_INTERVAL_SEC = 5.0f;

    private static final int MIN_STARTING_GOLD = 0;
    private static final int MAX_STARTING_GOLD = 2_000;
    private static final float MIN_KILL_REWARD_MULTIPLIER = 0.25f;
    private static final float MAX_KILL_REWARD_MULTIPLIER = 3.0f;

    private final EnemiesConfigDto enemies;
    private final TowersConfigDto towers;
    private final WavesConfigDto waves;
    private final EconomyConfigDto economy;

    private GameConfig(
        EnemiesConfigDto enemies,
        TowersConfigDto towers,
        WavesConfigDto waves,
        EconomyConfigDto economy
    ) {
        this.enemies = Objects.requireNonNull(enemies, "enemies");
        this.towers = Objects.requireNonNull(towers, "towers");
        this.waves = Objects.requireNonNull(waves, "waves");
        this.economy = Objects.requireNonNull(economy, "economy");
    }

    public static GameConfig loadDefault() {
        Json json = new Json();

        EnemiesConfigDto enemies = readOrDefault(json, ENEMIES_PATH, EnemiesConfigDto.class, GameConfig::defaultEnemies);
        TowersConfigDto towers = readOrDefault(json, TOWERS_PATH, TowersConfigDto.class, GameConfig::defaultTowers);
        WavesConfigDto waves = readOrDefault(json, WAVES_PATH, WavesConfigDto.class, GameConfig::defaultWaves);
        EconomyConfigDto economy = readOrDefault(json, ECONOMY_PATH, EconomyConfigDto.class, GameConfig::defaultEconomy);

        if (enemies.enemies == null) {
            enemies.enemies = new ArrayList<>();
        }
        if (towers.towers == null) {
            towers.towers = new ArrayList<>();
        }
        if (waves.waves == null) {
            waves.waves = new ArrayList<>();
        }

        ensureNonEmpty(enemies.enemies, defaultEnemies().enemies);
        ensureNonEmpty(towers.towers, defaultTowers().towers);
        ensureNonEmpty(waves.waves, defaultWaves().waves);

        sanitizeEnemies(enemies);
        sanitizeTowers(towers);
        sanitizeWaves(waves, enemies);
        sanitizeEconomy(economy);

        return new GameConfig(enemies, towers, waves, economy);
    }

    public EnemiesConfigDto getEnemies() {
        return enemies;
    }

    public TowersConfigDto getTowers() {
        return towers;
    }

    public WavesConfigDto getWaves() {
        return waves;
    }

    public EconomyConfigDto getEconomy() {
        return economy;
    }

    private static <T> T readOrDefault(Json json, String internalPath, Class<T> type, Supplier<T> defaultSupplier) {
        Objects.requireNonNull(json, "json");
        Objects.requireNonNull(internalPath, "internalPath");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(defaultSupplier, "defaultSupplier");

        FileHandle file = Gdx.files.internal(internalPath);
        if (!file.exists()) {
            return defaultSupplier.get();
        }
        try {
            T parsed = json.fromJson(type, file);
            return parsed != null ? parsed : defaultSupplier.get();
        } catch (Exception ignored) {
            return defaultSupplier.get();
        }
    }

    private static <T> void ensureNonEmpty(List<T> target, List<T> fallback) {
        if (!target.isEmpty()) {
            return;
        }
        target.addAll(fallback);
    }

    private static void sanitizeEnemies(EnemiesConfigDto enemies) {
        Set<String> usedIds = new HashSet<>();
        for (int i = 0; i < enemies.enemies.size(); i++) {
            EnemyConfigDto enemy = enemies.enemies.get(i);
            EnemyConfigDto fallback = defaultEnemies().enemies.get(0);
            String id = normalizeId(enemy.id, "enemy", i);
            if (usedIds.contains(id)) {
                String uniqueId = id + "_" + (i + 1);
                warn("Duplicate enemy id '" + id + "', renamed to '" + uniqueId + "'");
                id = uniqueId;
            }
            usedIds.add(id);
            enemy.id = id;
            enemy.hp = clampInt(enemy.hp, MIN_ENEMY_HP, MAX_ENEMY_HP, fallback.hp, "enemy.hp(" + id + ")");
            enemy.speed = clampFloat(enemy.speed, MIN_ENEMY_SPEED, MAX_ENEMY_SPEED, fallback.speed, "enemy.speed(" + id + ")");
            enemy.reward = clampInt(enemy.reward, MIN_ENEMY_REWARD, MAX_ENEMY_REWARD, fallback.reward, "enemy.reward(" + id + ")");
        }
    }

    private static void sanitizeTowers(TowersConfigDto towers) {
        Set<String> usedIds = new HashSet<>();
        for (int i = 0; i < towers.towers.size(); i++) {
            TowerConfigDto tower = towers.towers.get(i);
            TowerConfigDto fallback = defaultTowers().towers.get(0);
            String id = normalizeId(tower.id, "tower", i);
            if (usedIds.contains(id)) {
                String uniqueId = id + "_" + (i + 1);
                warn("Duplicate tower id '" + id + "', renamed to '" + uniqueId + "'");
                id = uniqueId;
            }
            usedIds.add(id);
            tower.id = id;
            tower.cost = clampInt(tower.cost, MIN_TOWER_COST, MAX_TOWER_COST, fallback.cost, "tower.cost(" + id + ")");
            tower.range = clampFloat(tower.range, MIN_TOWER_RANGE, MAX_TOWER_RANGE, fallback.range, "tower.range(" + id + ")");
            tower.damage = clampInt(tower.damage, MIN_TOWER_DAMAGE, MAX_TOWER_DAMAGE, fallback.damage, "tower.damage(" + id + ")");
            tower.fireRateSec = clampFloat(tower.fireRateSec, MIN_TOWER_FIRE_RATE_SEC, MAX_TOWER_FIRE_RATE_SEC, fallback.fireRateSec, "tower.fireRateSec(" + id + ")");
        }
    }

    private static void sanitizeWaves(WavesConfigDto waves, EnemiesConfigDto enemies) {
        Set<String> enemyIds = new HashSet<>();
        for (EnemyConfigDto enemy : enemies.enemies) {
            enemyIds.add(enemy.id);
        }
        String defaultEnemyId = enemies.enemies.get(0).id;
        for (int i = 0; i < waves.waves.size(); i++) {
            WaveEntryDto wave = waves.waves.get(i);
            WaveEntryDto fallback = defaultWaves().waves.get(0);
            String enemyId = normalizeId(wave.enemyId, "wave_enemy", i);
            if (!enemyIds.contains(enemyId)) {
                warn("wave.enemyId('" + enemyId + "') does not exist, fallback to '" + defaultEnemyId + "'");
                wave.enemyId = defaultEnemyId;
            } else {
                wave.enemyId = enemyId;
            }
            wave.count = clampInt(wave.count, MIN_WAVE_COUNT, MAX_WAVE_COUNT, fallback.count, "wave.count(index=" + i + ")");
            wave.spawnIntervalSec = clampFloat(
                wave.spawnIntervalSec,
                MIN_WAVE_SPAWN_INTERVAL_SEC,
                MAX_WAVE_SPAWN_INTERVAL_SEC,
                fallback.spawnIntervalSec,
                "wave.spawnIntervalSec(index=" + i + ")"
            );
        }
    }

    private static void sanitizeEconomy(EconomyConfigDto economy) {
        EconomyConfigDto fallback = defaultEconomy();
        economy.startingGold = clampInt(
            economy.startingGold,
            MIN_STARTING_GOLD,
            MAX_STARTING_GOLD,
            fallback.startingGold,
            "economy.startingGold"
        );
        economy.killRewardMultiplier = clampFloat(
            economy.killRewardMultiplier,
            MIN_KILL_REWARD_MULTIPLIER,
            MAX_KILL_REWARD_MULTIPLIER,
            fallback.killRewardMultiplier,
            "economy.killRewardMultiplier"
        );
        if (economy.buildRefundRate < 0f || economy.buildRefundRate > 1f) {
            warn("economy.buildRefundRate out of range [0..1], fallback to " + fallback.buildRefundRate);
            economy.buildRefundRate = fallback.buildRefundRate;
        }
    }

    private static String normalizeId(String rawId, String fallbackPrefix, int index) {
        String trimmed = rawId == null ? "" : rawId.trim();
        if (!trimmed.isEmpty()) {
            return trimmed;
        }
        String fallback = fallbackPrefix + "_" + (index + 1);
        warn("Empty id found, fallback to '" + fallback + "'");
        return fallback;
    }

    private static int clampInt(int value, int min, int max, int fallback, String fieldName) {
        if (value < min || value > max) {
            int clamped = Math.max(min, Math.min(max, fallback));
            warn(fieldName + " out of range [" + min + ".." + max + "], fallback to " + clamped);
            return clamped;
        }
        return value;
    }

    private static float clampFloat(float value, float min, float max, float fallback, String fieldName) {
        if (Float.isNaN(value) || Float.isInfinite(value) || value < min || value > max) {
            float clamped = Math.max(min, Math.min(max, fallback));
            warn(fieldName + " out of range [" + min + ".." + max + "], fallback to " + clamped);
            return clamped;
        }
        return value;
    }

    private static void warn(String message) {
        if (Gdx.app != null) {
            Gdx.app.log(TAG, message);
        }
    }

    private static EnemiesConfigDto defaultEnemies() {
        EnemiesConfigDto dto = new EnemiesConfigDto();
        EnemyConfigDto grunt = new EnemyConfigDto();
        grunt.id = "grunt";
        grunt.hp = 100;
        grunt.speed = 1.0f;
        grunt.reward = 10;
        dto.enemies.add(grunt);
        return dto;
    }

    private static TowersConfigDto defaultTowers() {
        TowersConfigDto dto = new TowersConfigDto();
        TowerConfigDto basic = new TowerConfigDto();
        basic.id = "basic_tower";
        basic.cost = 75;
        basic.range = 2.8f;
        basic.damage = 15;
        basic.fireRateSec = 1.0f;
        dto.towers.add(basic);
        return dto;
    }

    private static WavesConfigDto defaultWaves() {
        WavesConfigDto dto = new WavesConfigDto();
        WaveEntryDto wave = new WaveEntryDto();
        wave.enemyId = "grunt";
        wave.count = 10;
        wave.spawnIntervalSec = 1.25f;
        dto.waves.add(wave);
        return dto;
    }

    private static EconomyConfigDto defaultEconomy() {
        EconomyConfigDto dto = new EconomyConfigDto();
        dto.startingGold = 150;
        dto.killRewardMultiplier = 1.0f;
        dto.buildRefundRate = 0.5f;
        return dto;
    }
}
