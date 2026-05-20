package com.ironhold.core.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.ironhold.game.model.FlameConeEffect;
import com.ironhold.ui.GameTheme;

import java.util.List;

/**
 * Renders expanding flame cones (yellow near the tower → red at the tip).
 */
public final class FlameConeRenderer {

    private final ShapeRenderer shapes;

    public FlameConeRenderer() {
        this.shapes = new ShapeRenderer();
    }

    public void render(SpriteBatch batch, Matrix4 proj, List<FlameConeEffect> effects) {
        if (effects == null || effects.isEmpty()) return;

        batch.end();
        shapes.setProjectionMatrix(proj);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeType.Filled);

        for (FlameConeEffect fx : effects) {
            drawCone(fx);
        }

        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        batch.setColor(GameTheme.TINT_WHITE);
    }

    private void drawCone(FlameConeEffect fx) {
        float progress = fx.getProgress();
        if (progress <= 0.001f) return;

        float fade = 1f - progress * 0.35f;
        float len = fx.getReachDistance();
        if (len < 4f) return;

        float cos = (float) Math.cos(fx.getAimAngleRad());
        float sin = (float) Math.sin(fx.getAimAngleRad());
        float tan = (float) Math.tan(fx.getHalfAngleRad());

        float ox = fx.getOriginX();
        float oy = fx.getOriginY();
        float bx = ox + cos * len;
        float by = oy + sin * len;
        float spread = len * tan;
        float px = -sin * spread;
        float py =  cos * spread;

        float lx = bx + px;
        float ly = by + py;
        float rx = bx - px;
        float ry = by - py;

        // Outer red layer
        shapes.setColor(scaleAlpha(lerp(GameTheme.FLAME_YELLOW, GameTheme.FLAME_RED, 0.85f), 0.38f * fade));
        shapes.triangle(ox, oy, lx, ly, rx, ry);

        // Mid orange
        float mid = 0.72f;
        float mx = ox + cos * len * mid;
        float my = oy + sin * len * mid;
        float ms = spread * mid;
        shapes.setColor(scaleAlpha(lerp(GameTheme.FLAME_YELLOW, GameTheme.FLAME_RED, 0.45f), 0.52f * fade));
        shapes.triangle(ox, oy, mx - sin * ms, my + cos * ms, mx + sin * ms, my - cos * ms);

        // Inner yellow core
        float inner = 0.45f;
        float ix = ox + cos * len * inner;
        float iy = oy + sin * len * inner;
        float is = spread * inner;
        shapes.setColor(scaleAlpha(GameTheme.FLAME_YELLOW, 0.65f * fade));
        shapes.triangle(ox, oy, ix - sin * is, iy + cos * is, ix + sin * is, iy - cos * is);
    }

    private static Color lerp(Color a, Color b, float t) {
        float u = Math.max(0f, Math.min(1f, t));
        return new Color(
            a.r + (b.r - a.r) * u,
            a.g + (b.g - a.g) * u,
            a.b + (b.b - a.b) * u,
            a.a + (b.a - a.a) * u
        );
    }

    private static Color scaleAlpha(Color source, float factor) {
        Color c = new Color(source);
        float a = source.a * factor;
        c.a = Math.max(0f, Math.min(1f, a));
        return c;
    }

    public void dispose() {
        shapes.dispose();
    }
}
