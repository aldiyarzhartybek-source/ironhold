package com.ironhold.core;

import com.badlogic.gdx.graphics.OrthographicCamera;

/**
 * Fixed world size for gameplay (paths, slots, entities). {@link com.badlogic.gdx.utils.viewport.FitViewport}
 * fits this rect on screen; {@link #WORLD_ZOOM} shrinks the map to half size and centers on the playfield.
 */
public final class GameplayViewport {

    /** Logical world width (level coords ~64–760 plus margin). */
    public static final float WORLD_WIDTH = 824f;
    /** Logical world height (level coords ~60–420 plus margin). */
    public static final float WORLD_HEIGHT = 480f;

    /** Camera zoom &gt; 1 — smaller on-screen map (2 = half size). */
    public static final float WORLD_ZOOM = 2f;

    /** Centre of level paths / slots in world coordinates. */
    public static final float CONTENT_CENTER_X = 412f;
    public static final float CONTENT_CENTER_Y = 240f;

    private GameplayViewport() {
    }

    public static void applyWorldCamera(OrthographicCamera camera) {
        camera.zoom = WORLD_ZOOM;
        camera.position.set(CONTENT_CENTER_X, CONTENT_CENTER_Y, 0f);
    }

    public static float visibleWorldWidth(OrthographicCamera camera) {
        return camera.viewportWidth * camera.zoom;
    }

    public static float visibleWorldHeight(OrthographicCamera camera) {
        return camera.viewportHeight * camera.zoom;
    }
}
