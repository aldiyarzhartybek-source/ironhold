package com.ironhold.core.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.ironhold.game.model.LightningEffect;
import com.ironhold.ui.GameTheme;

import java.util.List;

/**
 * Renders chain-lightning flashes produced by the Lightning tower.
 *
 * <p>Each {@link LightningEffect} stores pre-computed zigzag waypoints.
 * We draw two overlapping line passes:
 * <ol>
 *   <li>Wide outer glow — low alpha, purple.</li>
 *   <li>Thin bright core — high alpha, white-blue, fades with progress.</li>
 * </ol>
 */
public final class LightningRenderer {

    private final ShapeRenderer shapes;

    public LightningRenderer() {
        this.shapes = new ShapeRenderer();
    }

    public void render(SpriteBatch batch, Matrix4 proj, List<LightningEffect> effects) {
        if (effects == null || effects.isEmpty()) return;

        batch.end();
        shapes.setProjectionMatrix(proj);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        for (LightningEffect fx : effects) {
            float[] wp = fx.getWaypoints();
            if (wp.length < 4) continue;
            // Fade out as the effect ages (progress 0→1 = fresh→gone)
            float alpha = 1f - fx.getProgress();

            // Outer glow — wide, purple, low alpha
            Gdx.gl.glLineWidth(5f);
            shapes.begin(ShapeType.Line);
            shapes.setColor(
                GameTheme.LIGHTNING_GLOW.r,
                GameTheme.LIGHTNING_GLOW.g,
                GameTheme.LIGHTNING_GLOW.b,
                GameTheme.LIGHTNING_GLOW.a * alpha);
            for (int i = 0; i < wp.length - 2; i += 2) {
                shapes.line(wp[i], wp[i + 1], wp[i + 2], wp[i + 3]);
            }
            shapes.end();

            // Core — thin, bright white-blue
            Gdx.gl.glLineWidth(2f);
            shapes.begin(ShapeType.Line);
            shapes.setColor(
                GameTheme.LIGHTNING_CORE.r,
                GameTheme.LIGHTNING_CORE.g,
                GameTheme.LIGHTNING_CORE.b,
                GameTheme.LIGHTNING_CORE.a * alpha);
            for (int i = 0; i < wp.length - 2; i += 2) {
                shapes.line(wp[i], wp[i + 1], wp[i + 2], wp[i + 3]);
            }
            shapes.end();
        }

        Gdx.gl.glLineWidth(1f);
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        batch.setColor(GameTheme.TINT_WHITE);
    }

    public void dispose() {
        shapes.dispose();
    }
}
