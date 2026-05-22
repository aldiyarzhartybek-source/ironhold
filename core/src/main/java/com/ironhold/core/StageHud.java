package com.ironhold.core;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.ironhold.game.GameRuntimeView;
import com.ironhold.ui.GameTheme;

import java.util.Objects;

/**
 * In-game HUD.
 *
 * <p>Production row: Lives (left) | Wave (centre) | Gold / Time / Speed (right).
 * <p>Debug block: shown only when {@code debugMode=true} — game state metrics,
 * event counters, spawn timers.
 */
public final class StageHud {

    private static final float LEFT_X      = 24f;
    private static final float TOP_MARGIN  = 24f;
    private static final float LINE_H      = 28f;
    private static final float RIGHT_COL   = 220f; // distance from right edge

    private final BitmapFont font;
    private int screenWidth;
    private int screenHeight;

    public StageHud(BitmapFont font, int screenWidth, int screenHeight) {
        this.font = Objects.requireNonNull(font, "font");
        this.screenWidth  = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void resize(int w, int h) {
        this.screenWidth  = w;
        this.screenHeight = h;
    }

    public void render(SpriteBatch batch, GameRuntimeView view, boolean debugMode) {
        font.setColor(GameTheme.UI_TEXT);
        drawProductionHud(batch, view);
        if (debugMode) {
            drawDebugHud(batch, view);
        }
    }

    // ── Production HUD ────────────────────────────────────────────────────

    private void drawProductionHud(SpriteBatch batch, GameRuntimeView view) {
        float topY  = screenHeight - TOP_MARGIN;
        float rightX = screenWidth - RIGHT_COL;
        var level   = view.getLevelState();

        // Left column
        font.draw(batch, "Lives: " + level.getBaseLives(), LEFT_X, topY);

        // Centre
        font.draw(batch,
            "Wave: " + level.getCurrentWaveNumber() + " / " + level.getTotalWaves(),
            screenWidth * 0.5f - 60f, topY);

        // Right column
        font.draw(batch, "Gold: "  + view.getGold(),                          rightX, topY);
        font.draw(batch, "Time: "  + view.getElapsedLevelTimeFormatted(),      rightX, topY - LINE_H);
        font.draw(batch, "Speed: x" + speedLabel(view.getTimeScale()),         rightX, topY - LINE_H * 2f);
    }

    // ── Debug HUD (debugMode only) ────────────────────────────────────────

    private void drawDebugHud(SpriteBatch batch, GameRuntimeView view) {
        float topY   = screenHeight - TOP_MARGIN;
        float baseY  = topY - LINE_H * 3f;
        var level    = view.getLevelState();

        // Debug additions to main row (left column rows 2-4)
        font.setColor(GameTheme.UI_TEXT_MUTED);
        font.draw(batch, "Mode: "   + view.getGameMode(),                LEFT_X, topY - LINE_H);
        font.draw(batch, "Status: " + level.getStatus(),                 LEFT_X, topY - LINE_H * 2f);
        font.draw(batch, "Build: "  + view.getLastBuildPlacementResult(),LEFT_X, topY - LINE_H * 3f);

        // Spawn / wave metrics
        font.draw(batch, "Spawn timer: "
            + String.format("%.2f", level.getSpawnTimerSec())
            + " / "
            + String.format("%.2f", level.getActiveSpawnIntervalSec()),  LEFT_X, baseY - LINE_H * 2f);
        font.draw(batch, "Wave spawned: "     + level.getSpawnedInCurrentWave(),    LEFT_X, baseY - LINE_H * 3f);
        font.draw(batch, "Total spawned: "    + level.getTotalSpawnedEnemies(),     LEFT_X, baseY - LINE_H * 4f);
        font.draw(batch, "Last enemyId: "     + level.getLastSpawnedEnemyId(),      LEFT_X, baseY - LINE_H * 5f);
        font.draw(batch, "Active enemies: "   + view.getActiveEnemies().size(),     LEFT_X, baseY - LINE_H * 6f);
        font.draw(batch, "Escaped enemies: "  + level.getEscapedEnemies(),          LEFT_X, baseY - LINE_H * 7f);
        font.draw(batch, "Placed towers: "    + view.getPlacedTowers().size(),      LEFT_X, baseY - LINE_H * 8f);
        font.draw(batch, "Kills: "            + view.getTotalKilledEnemies(),       LEFT_X, baseY - LINE_H * 9f);
        font.draw(batch, "Gold spent: "       + view.getTotalGoldSpent(),           LEFT_X, baseY - LINE_H * 10f);
        font.draw(batch, "Last reward: +"     + view.getLastAwardedGold(),          LEFT_X, baseY - LINE_H * 11f);

        // Event counters
        font.draw(batch, "Ev EnemySpawned: "  + view.getEnemySpawnedEvents(),       LEFT_X, baseY - LINE_H * 12f);
        font.draw(batch, "Ev EnemyKilled: "   + view.getEnemyKilledEvents(),        LEFT_X, baseY - LINE_H * 13f);
        font.draw(batch, "Ev TowerBuilt: "    + view.getTowerBuiltEvents(),         LEFT_X, baseY - LINE_H * 14f);
        font.draw(batch, "Ev WaveStarted: "   + view.getWaveStartedEvents(),        LEFT_X, baseY - LINE_H * 15f);
        font.draw(batch, "Ev WaveCompleted: " + view.getWaveCompletedEvents(),      LEFT_X, baseY - LINE_H * 16f);

        font.setColor(GameTheme.UI_TEXT);
    }

    private static String speedLabel(float timeScale) {
        return timeScale >= 1.95f ? "2" : "1";
    }
}
