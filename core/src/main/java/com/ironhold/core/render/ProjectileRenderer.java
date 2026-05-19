package com.ironhold.core.render;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.ironhold.game.model.ActiveProjectile;
import com.ironhold.ui.GameTheme;

import java.util.List;

/**
 * Draws projectiles as small filled circles using {@link ShapeRenderer}.
 * Leaves {@code batch} in {@code begin()} state when done.
 */
public final class ProjectileRenderer {

    private static final float RADIUS = 6f;
    private static final int   SEGS   = 10;

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
        shapes.setColor(GameTheme.PROJECTILE);

        for (ActiveProjectile p : projectiles) {
            shapes.circle(p.getX(), p.getY(), RADIUS, SEGS);
        }

        shapes.end();
        batch.begin();
        batch.setColor(GameTheme.TINT_WHITE);
    }

    public void dispose() {
        shapes.dispose();
    }
}
