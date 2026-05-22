package com.ironhold.config.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-level session descriptor loaded from {@code config/levels/level_N.json}.
 */
public final class LevelConfigDto {
    public String levelId = "level_1";
    public int levelNumber = 1;
    public String map = "maps/level0.tmx";
    public int startingGold = 150;
    /** Internal path to a waves JSON file (e.g. {@code config/waves.json}). */
    public String wavesConfig = "config/waves.json";
    /** Enemy waypoints from spawn to base (at least 2 points). */
    public List<Vec2Dto> enemyPath = new ArrayList<>();
    /** Tower build positions on this level. */
    public List<BuildSlotDto> buildSlots = new ArrayList<>();
}
