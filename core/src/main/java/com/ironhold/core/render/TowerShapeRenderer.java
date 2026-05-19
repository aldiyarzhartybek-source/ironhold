package com.ironhold.core.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.ironhold.game.model.PlacedTower;
import com.ironhold.ui.GameTheme;

import java.util.List;

/**
 * Draws placed towers as rounded hollow squares.
 *
 * <p>Technique (all Filled, no ShapeType.Line):
 * <ol>
 *   <li>Outer rounded rect — white thick border.</li>
 *   <li>Inner rounded rect — darker teal, creates hollow recessed look.</li>
 * </ol>
 * Shadow is drawn by {@link GameplayMapRenderer} (Layer 2b blended pass).
 *
 * <p>Rounded rect = centre strip + 2 side strips + 4 corner circles.
 */
public final class TowerShapeRenderer {

    private static final int CORNER_SEGS = 10;

    private final ShapeRenderer shapes;

    public TowerShapeRenderer() {
        this.shapes = new ShapeRenderer();
    }

    public void render(SpriteBatch batch, Matrix4 projectionMatrix,
                       List<PlacedTower> towers) {
        if (towers == null || towers.isEmpty()) return;

        batch.end();
        shapes.setProjectionMatrix(projectionMatrix);
        shapes.begin(ShapeType.Filled);

        for (PlacedTower tower : towers) {
            drawTower(tower.getX(), tower.getY());
        }

        shapes.end();
        batch.begin();
        batch.setColor(GameTheme.TINT_WHITE);
    }

    private void drawTower(float x, float y) {
        float s  = GameTheme.Draw.TOWER_SIZE;           // 28f — full square size
        float t  = GameTheme.Draw.TOWER_OUTLINE_THICK;  // 4.5f — thick border
        float cr = GameTheme.Draw.TOWER_CORNER_R;       // 5f — corner radius

        float half = s * 0.5f;

        // 1. White outer rounded rectangle (thick border)
        shapes.setColor(GameTheme.TOWER_OUTLINE);
        drawRoundedRect(x - half, y - half, s, s, cr);

        // 2. Inner rounded rect — muted teal, creates hollow recessed look
        float innerS  = s - t * 2f;
        float innerCr = Math.max(1f, cr - t * 0.6f);
        shapes.setColor(GameTheme.SLOT_RECESS);   // #169488 — muted, clearly dimmer than wall
        drawRoundedRect(x - half + t, y - half + t, innerS, innerS, innerCr);
    }

    /**
     * Draws a filled rounded rectangle using centre strip, side strips, and
     * 4 corner circles — all in the current {@code shapes} colour.
     *
     * @param x     left edge
     * @param y     bottom edge
     * @param w     total width
     * @param h     total height
     * @param r     corner radius
     */
    private void drawRoundedRect(float x, float y, float w, float h, float r) {
        // Centre horizontal strip (full height, inner width)
        shapes.rect(x + r, y, w - r * 2f, h);
        // Left strip
        shapes.rect(x, y + r, r, h - r * 2f);
        // Right strip
        shapes.rect(x + w - r, y + r, r, h - r * 2f);
        // Four corner arcs (filled fan)
        shapes.circle(x + r,         y + r,         r, CORNER_SEGS); // bottom-left
        shapes.circle(x + w - r,     y + r,         r, CORNER_SEGS); // bottom-right
        shapes.circle(x + r,         y + h - r,     r, CORNER_SEGS); // top-left
        shapes.circle(x + w - r,     y + h - r,     r, CORNER_SEGS); // top-right
    }

    public void dispose() {
        shapes.dispose();
    }
}
