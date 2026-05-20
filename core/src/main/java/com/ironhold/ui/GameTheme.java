package com.ironhold.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;

/**
 * Single source of truth for IronHold palette and shared draw constants.
 */
public final class GameTheme {

    // --- Core palette ---
    public static final Color BACKGROUND   = hex("0A0A21");
    public static final Color PATH_TEAL    = hex("1ECFBF");
    public static final Color ENEMY_ACCENT = hex("F77F2A");
    public static final Color GOLD         = hex("F5C542");
    public static final Color DANGER_RED   = hex("E84855");

    // --- UI (Scene2D / menus) ---
    public static final Color UI_TEXT       = hex("EBF0F8");
    public static final Color UI_TEXT_MUTED = hex("9EA8BD");
    public static final Color BUTTON_UP     = hex("333347");
    public static final Color BUTTON_OVER   = hex("47475F");
    public static final Color BUTTON_DOWN   = hex("292933");

    // --- Wall / groove / slot ---
    public static final Color WALL_COLOR        = new Color(0.08f, 0.08f, 0.20f, 1f);
    public static final Color GROOVE_COLOR      = new Color(0.62f, 0.65f, 0.82f, 1f);
    public static final Color SLOT_RECESS       = new Color(0.20f, 0.22f, 0.42f, 1f);
    public static final Color SLOT_RECESS_INNER = new Color(0.10f, 0.11f, 0.24f, 1f);

    // --- Shadow ---

    // --- Tower ---
    public static final Color TOWER_OUTLINE = Color.WHITE;
    /** Pulsing core in the middle of the base — alive feeling between shots. */
    public static final Color TOWER_CORE_BASE      = withAlpha(hex("1ECFBF"), 0.95f);
    public static final Color TOWER_CORE_HIGHLIGHT = withAlpha(Color.WHITE, 0.85f);

    // --- Enemy ---
    public static final Color ENEMY_OUTLINE = withAlpha(Color.WHITE, 0.90f);
    // --- Projectile (energy beam + trail) ---
    /** Hot bright core of the beam. */
    public static final Color PROJECTILE_CORE     = Color.WHITE;
    /** Outer halo around the beam core — same teal as the path. */
    public static final Color PROJECTILE_HALO     = withAlpha(hex("8FF5E8"), 0.85f);
    /** Trail tint — fades from the halo colour down to transparent. */
    public static final Color PROJECTILE_TRAIL    = withAlpha(hex("8FF5E8"), 0.55f);

    // --- Mortar tower ---
    public static final Color MORTAR_OUTLINE   = Color.WHITE;
    public static final Color MORTAR_CORE      = hex("8B1538");
    public static final Color MORTAR_CORE_GLOW = new Color(0.55f, 0.12f, 0.22f, 0.9f);
    public static final Color MORTAR_SHELL     = new Color(0.70f, 0.18f, 0.22f, 1f);
    public static final Color MORTAR_SHELL_GLOW = new Color(0.90f, 0.35f, 0.25f, 0.45f);
    public static final Color MORTAR_BLAST_RING = new Color(1.00f, 0.45f, 0.20f, 0.55f);
    public static final Color MORTAR_BLAST_CORE = new Color(0.04f, 0.04f, 0.13f, 0.9f);

    // --- Lightning tower ---
    /** Bright neon-violet diamond outline and core. */
    public static final Color LIGHTNING_OUTLINE = hex("CC44FF");
    public static final Color LIGHTNING_CORE    = new Color(0.85f, 0.95f, 1.00f, 1.00f);
    /** Wide outer glow drawn around the bolt. */
    public static final Color LIGHTNING_GLOW    = new Color(0.55f, 0.20f, 1.00f, 0.55f);

    // --- Hit effect (radial burst on impact) ---
    public static final Color HIT_BURST_LINE = withAlpha(Color.WHITE, 0.9f);
    public static final Color HIT_BURST_RING = withAlpha(hex("8FF5E8"), 0.6f);

    // --- Backdrop (GameScreen drawVisualBackdrop) ---
    public static final Color BACKDROP_BASE     = withAlpha(BACKGROUND, 1f);
    public static final Color BACKDROP_TOP_GLOW = hex("0C0C24");
    public static final Color BACKDROP_FRAME    = withAlpha(PATH_TEAL, 0.12f);

    // --- Markers ---
    public static final Color SPAWN_MARKER = withAlpha(GOLD, 0.95f);
    public static final Color BASE_MARKER  = withAlpha(DANGER_RED, 0.95f);

    // --- UI overlays ---
    public static final Color REWARD_FLOAT             = withAlpha(PATH_TEAL, 1f);
    public static final Color BANNER_BACKGROUND        = withAlpha(BACKGROUND, 0.65f);
    public static final Color BANNER_TEXT              = withAlpha(UI_TEXT, 1f);
    public static final Color TOAST_ERROR_BACKGROUND   = withAlpha(DANGER_RED, 0.35f);
    public static final Color TOAST_ERROR_TEXT         = hex("FFC2C2");
    public static final Color TOAST_SUCCESS_BACKGROUND = withAlpha(PATH_TEAL, 0.25f);
    public static final Color TOAST_SUCCESS_TEXT       = hex("D2FFF0");

    public static final Color TINT_WHITE      = Color.WHITE;
    public static final Color TEXTURE_NEUTRAL = UI_TEXT;

    private GameTheme() {}

    public static void clearBackground() {
        Gdx.gl.glClearColor(0.04f, 0.04f, 0.13f, 1f);
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

    /** Shared layout numbers for world drawing. */
    public static final class Draw {
        // Wall / groove geometry
        public static final float GROOVE_HALF_WIDTH = 16f;

        // Tower — rounded hollow square
        public static final float TOWER_SIZE          = 28f;
        public static final float TOWER_OUTLINE_THICK = 4.5f;
        public static final float TOWER_CORNER_R      = 5f;

        /** Pulsing core radius (peak) drawn at the tower centre. */
        public static final float TOWER_CORE_RADIUS   = 3.5f;
        /** Mortar hexagon is drawn slightly larger than the Dart square. */
        public static final float MORTAR_TOWER_SCALE  = 1.14f;

        // Mortar shell + blast
        public static final float MORTAR_SHELL_RADIUS = 9f;

        // Projectile (energy beam)
        /** Length of the bright core of the beam along its direction of travel. */
        public static final float PROJECTILE_BEAM_LEN = 10f;
        /** Half-width of the beam perpendicular to its travel direction. */
        public static final float PROJECTILE_BEAM_HW  = 2f;
        /** How many ghost copies fade out behind the beam. */
        public static final int   PROJECTILE_TRAIL_STEPS = 4;
        /** Pixel spacing between successive trail copies. */
        public static final float PROJECTILE_TRAIL_STEP_PX = 5f;

        // Hit effect (radial burst)
        public static final int   HIT_BURST_SPOKES   = 8;
        public static final float HIT_BURST_RADIUS_START = 2f;
        public static final float HIT_BURST_RADIUS_END   = 7f;
        public static final float HIT_BURST_LINE_LEN     = 3f;
        public static final float HIT_BURST_LINE_THICK   = 1.2f;

        // Enemy
        public static final float ENEMY_OUTLINE_THICK = 2.5f;
        public static final float ENEMY_RADIUS         = 10f;
        /** Peak scale multiplier applied to an enemy at the start of a hit flash. */
        public static final float ENEMY_HIT_FLASH_SCALE = 1.22f;

        // Markers
        public static final float SPAWN_MARKER_RADIUS = 16f;
        public static final float BASE_MARKER_RADIUS  = 18f;

        // Shared
        public static final int CIRCLE_SEGMENTS = 24;

        private Draw() {}
    }
}
