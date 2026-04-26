package com.ironhold.core;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.ironhold.game.GameFacade;
import com.ironhold.level.RuntimeLevelState;

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
    private final GameFacade game;
    private int screenWidth;
    private int screenHeight;

    public StageHud(BitmapFont font, GameFacade game, int screenWidth, int screenHeight) {
        this.font = Objects.requireNonNull(font, "font");
        this.game = Objects.requireNonNull(game, "game");
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void resize(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void render(SpriteBatch batch, RuntimeLevelState level) {
        drawMainHud(batch, level);
        drawDebugHud(batch, level);
    }

    private void drawMainHud(SpriteBatch batch, RuntimeLevelState level) {
        float topY = screenHeight - TOP_MARGIN;
        font.draw(batch, "Lives: " + level.getBaseLives(), LEFT_X, topY);
        font.draw(batch, "Wave: " + level.getCurrentWaveNumber() + "/" + level.getTotalWaves(), screenWidth * CENTER_X_FACTOR - 80f, topY);
        font.draw(batch, "Gold: " + game.getEconomy().getGold(), screenWidth - RIGHT_MARGIN, topY);
        font.draw(batch, "Status: " + level.getStatus(), LEFT_X, topY - LINE_HEIGHT);
        font.draw(batch, "Build: " + game.getLastBuildPlacementResult(), screenWidth - RIGHT_MARGIN, topY - LINE_HEIGHT);
    }

    private void drawDebugHud(SpriteBatch batch, RuntimeLevelState level) {
        float baseY = screenHeight - (TOP_MARGIN + LINE_HEIGHT * 3f);
        font.draw(batch, "Spawn timer: " + String.format("%.2f", level.getSpawnTimerSec()), LEFT_X, baseY);
        font.draw(batch, "Wave spawned: " + level.getSpawnedInCurrentWave(), LEFT_X, baseY - LINE_HEIGHT);
        font.draw(batch, "Total spawned: " + level.getTotalSpawnedEnemies(), LEFT_X, baseY - LINE_HEIGHT * 2f);
        font.draw(batch, "Last enemyId: " + level.getLastSpawnedEnemyId(), LEFT_X, baseY - LINE_HEIGHT * 3f);
        font.draw(batch, "Active enemies: " + game.getActiveEnemies().size(), LEFT_X, baseY - LINE_HEIGHT * 4f);
        font.draw(batch, "Escaped enemies: " + level.getEscapedEnemies(), LEFT_X, baseY - LINE_HEIGHT * 5f);
        font.draw(batch, "Placed towers: " + game.getPlacedTowers().size(), LEFT_X, baseY - LINE_HEIGHT * 6f);
        font.draw(batch, "Killed enemies: " + game.getTotalKilledEnemies(), LEFT_X, baseY - LINE_HEIGHT * 7f);
        font.draw(batch, "Last reward: +" + game.getLastAwardedGold() + " (press K to test)", LEFT_X, baseY - LINE_HEIGHT * 8f);
        font.draw(batch, "Event EnemySpawned: " + game.getEventTracker().getEnemySpawnedEvents(), LEFT_X, baseY - LINE_HEIGHT * 9f);
        font.draw(batch, "Event EnemyKilled: " + game.getEventTracker().getEnemyKilledEvents(), LEFT_X, baseY - LINE_HEIGHT * 10f);
        font.draw(batch, "Event TowerBuilt: " + game.getEventTracker().getTowerBuiltEvents(), LEFT_X, baseY - LINE_HEIGHT * 11f);
        font.draw(batch, "Event WaveStarted: " + game.getEventTracker().getWaveStartedEvents(), LEFT_X, baseY - LINE_HEIGHT * 12f);
        font.draw(batch, "Event WaveCompleted: " + game.getEventTracker().getWaveCompletedEvents(), LEFT_X, baseY - LINE_HEIGHT * 13f);
    }
}
