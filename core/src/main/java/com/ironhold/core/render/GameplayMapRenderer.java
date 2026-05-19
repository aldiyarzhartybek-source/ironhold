package com.ironhold.core.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.ironhold.game.GameRuntimeView;
import com.ironhold.game.model.BuildSlot;
import com.ironhold.ui.GameTheme;

import java.util.List;

/**
 * Draws enemy path, build sockets, spawn and base markers (Stage 5 map visuals).
 */
public final class GameplayMapRenderer {

    private static final float LANE_DOT_RADIUS = 2.5f;

    private final ShapeRenderer shapes;

    public GameplayMapRenderer() {
        this.shapes = new ShapeRenderer();
    }

    public void render(SpriteBatch batch, Matrix4 projectionMatrix, GameRuntimeView view) {
        List<Vector2> path = view.getEnemyPath();
        if (path.isEmpty()) {
            return;
        }

        batch.end();
        shapes.setProjectionMatrix(projectionMatrix);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        drawPath(path);
        drawBuildSlots(view.getBuildSlots());
        drawSpawnAndBase(path);

        shapes.end();
        batch.begin();
        batch.setColor(GameTheme.TINT_WHITE);
    }

    public void dispose() {
        shapes.dispose();
    }

    private void drawPath(List<Vector2> path) {
        if (path.size() < 2) {
            return;
        }
        for (int i = 0; i < path.size() - 1; i++) {
            Vector2 from = path.get(i);
            Vector2 to = path.get(i + 1);
            drawThickSegment(from.x, from.y, to.x, to.y, GameTheme.Draw.PATH_OUTER_HALF_WIDTH, GameTheme.PATH_OUTER);
            drawThickSegment(from.x, from.y, to.x, to.y, GameTheme.Draw.PATH_INNER_HALF_WIDTH, GameTheme.PATH_BODY);
            drawLaneDots(from, to);
        }
        for (int i = 1; i < path.size() - 1; i++) {
            Vector2 joint = path.get(i);
            shapes.setColor(GameTheme.PATH_BODY);
            shapes.circle(joint.x, joint.y, GameTheme.Draw.PATH_INNER_HALF_WIDTH, GameTheme.Draw.CIRCLE_SEGMENTS);
        }
    }

    private void drawLaneDots(Vector2 from, Vector2 to) {
        float dx = to.x - from.x;
        float dy = to.y - from.y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length < 0.01f) {
            return;
        }
        int markerCount = Math.max(1, (int) (length / GameTheme.Draw.ROAD_MARKER_STEP));
        shapes.setColor(GameTheme.PATH_LANE);
        for (int m = 0; m <= markerCount; m++) {
            float t = (float) m / markerCount;
            float x = from.x + dx * t;
            float y = from.y + dy * t;
            shapes.circle(x, y, LANE_DOT_RADIUS, GameTheme.Draw.CIRCLE_SEGMENTS);
        }
    }

    private void drawThickSegment(float x1, float y1, float x2, float y2, float halfWidth, Color color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length < 0.01f) {
            return;
        }
        float nx = -dy / length * halfWidth;
        float ny = dx / length * halfWidth;
        shapes.setColor(color);
        shapes.triangle(x1 + nx, y1 + ny, x1 - nx, y1 - ny, x2 + nx, y2 + ny);
        shapes.triangle(x2 + nx, y2 + ny, x1 - nx, y1 - ny, x2 - nx, y2 - ny);
    }

    private void drawBuildSlots(List<BuildSlot> slots) {
        for (BuildSlot slot : slots) {
            if (slot.isOccupied()) {
                drawOccupiedSocket(slot.getX(), slot.getY());
            } else {
                drawFreeSocket(slot.getX(), slot.getY());
            }
        }
    }

    private void drawFreeSocket(float x, float y) {
        shapes.setColor(GameTheme.SLOT_FREE);
        shapes.circle(x, y, GameTheme.Draw.SLOT_RING_RADIUS, GameTheme.Draw.CIRCLE_SEGMENTS);
        shapes.setColor(GameTheme.SLOT_CORE);
        shapes.circle(x, y, GameTheme.Draw.SLOT_INNER_RADIUS, GameTheme.Draw.CIRCLE_SEGMENTS);
    }

    private void drawOccupiedSocket(float x, float y) {
        shapes.setColor(GameTheme.SLOT_OCCUPIED);
        shapes.circle(x, y, GameTheme.Draw.SLOT_OCCUPIED_RING_RADIUS, GameTheme.Draw.CIRCLE_SEGMENTS);
        shapes.setColor(GameTheme.SLOT_CORE);
        shapes.circle(x, y, GameTheme.Draw.SLOT_INNER_RADIUS + 1f, GameTheme.Draw.CIRCLE_SEGMENTS);
        shapes.setColor(GameTheme.withAlpha(GameTheme.TOWER_BLUE, 0.55f));
        float cross = 6f;
        shapes.rect(x - cross, y - 1f, cross * 2f, 2f);
        shapes.rect(x - 1f, y - cross, 2f, cross * 2f);
    }

    private void drawSpawnAndBase(List<Vector2> path) {
        Vector2 spawn = path.get(0);
        Vector2 base = path.get(path.size() - 1);
        Vector2 firstStep = path.size() > 1 ? path.get(1) : spawn;

        drawSpawnGate(spawn, firstStep);
        drawBaseFortress(base);
    }

    private void drawSpawnGate(Vector2 spawn, Vector2 toward) {
        float dx = toward.x - spawn.x;
        float dy = toward.y - spawn.y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length < 0.01f) {
            dx = 1f;
            dy = 0f;
            length = 1f;
        }
        float ux = dx / length;
        float uy = dy / length;
        float px = -uy;
        float py = ux;
        float r = GameTheme.Draw.SPAWN_MARKER_RADIUS;
        float tipX = spawn.x + ux * r * 1.35f;
        float tipY = spawn.y + uy * r * 1.35f;
        float backX = spawn.x - ux * r * 0.55f;
        float backY = spawn.y - uy * r * 0.55f;

        shapes.setColor(GameTheme.SPAWN_MARKER);
        shapes.triangle(tipX, tipY, backX + px * r, backY + py * r, backX - px * r, backY - py * r);
        shapes.setColor(GameTheme.withAlpha(GameTheme.GOLD, 0.35f));
        shapes.circle(spawn.x, spawn.y, r * 0.55f, GameTheme.Draw.CIRCLE_SEGMENTS);
    }

    private void drawBaseFortress(Vector2 base) {
        float radius = GameTheme.Draw.BASE_MARKER_RADIUS;
        shapes.setColor(GameTheme.withAlpha(GameTheme.BASE_MARKER, 0.45f));
        shapes.circle(base.x, base.y, radius + 4f, GameTheme.Draw.CIRCLE_SEGMENTS);
        shapes.setColor(GameTheme.BASE_MARKER);
        drawHexagon(base.x, base.y, radius);
        shapes.setColor(GameTheme.withAlpha(GameTheme.BACKGROUND, 0.85f));
        shapes.circle(base.x, base.y, radius * 0.45f, GameTheme.Draw.CIRCLE_SEGMENTS);
    }

    private void drawHexagon(float centerX, float centerY, float radius) {
        for (int i = 0; i < 6; i++) {
            float a0 = (float) (Math.PI / 6f + i * Math.PI / 3f);
            float a1 = (float) (Math.PI / 6f + (i + 1) * Math.PI / 3f);
            float x0 = centerX + (float) Math.cos(a0) * radius;
            float y0 = centerY + (float) Math.sin(a0) * radius;
            float x1 = centerX + (float) Math.cos(a1) * radius;
            float y1 = centerY + (float) Math.sin(a1) * radius;
            shapes.triangle(centerX, centerY, x0, y0, x1, y1);
        }
    }
}
