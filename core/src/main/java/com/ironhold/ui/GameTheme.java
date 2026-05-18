package com.ironhold.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;

/**
 * Shared palette for UI and gameplay drawing (Stage 5 will extend usage).
 */
public final class GameTheme {

    public static final Color BACKGROUND = Color.valueOf("0E1018");
    public static final Color PATH_TEAL = Color.valueOf("2EC4B6");
    public static final Color UI_TEXT = new Color(0.92f, 0.94f, 0.97f, 1f);
    public static final Color UI_TEXT_MUTED = new Color(0.62f, 0.66f, 0.74f, 1f);
    public static final Color BUTTON_UP = new Color(0.2f, 0.2f, 0.28f, 1f);
    public static final Color BUTTON_OVER = new Color(0.28f, 0.28f, 0.38f, 1f);
    public static final Color BUTTON_DOWN = new Color(0.16f, 0.16f, 0.22f, 1f);

    private GameTheme() {
    }

    public static void clearBackground() {
        Gdx.gl.glClearColor(BACKGROUND.r, BACKGROUND.g, BACKGROUND.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
}
