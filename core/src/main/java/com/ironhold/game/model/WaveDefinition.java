package com.ironhold.game.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Runtime wave: one or more enemy spawn groups (mixed composition).
 */
public final class WaveDefinition {

    private final List<WaveSpawnGroup> groups;

    public WaveDefinition(List<WaveSpawnGroup> groups) {
        if (groups == null || groups.isEmpty()) {
            throw new IllegalArgumentException("groups must not be empty");
        }
        this.groups = List.copyOf(groups);
    }

    public WaveDefinition(String enemyId, int count, float spawnIntervalSec) {
        this(Collections.singletonList(new WaveSpawnGroup(enemyId, count, spawnIntervalSec)));
    }

    public List<WaveSpawnGroup> getGroups() {
        return groups;
    }

    public int getTotalCount() {
        int total = 0;
        for (WaveSpawnGroup group : groups) {
            total += group.getCount();
        }
        return total;
    }
}
