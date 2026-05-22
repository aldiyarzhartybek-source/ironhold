package com.ironhold.core.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.ironhold.game.model.PlacedTower;
import com.ironhold.ui.GameTheme;
import com.ironhold.ui.GameTheme.TowerStyle;

import java.util.List;

/**
 * Draws placed towers: coloured body (per type) + pulsing core.
 */
public final class TowerShapeRenderer {

    private static final int CORNER_SEGS = 10;
    private static final float PULSE_HZ = 1.4f;
    private static final float GLOW_ALPHA_SCALE = 0.8f;

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
            TowerStyle style = GameTheme.towerStyle(tower.getTowerId());
            float x = tower.getX();
            float y = tower.getY();
            drawTowerBody(tower.getTowerId(), x, y, style);
            drawPulsingCore(x, y + style.coreOffsetY, tower.getPulsePhaseSec(), style);
        }

        shapes.end();
        batch.begin();
        batch.setColor(GameTheme.TINT_WHITE);
    }

    private void drawTowerBody(String towerId, float x, float y, TowerStyle style) {
        if ("lightning_tower".equals(towerId)) {
            drawDiamond(x, y, style);
        } else if ("mortar_tower".equals(towerId)) {
            drawMortarHex(x, y, style);
        } else if ("flamethrower_tower".equals(towerId)) {
            drawFlameTriangle(x, y, style);
        } else {
            drawRoundedTower(x, y, style);
        }
    }

    private void drawRoundedTower(float x, float y, TowerStyle style) {
        float s  = GameTheme.Draw.TOWER_SIZE;
        float t  = GameTheme.Draw.TOWER_OUTLINE_THICK;
        float cr = GameTheme.Draw.TOWER_CORNER_R;
        float half = s * 0.5f;

        shapes.setColor(style.bodyOuter);
        drawRoundedRect(x - half, y - half, s, s, cr);

        float innerS  = s - t * 2f;
        float innerCr = Math.max(1f, cr - t * 0.6f);
        shapes.setColor(style.bodyInner);
        drawRoundedRect(x - half + t, y - half + t, innerS, innerS, innerCr);
    }

    private void drawFlameTriangle(float cx, float cy, TowerStyle style) {
        float half = GameTheme.Draw.TOWER_SIZE * 0.5f;
        float t = GameTheme.Draw.TOWER_OUTLINE_THICK;

        shapes.setColor(style.bodyOuter);
        drawFilledTriangleUp(cx, cy, half);

        shapes.setColor(style.bodyInner);
        drawFilledTriangleUp(cx, cy, Math.max(4f, half - t));
    }

    private void drawFilledTriangleUp(float cx, float cy, float half) {
        float topY = cy + half;
        float baseY = cy - half;
        shapes.triangle(cx, topY, cx - half, baseY, cx + half, baseY);
    }

    private void drawMortarHex(float cx, float cy, TowerStyle style) {
        float half = GameTheme.Draw.TOWER_SIZE * 0.5f * GameTheme.Draw.MORTAR_TOWER_SCALE;
        float t = GameTheme.Draw.TOWER_OUTLINE_THICK + 0.5f;

        shapes.setColor(style.bodyOuter);
        drawFilledHexagon(cx, cy, half);

        shapes.setColor(style.bodyInner);
        drawFilledHexagon(cx, cy, Math.max(4f, half - t));
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

    private void drawDiamond(float cx, float cy, TowerStyle style) {
        float half = GameTheme.Draw.TOWER_SIZE * 0.5f;
        float t = GameTheme.Draw.TOWER_OUTLINE_THICK;

        shapes.setColor(style.bodyOuter);
        drawFilledDiamond(cx, cy, half);

        shapes.setColor(style.bodyInner);
        drawFilledDiamond(cx, cy, Math.max(4f, half - t));
    }

    private void drawFilledDiamond(float cx, float cy, float half) {
        shapes.triangle(cx, cy, cx + half, cy, cx, cy + half);
        shapes.triangle(cx, cy, cx - half, cy, cx, cy + half);
        shapes.triangle(cx, cy, cx + half, cy, cx, cy - half);
        shapes.triangle(cx, cy, cx - half, cy, cx, cy - half);
    }

    private void drawPulsingCore(float cx, float cy, float pulsePhaseSec, TowerStyle style) {
        float sin = (float) Math.sin(pulsePhaseSec * PULSE_HZ * (Math.PI * 2.0));
        float intensity = 0.775f + 0.225f * sin;
        float radius = GameTheme.Draw.TOWER_CORE_RADIUS * style.coreRadiusMult * intensity;

        shapes.setColor(blendAlpha(style.coreGlow, intensity * GLOW_ALPHA_SCALE));
        shapes.circle(cx, cy, radius, 12);
        shapes.setColor(blendAlpha(style.coreHot, intensity));
        shapes.circle(cx, cy, radius * 0.45f, 10);
    }

    private void drawRoundedRect(float x, float y, float w, float h, float r) {
        shapes.rect(x + r, y, w - r * 2f, h);
        shapes.rect(x, y + r, r, h - r * 2f);
        shapes.rect(x + w - r, y + r, r, h - r * 2f);
        shapes.circle(x + r,         y + r,         r, CORNER_SEGS);
        shapes.circle(x + w - r,     y + r,         r, CORNER_SEGS);
        shapes.circle(x + r,         y + h - r,     r, CORNER_SEGS);
        shapes.circle(x + w - r,     y + h - r,     r, CORNER_SEGS);
    }

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
