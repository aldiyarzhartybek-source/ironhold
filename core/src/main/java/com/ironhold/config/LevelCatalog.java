package com.ironhold.config;

import com.badlogic.gdx.utils.Json;
import com.ironhold.config.dto.LevelConfigDto;
import com.ironhold.game.model.LevelDefinition;
import com.ironhold.game.model.WaveDefinition;
import com.ironhold.game.model.WaveDefinitionFactory;
import com.ironhold.save.ProgressService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Factory for per-level session definitions (map, starting gold, waves).
 */
public final class LevelCatalog {

    private static final String LEVEL_CONFIG_PATH_FORMAT = "config/levels/level_%d.json";

    private final Map<Integer, LevelDefinition> levelsByNumber;

    private LevelCatalog(Map<Integer, LevelDefinition> levelsByNumber) {
        this.levelsByNumber = Map.copyOf(levelsByNumber);
    }

    public static LevelCatalog load(GameConfig gameConfig) {
        Objects.requireNonNull(gameConfig, "gameConfig");
        Map<Integer, LevelDefinition> levels = new LinkedHashMap<>();
        for (int levelNumber = 1; levelNumber <= ProgressService.MAX_LEVELS; levelNumber++) {
            levels.put(levelNumber, resolveLevel(levelNumber, gameConfig));
        }
        return new LevelCatalog(levels);
    }

    public LevelDefinition getLevel(int levelNumber) {
        return levelsByNumber.get(levelNumber);
    }

    public int getLevelCount() {
        return levelsByNumber.size();
    }

    private static LevelDefinition resolveLevel(int levelNumber, GameConfig gameConfig) {
        LevelConfigDto dto = loadLevelDto(levelNumber);
        if (dto.levelNumber <= 0) {
            dto.levelNumber = levelNumber;
        }
        if (dto.levelId == null || dto.levelId.isBlank()) {
            dto.levelId = "level_" + levelNumber;
        }
        if (dto.map == null || dto.map.isBlank()) {
            dto.map = "maps/level0.tmx";
        }
        if (dto.wavesConfig == null || dto.wavesConfig.isBlank()) {
            dto.wavesConfig = "config/waves.json";
        }
        int startingGold = gameConfig.clampStartingGold(dto.startingGold, levelNumber);
        List<WaveDefinition> waves = WaveDefinitionFactory.fromLevelWaves(
            gameConfig.loadWavesConfig(dto.wavesConfig)
        );
        return new LevelDefinition(levelNumber, dto.levelId.trim(), dto.map.trim(), startingGold, waves);
    }

    private static LevelConfigDto loadLevelDto(int levelNumber) {
        Json json = new Json();
        String path = String.format(LEVEL_CONFIG_PATH_FORMAT, levelNumber);
        LevelConfigDto parsed = GameConfig.readInternalJson(json, path, LevelConfigDto.class);
        if (parsed != null) {
            return parsed;
        }
        LevelConfigDto fallback = new LevelConfigDto();
        fallback.levelNumber = levelNumber;
        fallback.levelId = "level_" + levelNumber;
        return fallback;
    }
}
