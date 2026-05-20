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
            if ("lightning_tower".equals(tower.getTowerId())) {
                drawDiamond(tower.getX(), tower.getY());
                drawLightningCore(tower.getX(), tower.getY(), tower.getPulsePhaseSec());
            } else if ("mortar_tower".equals(tower.getTowerId())) {
                drawMortarHex(tower.getX(), tower.getY());
                drawMortarCore(tower.getX(), tower.getY(), tower.getPulsePhaseSec());
            } else if ("flamethrower_tower".equals(tower.getTowerId())) {
                drawFlameTriangle(tower.getX(), tower.getY());
                drawFlameCore(tower.getX(), tower.getY(), tower.getPulsePhaseSec());
            } else {
                drawBase(tower.getX(), tower.getY());
                drawCore(tower.getX(), tower.getY(), tower.getPulsePhaseSec());
            }
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

    // ── Flamethrower (warning triangle, apex up) ───────────────────────────

    private void drawFlameTriangle(float cx, float cy) {
        float half = GameTheme.Draw.TOWER_SIZE * 0.5f;
        float t = GameTheme.Draw.TOWER_OUTLINE_THICK;

        shapes.setColor(GameTheme.FLAME_OUTLINE);
        drawFilledTriangleUp(cx, cy, half);

        shapes.setColor(GameTheme.SLOT_RECESS);
        drawFilledTriangleUp(cx, cy, Math.max(4f, half - t));
    }

    /** Apex at top centre, base below — warning-sign orientation. */
    private void drawFilledTriangleUp(float cx, float cy, float half) {
        float topY = cy + half;
        float baseY = cy - half;
        shapes.triangle(cx, topY, cx - half, baseY, cx + half, baseY);
    }

    private void drawFlameCore(float cx, float cy, float pulsePhaseSec) {
        float sin = (float) Math.sin(pulsePhaseSec * PULSE_HZ * (Math.PI * 2.0));
        float intensity = 0.775f + 0.225f * sin;
        float radius = GameTheme.Draw.TOWER_CORE_RADIUS * 1.2f * intensity;
        float coreY = cy - GameTheme.Draw.TOWER_SIZE * 0.08f;

        shapes.setColor(blendAlpha(GameTheme.FLAME_CORE_GLOW, intensity * 0.85f));
        shapes.circle(cx, coreY, radius, 12);
        shapes.setColor(blendAlpha(GameTheme.FLAME_CORE, intensity));
        shapes.circle(cx, coreY, radius * 0.5f, 10);
    }

    // ── Mortar tower (large hexagon) ───────────────────────────────────────

    private void drawMortarHex(float cx, float cy) {
        float half = GameTheme.Draw.TOWER_SIZE * 0.5f * GameTheme.Draw.MORTAR_TOWER_SCALE;
        float t = GameTheme.Draw.TOWER_OUTLINE_THICK + 0.5f;

        shapes.setColor(GameTheme.MORTAR_OUTLINE);
        drawFilledHexagon(cx, cy, half);

        shapes.setColor(GameTheme.SLOT_RECESS);
        drawFilledHexagon(cx, cy, Math.max(4f, half - t));
    }

    private void drawMortarCore(float cx, float cy, float pulsePhaseSec) {
        float sin = (float) Math.sin(pulsePhaseSec * PULSE_HZ * (Math.PI * 2.0));
        float intensity = 0.775f + 0.225f * sin;
        float radius = GameTheme.Draw.TOWER_CORE_RADIUS * 1.35f * intensity;

        shapes.setColor(blendAlpha(GameTheme.MORTAR_CORE_GLOW, intensity * 0.85f));
        shapes.circle(cx, cy, radius, 12);
        shapes.setColor(blendAlpha(GameTheme.MORTAR_CORE, intensity));
        shapes.circle(cx, cy, radius * 0.5f, 10);
    }

    private void drawFilledHexagon(float cx, float cy, float r) {
        for (int i = 0; i < 6; i++) {
            float a0 = (float) (Math.PI / 6.0 + i * Math.PI / 3.0);
            float a1 = (float) (Math.PI / 6.0 + (i + 1) * Math.PI / 3.0);
            shapes.triangle(cx, cy,
                cx + (float) Math.cos(a0) * r, cy + (float) Math.sin(a0) * r,
                cx + (float) Math.cos(a1) * r, cy + (float) Math.sin(a1) * r);
        }
    }

    // ── Lightning tower (rhombus / diamond) ────────────────────────────────

    private void drawDiamond(float cx, float cy) {
        float s    = GameTheme.Draw.TOWER_SIZE;
        float half = s * 0.5f;
        float t    = GameTheme.Draw.TOWER_OUTLINE_THICK;

        // Outer violet diamond
        shapes.setColor(GameTheme.LIGHTNING_OUTLINE);
        drawFilledDiamond(cx, cy, half);

        // Inner dark recess diamond
        float inner = half - t;
        shapes.setColor(GameTheme.SLOT_RECESS);
        drawFilledDiamond(cx, cy, inner);
    }

    /** Filled axis-aligned rhombus (4-triangle fan from center). */
    private void drawFilledDiamond(float cx, float cy, float half) {
        // top-right
        shapes.triangle(cx, cy, cx + half, cy, cx, cy + half);
        // top-left
        shapes.triangle(cx, cy, cx - half, cy, cx, cy + half);
        // bottom-right
        shapes.triangle(cx, cy, cx + half, cy, cx, cy - half);
        // bottom-left
        shapes.triangle(cx, cy, cx - half, cy, cx, cy - half);
    }

    private void drawLightningCore(float cx, float cy, float pulsePhaseSec) {
        float sin = (float) Math.sin(pulsePhaseSec * PULSE_HZ * (Math.PI * 2.0));
        float intensity = 0.775f + 0.225f * sin;
        float radius = GameTheme.Draw.TOWER_CORE_RADIUS * intensity;

        shapes.setColor(blendAlpha(GameTheme.LIGHTNING_GLOW, intensity * 0.8f));
        shapes.circle(cx, cy, radius, 12);
        shapes.setColor(blendAlpha(GameTheme.LIGHTNING_CORE, intensity));
        shapes.circle(cx, cy, radius * 0.45f, 10);
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
