package com.ironhold.core.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.ironhold.game.model.MortarExplosionEffect;
import com.ironhold.ui.GameTheme;

import java.util.List;

/**
 * Draws expanding splash rings for mortar impacts.
 */
public final class MortarExplosionRenderer {

    private final ShapeRenderer shapes;

    public MortarExplosionRenderer() {
        this.shapes = new ShapeRenderer();
    }

    public void render(SpriteBatch batch, Matrix4 proj, List<MortarExplosionEffect> effects) {
        if (effects == null || effects.isEmpty()) return;

        batch.end();
        shapes.setProjectionMatrix(proj);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeType.Filled);

        for (MortarExplosionEffect fx : effects) {
            float p = fx.getProgress();
            float fade = 1f - p;
            float radius = fx.getBlastRadius() * p;
            if (radius < 1f) continue;

            float cx = fx.getX();
            float cy = fx.getY();

            // Outer blast ring
            shapes.setColor(
                GameTheme.MORTAR_BLAST_RING.r,
                GameTheme.MORTAR_BLAST_RING.g,
                GameTheme.MORTAR_BLAST_RING.b,
                GameTheme.MORTAR_BLAST_RING.a * fade);
            shapes.circle(cx, cy, radius, 24);

            // Hollow centre so it reads as a ring, not a filled disc
            float inner = Math.max(0f, radius - 3.5f);
            shapes.setColor(
                GameTheme.MORTAR_BLAST_CORE.r,
                GameTheme.MORTAR_BLAST_CORE.g,
                GameTheme.MORTAR_BLAST_CORE.b,
                GameTheme.MORTAR_BLAST_CORE.a * fade * 0.85f);
            shapes.circle(cx, cy, inner, 22);
        }

        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        batch.setColor(GameTheme.TINT_WHITE);
    }

    public void dispose() {
        shapes.dispose();
    }
}
