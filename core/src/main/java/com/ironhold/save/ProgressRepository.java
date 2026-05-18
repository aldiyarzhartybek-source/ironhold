package com.ironhold.save;

/**
 * Repository for loading and persisting {@link PlayerProgress} (GoF Repository).
 */
public interface ProgressRepository {

    PlayerProgress load();

    void save(PlayerProgress progress);
}
