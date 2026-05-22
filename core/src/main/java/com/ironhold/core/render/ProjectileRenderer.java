package com.ironhold.core.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.ironhold.game.model.ActiveProjectile;
import com.ironhold.game.model.ProjectileKind;
import com.ironhold.ui.GameTheme;

import java.util.List;

/**
 * Draws projectiles as oriented energy beams with a fading trail.
 *
 * <p>Per projectile, three visual elements are composed (back-to-front):
 * <ol>
 *   <li><b>Trail</b> — N ghost copies of the beam laid out behind the head,
 *       each smaller and dimmer than the previous one. Uses {@code prevX/prevY}
 *       from the projectile to derive the travel direction without the renderer
 *       needing to keep a history buffer.</li>
 *   <li><b>Halo</b> — a soft teal outline rectangle slightly larger than the
 *       core, drawn just behind it for a glow-like silhouette.</li>
 *   <li><b>Core</b> — a bright white rectangle oriented along the travel axis;
 *       this is the "energetic" highlight that catches the eye.</li>
 * </ol>
 *
 * <p>All elements are oriented rectangles built from two triangles, drawn within
 * the active {@link ShapeRenderer.ShapeType#Filled} batch. No {@code Line} mode,
 * no per-resolution thickness drift.
 */
public final class ProjectileRenderer {

    private final ShapeRenderer shapes;

    public ProjectileRenderer() {
        this.shapes = new ShapeRenderer();
    }

    public void render(SpriteBatch batch, Matrix4 proj,
                       List<ActiveProjectile> projectiles) {
        if (projectiles.isEmpty()) return;

        batch.end();
        shapes.setProjectionMatrix(proj);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        for (ActiveProjectile p : projectiles) {
            drawProjectile(p);
        }

        shapes.end();
        batch.begin();
        batch.setColor(GameTheme.TINT_WHITE);
    }

    private void drawProjectile(ActiveProjectile p) {
        if (p.getKind() == ProjectileKind.MORTAR_SHELL) {
            drawMortarShell(p);
            return;
        }

        // Travel direction = (current - prev). On the first frame these are equal,
        // so we fall back to a horizontal default to avoid drawing a zero-length beam.
        float dx = p.getX() - p.getPrevX();
        float dy = p.getY() - p.getPrevY();
        float lenSq = dx * dx + dy * dy;
        float cos, sin;
        if (lenSq < 1e-4f) {
            cos = 1f;
            sin = 0f;
        } else {
            float invLen = 1f / (float) Math.sqrt(lenSq);
            cos = dx * invLen;
            sin = dy * invLen;
        }

        // 1. Trail — older copies first so the head paints over them.
        int   steps = GameTheme.Draw.PROJECTILE_TRAIL_STEPS;
        float spacing = GameTheme.Draw.PROJECTILE_TRAIL_STEP_PX;
        for (int i = steps; i >= 1; i--) {
            float t      = i / (float) steps;                       // 1.0 oldest, 0.25 closest
            float offset = spacing * i;
            float tx = p.getX() - cos * offset;
            float ty = p.getY() - sin * offset;
            float falloff = 1f - t;                                 // 0 oldest, 0.75 closest
            float halfLen = GameTheme.Draw.PROJECTILE_BEAM_LEN * 0.5f * (0.55f + falloff * 0.4f);
            float halfWid = GameTheme.Draw.PROJECTILE_BEAM_HW       * (0.4f  + falloff * 0.6f);
            shapes.setColor(scaleAlpha(GameTheme.PROJECTILE_TRAIL, falloff * 0.85f));
            drawOrientedRect(tx, ty, cos, sin, halfLen, halfWid);
        }

        // 2. Halo behind the core — sells the energetic glow without needing a shader.
        float coreLen = GameTheme.Draw.PROJECTILE_BEAM_LEN * 0.5f;
        float coreWid = GameTheme.Draw.PROJECTILE_BEAM_HW;
        shapes.setColor(GameTheme.PROJECTILE_HALO);
        drawOrientedRect(p.getX(), p.getY(), cos, sin, coreLen + 2f, coreWid + 1.5f);

        // 3. Hot white core
        shapes.setColor(GameTheme.PROJECTILE_CORE);
        drawOrientedRect(p.getX(), p.getY(), cos, sin, coreLen, coreWid);
    }

    /**
     * Draws a filled rectangle oriented along the unit vector ({@code cos}, {@code sin}),
     * centred at ({@code cx}, {@code cy}), with the given half-dimensions.
     */
    private void drawOrientedRect(float cx, float cy, float cos, float sin,
                                  float halfLen, float halfWid) {
        float lx =  halfLen,  rx = -halfLen;
        float ty =  halfWid,  by = -halfWid;
        float ax = cx + lx * cos - ty * sin, ay = cy + lx * sin + ty * cos;
        float bx = cx + lx * cos - by * sin, b_y = cy + lx * sin + by * cos;
        float dx = cx + rx * cos - ty * sin, dy = cy + rx * sin + ty * cos;
        float ex = cx + rx * cos - by * sin, ey = cy + rx * sin + by * cos;
        shapes.triangle(ax, ay, bx, b_y, ex, ey);
        shapes.triangle(ax, ay, ex, ey, dx, dy);
    }

    private void drawMortarShell(ActiveProjectile p) {
        float r = GameTheme.Draw.MORTAR_SHELL_RADIUS;
        shapes.setColor(GameTheme.MORTAR_SHELL_GLOW);
        shapes.circle(p.getX(), p.getY(), r + 2.5f, 16);
        shapes.setColor(GameTheme.MORTAR_SHELL);
        shapes.circle(p.getX(), p.getY(), r, 14);
        shapes.setColor(GameTheme.MORTAR_SHELL_HOT);
        shapes.circle(p.getX(), p.getY(), r * 0.45f, 10);
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
