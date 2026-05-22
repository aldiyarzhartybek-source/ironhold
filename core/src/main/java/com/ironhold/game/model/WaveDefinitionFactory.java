package com.ironhold.game.model;

import com.ironhold.config.dto.WaveEntryDto;
import com.ironhold.config.dto.WaveSpawnEntryDto;
import com.ironhold.config.dto.WavesConfigDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Factory for wave spawn definitions loaded from per-level JSON config.
 */
public final class WaveDefinitionFactory {

    private WaveDefinitionFactory() {
    }

    public static WaveDefinition fromEntry(WaveEntryDto entry) {
        Objects.requireNonNull(entry, "entry");
        if (entry.spawns != null && !entry.spawns.isEmpty()) {
            List<WaveSpawnGroup> groups = new ArrayList<>();
            for (WaveSpawnEntryDto spawn : entry.spawns) {
                if (spawn == null || spawn.enemyId == null || spawn.enemyId.isBlank()) {
                    continue;
                }
                groups.add(new WaveSpawnGroup(
                    spawn.enemyId.trim(),
                    spawn.count,
                    spawn.spawnIntervalSec
                ));
            }
            if (!groups.isEmpty()) {
                return new WaveDefinition(groups);
            }
        }
        return new WaveDefinition(
            entry.enemyId,
            entry.count,
            entry.spawnIntervalSec
        );
    }

    public static List<WaveDefinition> fromLevelWaves(WavesConfigDto wavesConfig) {
        Objects.requireNonNull(wavesConfig, "wavesConfig");
        List<WaveDefinition> result = new ArrayList<>(wavesConfig.waves.size());
        for (WaveEntryDto entry : wavesConfig.waves) {
            result.add(fromEntry(entry));
        }
        return result;
    }
}
