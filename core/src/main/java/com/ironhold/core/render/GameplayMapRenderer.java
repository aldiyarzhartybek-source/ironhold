package com.ironhold.core.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.ironhold.game.GameRuntimeView;
import com.ironhold.game.model.BuildSlot;
import com.ironhold.ui.GameTheme;

import java.util.List;

/**
 * Layered 2.5D map renderer with pseudo-volumetric depth.
 *
 * <p>Layer order:
 * <ol>
 *   <li>Teal viewport fill — the wall / high ground.</li>
 *   <li>Opaque shadow pass — path trench + empty slot circles, shifted (−14,−14).</li>
 *   <li>Blended shadow pass — built-tower squares (0,0,0,0.25f), shifted (−6,−6).</li>
 *   <li>Trench — dark graphite channel.</li>
 *   <li>Spawn gate marker.</li>
 *   <li>Base inner red indicator.</li>
 *   <li>Base white wireframe hexagon (Line).</li>
 *   <li>Slot circle markers.</li>
 * </ol>
 */
public final class GameplayMapRenderer {

    // Trench/path inner shadow offset (solid colour, no alpha accumulation)
    private static final float SHADOW_OX    = -14f;
    private static final float SHADOW_OY    = -14f;
    private static final Color SHADOW_COLOR = new Color(0.02f, 0.02f, 0.07f, 1f);


    private final ShapeRenderer shapes;

    public GameplayMapRenderer() {
        this.shapes = new ShapeRenderer();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Public API
    // ══════════════════════════════════════════════════════════════════════

    /** Renders the map. Leaves {@code batch} in {@code begin()} state when done. */
    public void render(SpriteBatch batch, Matrix4 projectionMatrix, GameRuntimeView view) {
        List<Vector2> path   = view.getEnemyPath();
        List<BuildSlot> slots = view.getBuildSlots();
        if (path.isEmpty()) return;

        float vw = Gdx.graphics.getWidth();
        float vh = Gdx.graphics.getHeight();

        batch.end();
        shapes.setProjectionMatrix(projectionMatrix);

        // ── 1: teal fill — entire viewport = wall/high ground ──────────────
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(GameTheme.WALL_COLOR);
        shapes.rect(0f, 0f, vw, vh);
        shapes.end();

        // ── 2a: opaque shadow pass — path trench + empty-slot circles ────────
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawOpaqueShadow(path, slots);
        shapes.end();

        // ── 2b: blended shadow pass — built-tower squares (0,0,0,0.25f) ─────
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawTowerShadows(slots);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // ── 3: trench ───────────────────────────────────────────────────────
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawTrench(path);
        shapes.end();

        // ── 4: spawn gate (Filled) ──────────────────────────────────────────
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawSpawnGate(path);
        shapes.end();

        // ── 5: base red inner indicator (Filled) ────────────────────────────
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawBaseInner(path);
        shapes.end();

        // ── 6: base white wireframe hexagon only (Line, no shadow here) ─────
        Gdx.gl.glLineWidth(3f);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        drawBaseWireframe(path);
        shapes.end();
        Gdx.gl.glLineWidth(1f);

        // ── 7: slot circle markers (Filled) ─────────────────────────────────
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawBuildSlots(slots);
        shapes.end();
        batch.begin();
        batch.setColor(GameTheme.TINT_WHITE);
    }

    public void dispose() {
        shapes.dispose();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Layer 2a — opaque shadow (path + empty-slot circles), no blending needed
    // ══════════════════════════════════════════════════════════════════════

    private void drawOpaqueShadow(List<Vector2> path, List<BuildSlot> slots) {
        float hw = GameTheme.Draw.GROOVE_HALF_WIDTH;
        float ox = SHADOW_OX, oy = SHADOW_OY;

        // Path trench shadow
        shapes.setColor(SHADOW_COLOR);
        for (int i = 0; i < path.size() - 1; i++) {
            Vector2 f = path.get(i), t = path.get(i + 1);
            drawThickSegment(f.x + ox, f.y + oy, t.x + ox, t.y + oy, hw, SHADOW_COLOR);
        }
        for (Vector2 p : path) {
            shapes.circle(p.x + ox, p.y + oy, hw, GameTheme.Draw.CIRCLE_SEGMENTS);
        }

        // Empty-slot circle shadows only (opaque, same colour as path shadow)
        for (BuildSlot slot : slots) {
            if (!slot.isOccupied()) {
                shapes.setColor(SHADOW_COLOR);
                shapes.circle(slot.getX() + ox, slot.getY() + oy, 9f, 16);
            }
        }
        // Base has no shadow — white wireframe stands clean
    }

    // ══════════════════════════════════════════════════════════════════════
    // Layer 2b — blended tower shadows, GL_BLEND enabled for this pass
    // ══════════════════════════════════════════════════════════════════════

    /** Draws a semi-transparent filled square beneath each built tower. */
    private void drawTowerShadows(List<BuildSlot> slots) {
        float half = GameTheme.Draw.TOWER_SIZE * 0.5f;
        shapes.setColor(0f, 0f, 0f, 0.25f);
        for (BuildSlot slot : slots) {
            if (slot.isOccupied()) {
                shapes.rect(slot.getX() - half - 6f,
                            slot.getY() - half - 6f,
                            GameTheme.Draw.TOWER_SIZE,
                            GameTheme.Draw.TOWER_SIZE);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Layer 4 — trench
    // ══════════════════════════════════════════════════════════════════════

    private void drawTrench(List<Vector2> path) {
        if (path.size() < 2) return;
        float hw = GameTheme.Draw.GROOVE_HALF_WIDTH;
        Color gc = GameTheme.GROOVE_COLOR;
        for (int i = 0; i < path.size() - 1; i++) {
            Vector2 f = path.get(i), t = path.get(i + 1);
            drawThickSegment(f.x, f.y, t.x, t.y, hw, gc);
        }
        shapes.setColor(gc);
        for (Vector2 p : path) {
            shapes.circle(p.x, p.y, hw, GameTheme.Draw.CIRCLE_SEGMENTS);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Layer 5 — spawn gate
    // ══════════════════════════════════════════════════════════════════════

    private void drawSpawnGate(List<Vector2> path) {
        Vector2 spawn     = path.get(0);
        Vector2 firstStep = path.size() > 1 ? path.get(1) : spawn;
        float dx = firstStep.x - spawn.x, dy = firstStep.y - spawn.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.01f) { dx = 1f; dy = 0f; len = 1f; }
        float ux = dx / len, uy = dy / len;
        float px = -uy, py = ux;
        float r  = GameTheme.Draw.SPAWN_MARKER_RADIUS;
        shapes.setColor(GameTheme.SPAWN_MARKER);
        shapes.triangle(
            spawn.x + ux * r * 1.35f,          spawn.y + uy * r * 1.35f,
            spawn.x - ux * r * 0.55f + px * r, spawn.y - uy * r * 0.55f + py * r,
            spawn.x - ux * r * 0.55f - px * r, spawn.y - uy * r * 0.55f - py * r
        );
        shapes.setColor(GameTheme.withAlpha(GameTheme.GOLD, 0.35f));
        shapes.circle(spawn.x, spawn.y, r * 0.55f, GameTheme.Draw.CIRCLE_SEGMENTS);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Layer 6 — base inner red indicator
    // ══════════════════════════════════════════════════════════════════════

    /** Small filled red hexagon + tiny bright dot — the inner "HP core" of the base. */
    private void drawBaseInner(List<Vector2> path) {
        Vector2 base = path.get(path.size() - 1);
        float r = GameTheme.Draw.BASE_MARKER_RADIUS;
        // Faint red inner fill
        shapes.setColor(GameTheme.withAlpha(GameTheme.BASE_MARKER, 0.38f));
        drawHexagon(base.x, base.y, r * 0.55f);
        // Bright red centre dot
        shapes.setColor(GameTheme.BASE_MARKER);
        shapes.circle(base.x, base.y, r * 0.20f, 12);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Layer 7 — base wireframe hexagon (Line)
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Bold white hexagon wireframe only — shadow is drawn earlier in the global Filled pass.
     * Rendered with line width 3f, set before entering this method.
     */
    private void drawBaseWireframe(List<Vector2> path) {
        Vector2 base = path.get(path.size() - 1);
        float r = GameTheme.Draw.BASE_MARKER_RADIUS;
        shapes.setColor(Color.WHITE);
        drawHexagonLine(base.x, base.y, r);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Layer 8 — slot circles
    // ══════════════════════════════════════════════════════════════════════

    private void drawBuildSlots(List<BuildSlot> slots) {
        for (BuildSlot slot : slots) {
            // Only draw circle marker for empty slots — towers cover occupied ones
            if (!slot.isOccupied()) {
                shapes.setColor(GameTheme.SLOT_RECESS);
                shapes.circle(slot.getX(), slot.getY(), 9f, 16);
                shapes.setColor(GameTheme.SLOT_RECESS_INNER);
                shapes.circle(slot.getX(), slot.getY(), 5f, 12);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Geometry helpers
    // ══════════════════════════════════════════════════════════════════════

    /** Filled hexagon (fan of 6 triangles). */
    private void drawHexagon(float cx, float cy, float r) {
        for (int i = 0; i < 6; i++) {
            float a0 = (float) (Math.PI / 6.0 + i       * Math.PI / 3.0);
            float a1 = (float) (Math.PI / 6.0 + (i + 1) * Math.PI / 3.0);
            shapes.triangle(cx, cy,
                cx + (float) Math.cos(a0) * r, cy + (float) Math.sin(a0) * r,
                cx + (float) Math.cos(a1) * r, cy + (float) Math.sin(a1) * r);
        }
    }

    /** Hexagon outline via 6 line segments (use in ShapeType.Line mode). */
    private void drawHexagonLine(float cx, float cy, float r) {
        for (int i = 0; i < 6; i++) {
            float a0 = (float) (Math.PI / 6.0 + i       * Math.PI / 3.0);
            float a1 = (float) (Math.PI / 6.0 + (i + 1) * Math.PI / 3.0);
            shapes.line(
                cx + (float) Math.cos(a0) * r, cy + (float) Math.sin(a0) * r,
                cx + (float) Math.cos(a1) * r, cy + (float) Math.sin(a1) * r);
        }
    }

    private void drawThickSegment(float x1, float y1, float x2, float y2,
                                  float hw, Color color) {
        float dx  = x2 - x1, dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.01f) return;
        float nx = -dy / len * hw, ny = dx / len * hw;
        shapes.setColor(color);
        shapes.triangle(x1 + nx, y1 + ny, x1 - nx, y1 - ny, x2 + nx, y2 + ny);
        shapes.triangle(x2 + nx, y2 + ny, x1 - nx, y1 - ny, x2 - nx, y2 - ny);
    }

}
