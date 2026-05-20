package com.ironhold.core.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.ironhold.game.model.HitEffect;
import com.ironhold.ui.GameTheme;

import java.util.List;

/**
 * Draws active {@link HitEffect}s as a short-lived radial burst at the impact point.
 *
 * <p>The burst is composed of two parts that animate over the effect's lifetime
 * (driven by {@link HitEffect#getProgress()}):
 * <ul>
 *   <li><b>Expanding ring</b> — a faint teal halo that grows from a small radius
 *       outward and fades to nothing.</li>
 *   <li><b>Spokes</b> — N short white line-segments emanating from the centre.
 *       They drift outward as the effect ages, giving a "spark scatter" feel.</li>
 * </ul>
 *
 * <p>Renders within {@link ShapeRenderer.ShapeType#Filled} (lines are built from
 * oriented rectangles) so it composes with other renderers without mode switches.
 * Leaves {@code batch} in {@code begin()} state on return.
 */
public final class HitEffectRenderer {

    /** Pre-computed cos/sin for evenly distributed spokes, allocated once. */
    private final float[] spokeCos;
    private final float[] spokeSin;

    private final ShapeRenderer shapes;

    public HitEffectRenderer() {
        this.shapes = new ShapeRenderer();
        int n = GameTheme.Draw.HIT_BURST_SPOKES;
        this.spokeCos = new float[n];
        this.spokeSin = new float[n];
        for (int i = 0; i < n; i++) {
            float a = (float) (Math.PI * 2.0 * i / n);
            this.spokeCos[i] = (float) Math.cos(a);
            this.spokeSin[i] = (float) Math.sin(a);
        }
    }

    public void render(SpriteBatch batch, Matrix4 projectionMatrix,
                       List<HitEffect> effects) {
        if (effects == null || effects.isEmpty()) return;

        batch.end();
        shapes.setProjectionMatrix(projectionMatrix);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        for (HitEffect effect : effects) {
            drawBurst(effect);
        }

        shapes.end();
        batch.begin();
        batch.setColor(GameTheme.TINT_WHITE);
    }

    private void drawBurst(HitEffect effect) {
        float p = effect.getProgress();        // 0 at spawn, 1 at extinction
        if (p >= 1f) return;
        float fade = 1f - p;                   // 1 at spawn, 0 at extinction
        float cx = effect.getX();
        float cy = effect.getY();

        // ── 1. Expanding teal ring ──
        // The ring fades as it grows so the silhouette dissolves at the edge.
        float rStart = GameTheme.Draw.HIT_BURST_RADIUS_START;
        float rEnd   = GameTheme.Draw.HIT_BURST_RADIUS_END;
        float radius = rStart + (rEnd - rStart) * p;
        shapes.setColor(scaleAlpha(GameTheme.HIT_BURST_RING, fade * fade));
        // Two concentric circles approximate a ring; inner one is the path background
        // colour so the ring reads as a hollow outline at the impact point.
        shapes.circle(cx, cy, radius, 18);
        shapes.setColor(GameTheme.GROOVE_COLOR);
        shapes.circle(cx, cy, Math.max(0f, radius - 2.2f), 18);

        // ── 2. Radial spokes ──
        float spokeStart = rStart + (rEnd - rStart) * 0.25f * p;
        float spokeLen   = GameTheme.Draw.HIT_BURST_LINE_LEN * fade;
        float thickness  = GameTheme.Draw.HIT_BURST_LINE_THICK;
        Color spokeColor = scaleAlpha(GameTheme.HIT_BURST_LINE, fade);
        shapes.setColor(spokeColor);
        int n = spokeCos.length;
        for (int i = 0; i < n; i++) {
            float cos = spokeCos[i];
            float sin = spokeSin[i];
            float innerX = cx + cos * spokeStart;
            float innerY = cy + sin * spokeStart;
            float outerX = cx + cos * (spokeStart + spokeLen);
            float outerY = cy + sin * (spokeStart + spokeLen);
            drawThickLine(innerX, innerY, outerX, outerY, thickness);
        }
    }

    /**
     * Draws a filled rectangle stretching between two world points with the
     * given total thickness. Equivalent to a thick line within Filled mode.
     */
    private void drawThickLine(float x1, float y1, float x2, float y2, float thickness) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1e-4f) return;
        float nx = -dy / len * (thickness * 0.5f);
        float ny =  dx / len * (thickness * 0.5f);
        float ax = x1 + nx, ay = y1 + ny;
        float bx = x1 - nx, by = y1 - ny;
        float cx = x2 - nx, cy = y2 - ny;
        float dx2 = x2 + nx, dy2 = y2 + ny;
        shapes.triangle(ax, ay, bx, by, cx, cy);
        shapes.triangle(ax, ay, cx, cy, dx2, dy2);
    }

    private static Color scaleAlpha(Color source, float factor) {
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
