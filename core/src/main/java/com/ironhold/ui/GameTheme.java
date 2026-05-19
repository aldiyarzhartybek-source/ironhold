package com.ironhold.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;

/**
 * Single source of truth for IronHold palette and shared draw constants.
 *
 * <p>Reference palette (Stage 5): dark background, teal path, orange enemies,
 * blue towers, gold economy accents, red danger. Minor tuning vs. hex values is
 * documented on derived shades ({@link #PATH_BODY}, backdrop layers).
 */
public final class GameTheme {

    // --- Core palette (hex from design) ---
    public static final Color BACKGROUND = hex("0E1018");
    public static final Color PATH_TEAL = hex("2EC4B6");
    public static final Color ENEMY_ACCENT = hex("F77F2A");
    public static final Color TOWER_BLUE = hex("4A7CFF");
    public static final Color GOLD = hex("F5C542");
    public static final Color DANGER_RED = hex("E84855");

    // --- UI (Scene2D / menus) ---
    public static final Color UI_TEXT = hex("EBF0F8");
    public static final Color UI_TEXT_MUTED = hex("9EA8BD");
    public static final Color BUTTON_UP = hex("333347");
    public static final Color BUTTON_OVER = hex("47475F");
    public static final Color BUTTON_DOWN = hex("292933");

    // --- Gameplay derived ---
    /** Darker teal fill for path bed (slightly below PATH_TEAL luminance). */
    public static final Color PATH_BODY = hex("1F8A80");
    /** Lane markers / path shimmer on top of {@link #PATH_BODY}. */
    public static final Color PATH_LANE = withAlpha(PATH_TEAL, 0.45f);
    /** Wider path underlay for depth. */
    public static final Color PATH_OUTER = withAlpha(PATH_TEAL, 0.28f);
    public static final Color BACKDROP_BASE = withAlpha(BACKGROUND, 1f);
    public static final Color BACKDROP_TOP_GLOW = hex("1C2420");
    public static final Color BACKDROP_FRAME = withAlpha(PATH_TEAL, 0.12f);

    public static final Color SLOT_FREE = withAlpha(PATH_TEAL, 0.55f);
    public static final Color SLOT_CORE = hex("0F141C");
    public static final Color SLOT_OCCUPIED = withAlpha(TOWER_BLUE, 0.85f);

    public static final Color HP_BAR_BACKGROUND = withAlpha(BACKGROUND, 0.9f);
    public static final Color HP_BAR_FILL = withAlpha(PATH_TEAL, 0.95f);
    public static final Color PROGRESS_BAR_BACKGROUND = withAlpha(BACKGROUND, 0.75f);
    public static final Color PROGRESS_BAR_FILL = withAlpha(GOLD, 0.95f);

    public static final Color PROJECTILE = withAlpha(GOLD, 1f);
    public static final Color HIT_EFFECT = withAlpha(ENEMY_ACCENT, 1f);
    public static final Color REWARD_FLOAT = withAlpha(PATH_TEAL, 1f);

    public static final Color SPAWN_MARKER = withAlpha(GOLD, 0.95f);
    public static final Color BASE_MARKER = withAlpha(DANGER_RED, 0.95f);

    public static final Color BANNER_BACKGROUND = withAlpha(BACKGROUND, 0.65f);
    public static final Color BANNER_TEXT = withAlpha(UI_TEXT, 1f);
    public static final Color TOAST_ERROR_BACKGROUND = withAlpha(DANGER_RED, 0.35f);
    public static final Color TOAST_ERROR_TEXT = hex("FFC2C2");
    public static final Color TOAST_SUCCESS_BACKGROUND = withAlpha(PATH_TEAL, 0.25f);
    public static final Color TOAST_SUCCESS_TEXT = hex("D2FFF0");

    /** White pixel tint for textured quads and font reset. */
    public static final Color TINT_WHITE = Color.WHITE;
    /** Neutral fill for procedurally generated 1x1-style textures. */
    public static final Color TEXTURE_NEUTRAL = UI_TEXT;

    private GameTheme() {
    }

    public static void clearBackground() {
        Gdx.gl.glClearColor(BACKGROUND.r, BACKGROUND.g, BACKGROUND.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    public static Color withAlpha(Color source, float alpha) {
        Color copy = new Color(source);
        copy.a = alpha;
        return copy;
    }

    public static Color multiplyAlpha(Color source, float alphaMultiplier) {
        Color copy = new Color(source);
        copy.a *= alphaMultiplier;
        return copy;
    }

    private static Color hex(String hexRgb) {
        return Color.valueOf(hexRgb);
    }

    /**
     * Shared layout numbers for world drawing (Stage 5+).
     */
    public static final class Draw {
        public static final float ROAD_WIDTH = 42f;
        public static final float PATH_OUTER_HALF_WIDTH = 24f;
        public static final float PATH_INNER_HALF_WIDTH = 18f;
        public static final int CIRCLE_SEGMENTS = 24;
        public static final float ROAD_MARKER_STEP = 24f;
        /** Base radius for geometric enemy shapes (diameter ~= ENEMY_SIZE). */
        public static final float ENEMY_RADIUS = 10f;
        public static final float ENEMY_SIZE = ENEMY_RADIUS * 2f;
        public static final float ENEMY_HP_BAR_WIDTH = 20f;
        public static final float ENEMY_HP_BAR_HEIGHT = 3f;
        public static final float ENEMY_PROGRESS_BAR_WIDTH = 16f;
        public static final float ENEMY_PROGRESS_BAR_HEIGHT = 2f;
        public static final float TOWER_SIZE = 24f;
        /** Visual ring radius; click radius remains 28f in {@code BuildSystem}. */
        public static final float SLOT_RING_RADIUS = 22f;
        public static final float SLOT_INNER_RADIUS = 12f;
        public static final float SLOT_OCCUPIED_RING_RADIUS = 24f;
        public static final float SPAWN_MARKER_RADIUS = 16f;
        public static final float BASE_MARKER_RADIUS = 18f;

        private Draw() {
        }
    }
}
