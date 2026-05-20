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
 * Draws placed towers as a two-layer composition:
 * <ol>
 *   <li>Base — white rounded square with a darker teal hollow centre.</li>
 *   <li>Pulsing core — a small teal dot that breathes between shots.</li>
 * </ol>
 *
 * <p>All rendering uses {@link ShapeType#Filled} primitives only.
 * Pulse phase is read from {@link PlacedTower} and driven by the combat system
 * so animations respect game pause and speed multipliers.
 */
public final class TowerShapeRenderer {

    private static final int CORNER_SEGS = 10;
    /** Pulse frequency in Hz — chosen so a base + a turret idle look "alive" but not jittery. */
    private static final float PULSE_HZ = 1.4f;

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
            drawBase(tower.getX(), tower.getY());
            drawCore(tower.getX(), tower.getY(), tower.getPulsePhaseSec());
        }

        shapes.end();
        batch.begin();
        batch.setColor(GameTheme.TINT_WHITE);
    }

    // ── Base (the static platform) ─────────────────────────────────────────

    private void drawBase(float x, float y) {
        float s  = GameTheme.Draw.TOWER_SIZE;
        float t  = GameTheme.Draw.TOWER_OUTLINE_THICK;
        float cr = GameTheme.Draw.TOWER_CORNER_R;
        float half = s * 0.5f;

        // 1. White outer rounded rectangle (thick border)
        shapes.setColor(GameTheme.TOWER_OUTLINE);
        drawRoundedRect(x - half, y - half, s, s, cr);

        // 2. Inner rounded rect — muted teal, creates hollow recessed look
        float innerS  = s - t * 2f;
        float innerCr = Math.max(1f, cr - t * 0.6f);
        shapes.setColor(GameTheme.SLOT_RECESS);
        drawRoundedRect(x - half + t, y - half + t, innerS, innerS, innerCr);
    }

    // ── Pulsing core (lives between shots) ─────────────────────────────────

    private void drawCore(float cx, float cy, float pulsePhaseSec) {
        // sin oscillates in [-1, 1] -> remap to [0.55, 1.0] so the core never
        // shrinks to nothing (which would look like flicker, not a pulse).
        float sin = (float) Math.sin(pulsePhaseSec * PULSE_HZ * (Math.PI * 2.0));
        float intensity = 0.775f + 0.225f * sin;            // [0.55, 1.0]
        float radius    = GameTheme.Draw.TOWER_CORE_RADIUS * intensity;

        // Outer teal glow circle (dimmer at low intensity)
        shapes.setColor(blendAlpha(GameTheme.TOWER_CORE_BASE, intensity));
        shapes.circle(cx, cy, radius, 12);
        // White hot centre (smaller, brighter at peak)
        shapes.setColor(blendAlpha(GameTheme.TOWER_CORE_HIGHLIGHT, intensity * intensity));
        shapes.circle(cx, cy, radius * 0.5f, 10);
    }

    // ── Geometry helpers ───────────────────────────────────────────────────

    /**
     * Draws a filled rounded rectangle using centre strip, side strips, and
     * 4 corner circles — all in the current {@code shapes} colour.
     */
    private void drawRoundedRect(float x, float y, float w, float h, float r) {
        shapes.rect(x + r, y, w - r * 2f, h);
        shapes.rect(x, y + r, r, h - r * 2f);
        shapes.rect(x + w - r, y + r, r, h - r * 2f);
        shapes.circle(x + r,         y + r,         r, CORNER_SEGS);
        shapes.circle(x + w - r,     y + r,         r, CORNER_SEGS);
        shapes.circle(x + r,         y + h - r,     r, CORNER_SEGS);
        shapes.circle(x + w - r,     y + h - r,     r, CORNER_SEGS);
    }

    /** Returns {@code source} with its alpha scaled by {@code factor}, clamped to [0, 1]. */
    private static Color blendAlpha(Color source, float factor) {
        Color c = new Color(source);
        float a = source.a * factor;
        if (a < 0f) a = 0f;
        if (a > 1f) a = 1f;
        c.a = a;
        return c;
    }

    public void dispose() {
        shapes.dispose();
    }
}
