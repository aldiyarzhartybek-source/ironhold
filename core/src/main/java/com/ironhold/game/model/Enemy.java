package com.ironhold.game.model;

import com.badlogic.gdx.graphics.Color;
import com.ironhold.ui.GameTheme;

/**
 * Runtime enemy template (balance + visual definition).
 */
public final class Enemy {

    private final String id;
    private final int maxHp;
    private final int currentHp;
    private final float speed;
    private final int reward;
    private final EnemyVisualShape visualShape;
    private final Color fillColor;
    private final float visualScale;

    public Enemy(
        String id,
        int maxHp,
        int currentHp,
        float speed,
        int reward,
        EnemyVisualShape visualShape,
        Color fillColor,
        float visualScale
    ) {
        this.id = id;
        this.maxHp = maxHp;
        this.currentHp = currentHp;
        this.speed = speed;
        this.reward = reward;
        this.visualShape = visualShape != null ? visualShape : EnemyVisualShape.SQUARE;
        this.fillColor = fillColor != null ? new Color(fillColor) : new Color(GameTheme.ENEMY_ACCENT);
        this.visualScale = visualScale > 0f ? visualScale : 1f;
    }

    public static Enemy defaultFallback() {
        return new Enemy(
            "unknown",
            100,
            100,
            1f,
            10,
            EnemyVisualShape.SQUARE,
            new Color(GameTheme.ENEMY_ACCENT),
            1f
        );
    }

    public String getId() {
        return id;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public float getSpeed() {
        return speed;
    }

    public int getReward() {
        return reward;
    }

    public EnemyVisualShape getVisualShape() {
        return visualShape;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public float getVisualScale() {
        return visualScale;
    }

    public float getVisualRadius() {
        return GameTheme.Draw.ENEMY_RADIUS * visualScale;
    }
}
