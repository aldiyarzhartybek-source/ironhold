package com.ironhold.core;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.ironhold.game.GameRuntimeView;

import java.util.Objects;

/**
 * Stage HUD renderer for gameplay and debug metrics.
 */
public final class StageHud {

    private static final float LEFT_X = 24f;
    private static final float TOP_MARGIN = 24f;
    private static final float LINE_HEIGHT = 28f;
    private static final float CENTER_X_FACTOR = 0.5f;
    private static final float RIGHT_MARGIN = 220f;

    private final BitmapFont font;
    private int screenWidth;
    private int screenHeight;

    public StageHud(BitmapFont font, int screenWidth, int screenHeight) {
        this.font = Objects.requireNonNull(font, "font");
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void resize(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void render(SpriteBatch batch, GameRuntimeView view) {
        drawMainHud(batch, view);
        drawDebugHud(batch, view);
    }

    private void drawMainHud(SpriteBatch batch, GameRuntimeView view) {
        float topY = screenHeight - TOP_MARGIN;
        var level = view.getLevelState();
        font.draw(batch, "Lives: " + level.getBaseLives(), LEFT_X, topY);
        font.draw(batch, "Wave: " + level.getCurrentWaveNumber() + "/" + level.getTotalWaves(), screenWidth * CENTER_X_FACTOR - 80f, topY);
        font.draw(batch, "Gold: " + view.getGold(), screenWidth - RIGHT_MARGIN, topY);
        font.draw(batch, "Status: " + level.getStatus(), LEFT_X, topY - LINE_HEIGHT);
        font.draw(batch, "Build: " + view.getLastBuildPlacementResult(), screenWidth - RIGHT_MARGIN, topY - LINE_HEIGHT);
    }

    private void drawDebugHud(SpriteBatch batch, GameRuntimeView view) {
        float baseY = screenHeight - (TOP_MARGIN + LINE_HEIGHT * 3f);
        var level = view.getLevelState();
        font.draw(batch, "Spawn timer: " + String.format("%.2f", level.getSpawnTimerSec()), LEFT_X, baseY);
        font.draw(batch, "Wave spawned: " + level.getSpawnedInCurrentWave(), LEFT_X, baseY - LINE_HEIGHT);
        font.draw(batch, "Total spawned: " + level.getTotalSpawnedEnemies(), LEFT_X, baseY - LINE_HEIGHT * 2f);
        font.draw(batch, "Last enemyId: " + level.getLastSpawnedEnemyId(), LEFT_X, baseY - LINE_HEIGHT * 3f);
        font.draw(batch, "Active enemies: " + view.getActiveEnemies().size(), LEFT_X, baseY - LINE_HEIGHT * 4f);
        font.draw(batch, "Escaped enemies: " + level.getEscapedEnemies(), LEFT_X, baseY - LINE_HEIGHT * 5f);
        font.draw(batch, "Placed towers: " + view.getPlacedTowers().size(), LEFT_X, baseY - LINE_HEIGHT * 6f);
        font.draw(batch, "Killed enemies: " + view.getTotalKilledEnemies(), LEFT_X, baseY - LINE_HEIGHT * 7f);
        font.draw(batch, "Last reward: +" + view.getLastAwardedGold() + " (press K to test)", LEFT_X, baseY - LINE_HEIGHT * 8f);
        font.draw(batch, "Event EnemySpawned: " + view.getEnemySpawnedEvents(), LEFT_X, baseY - LINE_HEIGHT * 9f);
        font.draw(batch, "Event EnemyKilled: " + view.getEnemyKilledEvents(), LEFT_X, baseY - LINE_HEIGHT * 10f);
        font.draw(batch, "Event TowerBuilt: " + view.getTowerBuiltEvents(), LEFT_X, baseY - LINE_HEIGHT * 11f);
        font.draw(batch, "Event WaveStarted: " + view.getWaveStartedEvents(), LEFT_X, baseY - LINE_HEIGHT * 12f);
        font.draw(batch, "Event WaveCompleted: " + view.getWaveCompletedEvents(), LEFT_X, baseY - LINE_HEIGHT * 13f);
    }
}
