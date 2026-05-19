package com.ironhold.core.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.ironhold.game.model.ActiveEnemy;
import com.ironhold.game.model.Enemy;
import com.ironhold.game.model.EnemyVisualShape;
import com.ironhold.ui.GameTheme;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Draws active enemies as colored geometric shapes defined by their template config.
 */
public final class EnemyShapeRenderer {

    private final Map<String, Enemy> templatesById;
    private final ShapeRenderer shapes;

    public EnemyShapeRenderer(Map<String, Enemy> templatesById) {
        this.templatesById = Map.copyOf(Objects.requireNonNull(templatesById, "templatesById"));
        this.shapes = new ShapeRenderer();
    }

    public void render(
        SpriteBatch batch,
        Matrix4 projectionMatrix,
        List<ActiveEnemy> enemies,
        List<Vector2> path
    ) {
        if (enemies.isEmpty()) {
            return;
        }

        batch.end();
        shapes.setProjectionMatrix(projectionMatrix);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        for (ActiveEnemy enemy : enemies) {
            Enemy template = resolveTemplate(enemy.getEnemyId());
            float radius   = template.getVisualRadius();
            float rotation = facingAngle(path, enemy);

            // 1. White outline — slightly larger shape drawn first
            float outlineRadius = radius + GameTheme.Draw.ENEMY_OUTLINE_THICK;
            drawShape(template.getVisualShape(), enemy.getX(), enemy.getY(),
                outlineRadius, rotation, GameTheme.ENEMY_OUTLINE);

            // 2. Normal colour fill on top
            drawShape(template.getVisualShape(), enemy.getX(), enemy.getY(),
                radius, rotation, template.getFillColor());
        }

        shapes.end();
        batch.begin();
        batch.setColor(GameTheme.TINT_WHITE);
    }

    public float getVisualRadius(ActiveEnemy enemy) {
        return resolveTemplate(enemy.getEnemyId()).getVisualRadius();
    }

    public void dispose() {
        shapes.dispose();
    }

    private Enemy resolveTemplate(String enemyId) {
        Enemy template = templatesById.get(enemyId);
        if (template != null) {
            return template;
        }
        return templatesById.values().stream().findFirst().orElseGet(Enemy::defaultFallback);
    }

    private void drawShape(
        EnemyVisualShape shape,
        float centerX,
        float centerY,
        float radius,
        float rotationRad,
        Color fillColor
    ) {
        shapes.setColor(fillColor);
        switch (shape) {
            case TRIANGLE:
                drawTriangle(centerX, centerY, radius, rotationRad);
                break;
            case PENTAGON:
                drawRegularPolygon(centerX, centerY, radius, rotationRad, 5);
                break;
            case HEXAGON:
                drawRegularPolygon(centerX, centerY, radius, rotationRad, 6);
                break;
            case SQUARE:
            default:
                drawSquare(centerX, centerY, radius, rotationRad);
                break;
        }
    }

    private void drawSquare(float centerX, float centerY, float radius, float rotationRad) {
        float half = radius * 0.92f;
        float cos = (float) Math.cos(rotationRad);
        float sin = (float) Math.sin(rotationRad);
        float[] cornersX = new float[4];
        float[] cornersY = new float[4];
        float[][] offsets = {
            {-half, -half},
            {half, -half},
            {half, half},
            {-half, half}
        };
        for (int i = 0; i < 4; i++) {
            float ox = offsets[i][0];
            float oy = offsets[i][1];
            cornersX[i] = centerX + ox * cos - oy * sin;
            cornersY[i] = centerY + ox * sin + oy * cos;
        }
        shapes.triangle(cornersX[0], cornersY[0], cornersX[1], cornersY[1], cornersX[2], cornersY[2]);
        shapes.triangle(cornersX[0], cornersY[0], cornersX[2], cornersY[2], cornersX[3], cornersY[3]);
    }

    private void drawTriangle(float centerX, float centerY, float radius, float rotationRad) {
        float tipAngle = rotationRad;
        float leftAngle = rotationRad + (float) (Math.PI * 2.0 / 3.0);
        float rightAngle = rotationRad - (float) (Math.PI * 2.0 / 3.0);
        float tipX = centerX + (float) Math.cos(tipAngle) * radius;
        float tipY = centerY + (float) Math.sin(tipAngle) * radius;
        float leftX = centerX + (float) Math.cos(leftAngle) * radius * 0.9f;
        float leftY = centerY + (float) Math.sin(leftAngle) * radius * 0.9f;
        float rightX = centerX + (float) Math.cos(rightAngle) * radius * 0.9f;
        float rightY = centerY + (float) Math.sin(rightAngle) * radius * 0.9f;
        shapes.triangle(tipX, tipY, leftX, leftY, rightX, rightY);
    }

    private void drawRegularPolygon(
        float centerX,
        float centerY,
        float radius,
        float rotationRad,
        int sides
    ) {
        for (int i = 0; i < sides; i++) {
            float a0 = rotationRad + (float) (Math.PI * 2.0 * i / sides);
            float a1 = rotationRad + (float) (Math.PI * 2.0 * (i + 1) / sides);
            float x0 = centerX + (float) Math.cos(a0) * radius;
            float y0 = centerY + (float) Math.sin(a0) * radius;
            float x1 = centerX + (float) Math.cos(a1) * radius;
            float y1 = centerY + (float) Math.sin(a1) * radius;
            shapes.triangle(centerX, centerY, x0, y0, x1, y1);
        }
    }

    private static float facingAngle(List<Vector2> path, ActiveEnemy enemy) {
        if (path == null || path.isEmpty()) {
            return 0f;
        }
        int waypointIndex = Math.max(0, Math.min(enemy.getTargetWaypointIndex(), path.size() - 1));
        Vector2 from = path.get(Math.max(0, waypointIndex - 1));
        Vector2 to = path.get(waypointIndex);
        float dx = to.x - from.x;
        float dy = to.y - from.y;
        if (dx * dx + dy * dy < 0.0001f && waypointIndex + 1 < path.size()) {
            to = path.get(waypointIndex + 1);
            dx = to.x - enemy.getX();
            dy = to.y - enemy.getY();
        }
        if (dx * dx + dy * dy < 0.0001f) {
            return 0f;
        }
        return (float) Math.atan2(dy, dx);
    }
}
