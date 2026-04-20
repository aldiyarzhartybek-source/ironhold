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
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Aggregates balance configs loaded from JSON files into DTOs.
 */
public final class GameConfig {

    private static final String ENEMIES_PATH = "config/enemies.json";
    private static final String TOWERS_PATH = "config/towers.json";
    private static final String WAVES_PATH = "config/waves.json";
    private static final String ECONOMY_PATH = "config/economy.json";

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
        if (economy.startingGold < 0) {
            economy.startingGold = 150;
        }
        if (economy.killRewardMultiplier <= 0f) {
            economy.killRewardMultiplier = 1f;
        }
        if (economy.buildRefundRate < 0f || economy.buildRefundRate > 1f) {
            economy.buildRefundRate = 0.5f;
        }

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
