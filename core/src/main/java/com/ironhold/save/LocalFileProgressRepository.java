package com.ironhold.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

import java.util.Objects;

/**
 * Persists progress to {@code Gdx.files.local("saves/progress.json")}.
 */
public final class LocalFileProgressRepository implements ProgressRepository {

    private static final String TAG = "ProgressRepository";
    static final String SAVE_PATH = "saves/progress.json";

    private final Json json;

    public LocalFileProgressRepository() {
        this(new Json());
    }

    LocalFileProgressRepository(Json json) {
        this.json = Objects.requireNonNull(json, "json");
    }

    @Override
    public PlayerProgress load() {
        FileHandle file = Gdx.files.local(SAVE_PATH);
        if (!file.exists()) {
            return PlayerProgress.defaultProgress();
        }
        try {
            ProgressSaveDto dto = json.fromJson(ProgressSaveDto.class, file);
            if (!isValidDto(dto)) {
                log("Invalid or unsupported progress file, using defaults");
                return PlayerProgress.defaultProgress();
            }
            return PlayerProgress.fromDto(dto);
        } catch (Exception e) {
            log("Failed to read progress: " + e.getMessage());
            return PlayerProgress.defaultProgress();
        }
    }

    @Override
    public void save(PlayerProgress progress) {
        Objects.requireNonNull(progress, "progress");
        try {
            FileHandle file = Gdx.files.local(SAVE_PATH);
            FileHandle parent = file.parent();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            ProgressSaveDto dto = progress.toDto();
            file.writeString(json.prettyPrint(dto), false, "UTF-8");
        } catch (Exception e) {
            log("Failed to write progress: " + e.getMessage());
        }
    }

    private static boolean isValidDto(ProgressSaveDto dto) {
        if (dto == null) {
            return false;
        }
        if (dto.schemaVersion != PlayerProgress.SCHEMA_VERSION) {
            return false;
        }
        if (dto.highestUnlockedLevel < 1 || dto.highestUnlockedLevel > ProgressService.MAX_LEVELS) {
            return false;
        }
        if (dto.completedLevels == null) {
            return false;
        }
        for (Integer level : dto.completedLevels) {
            if (level == null || level < 1 || level > ProgressService.MAX_LEVELS) {
                return false;
            }
        }
        return true;
    }

    private static void log(String message) {
        if (Gdx.app != null) {
            Gdx.app.log(TAG, message);
        }
    }
}
