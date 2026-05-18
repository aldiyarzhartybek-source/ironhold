package com.ironhold.save;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON persistence shape for {@code saves/progress.json}.
 */
public final class ProgressSaveDto {

    public int schemaVersion;
    public int highestUnlockedLevel;
    public List<Integer> completedLevels;

    public ProgressSaveDto() {
        completedLevels = new ArrayList<>();
    }
}
