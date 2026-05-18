package com.ironhold.config.dto;

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
}
