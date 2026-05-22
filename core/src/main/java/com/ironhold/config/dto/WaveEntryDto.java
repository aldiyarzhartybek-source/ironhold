package com.ironhold.config.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Single wave entry DTO. Use {@link #spawns} for mixed waves, or legacy single {@link #enemyId}.
 */
public final class WaveEntryDto {
    public String enemyId = "grunt";
    public int count = 10;
    public float spawnIntervalSec = 1.25f;
    /** Mixed wave: several enemy types in sequence within one wave. */
    public List<WaveSpawnEntryDto> spawns = new ArrayList<>();
}
