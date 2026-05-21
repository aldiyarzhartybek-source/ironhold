package com.ironhold.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.ironhold.game.GameFacade;
import com.ironhold.game.model.BuildSlot;
import com.ironhold.game.model.PlacedTower;
import com.ironhold.ui.GameTheme;

import java.util.Objects;

/**
 * Compact upgrade control shown above a selected placed tower (arrow + cost).
 */
public final class TowerUpgradeBar {

    private static final float BAR_W = 48f;
    private static final float WORLD_ABOVE_OFFSET = 34f;
    private static final float SCREEN_LIFT = 10f;
    private static final float FONT_SCALE = 0.68f;
    private static final float PAD = 3f;
    private static final float ARROW_GOLD_GAP = 3f;
    private static final float ARROW_HALF_W = 9f;
    private static final float ARROW_HEIGHT = 17f;
    private static final float ARROW_HIT_PAD = 7f;
    private static final Color NEON_UP = Color.valueOf("00adb5");
    private static final Color NEON_UP_HOVER = Color.valueOf("33c4cb");
    private static final Color NEON_UP_GLOW = new Color(0f, 0.68f, 0.72f, 0.32f);
    private static final Color DISABLED = Color.valueOf("5a5a6e");

    private final GameFacade game;
    private final BitmapFont font;
    private final ShapeRenderer shapes;
    private final OrthographicCamera uiCam;
    private final GlyphLayout glyphLayout = new GlyphLayout();

    private boolean visible;
    private String slotId;
    private float barScreenX;
    private float barScreenY;
    private float barScreenH;
    private int screenW;
    private int screenH;
    private boolean arrowHovered;
    private float fontScaleBackup;
    private OrthographicCamera worldCam;

    public TowerUpgradeBar(GameFacade game, BitmapFont font, int screenW, int screenH) {
        this.game = Objects.requireNonNull(game, "game");
        this.font = Objects.requireNonNull(font, "font");
        this.shapes = new ShapeRenderer();
        this.uiCam = new OrthographicCamera();
        resize(screenW, screenH);
    }

    public void show(String slotId, float worldX, float worldY, OrthographicCamera worldCam) {
        this.worldCam = worldCam;
        Vector3 screen = new Vector3(worldX, worldY + WORLD_ABOVE_OFFSET, 0f);
        worldCam.project(screen);
        screen.y += SCREEN_LIFT;

        float contentH = estimateContentHeight();
        this.slotId = slotId;
        this.barScreenX = screen.x - BAR_W * 0.5f;
        this.barScreenY = screen.y - contentH * 0.5f;
        this.barScreenH = contentH;

        this.barScreenX = Math.min(barScreenX, screenW - BAR_W - 8f);
        this.barScreenX = Math.max(barScreenX, 8f);
        this.barScreenY = Math.min(barScreenY, screenH - contentH - 8f);
        this.barScreenY = Math.max(barScreenY, 8f);

        this.visible = true;
        this.arrowHovered = false;
    }

    public void hide() {
        visible = false;
        slotId = null;
        arrowHovered = false;
    }

    public boolean isVisible() {
        return visible;
    }

    public void syncSelection(OrthographicCamera worldCam) {
        if (!visible || slotId == null) {
            return;
        }
        BuildSlot slot = findSlot(slotId);
        if (slot == null || !slot.isOccupied()) {
            hide();
            return;
        }
        show(slotId, slot.getX(), slot.getY(), worldCam);
    }

    public boolean handleTouchDown(int screenX, int gdxScreenY) {
        if (!visible) {
            return false;
        }
        int drawY = screenH - gdxScreenY;
        if (hitUpgradeButton(screenX, drawY) && canUpgrade()) {
            if (game.tryUpgradePlacedTower(slotId) && worldCam != null) {
                BuildSlot slot = findSlot(slotId);
                if (slot != null && slot.isOccupied()) {
                    show(slotId, slot.getX(), slot.getY(), worldCam);
                }
            }
            return true;
        }
        if (screenX >= barScreenX && screenX <= barScreenX + BAR_W
            && drawY >= barScreenY && drawY <= barScreenY + barScreenH) {
            return true;
        }
        hide();
        return false;
    }

    public void render(SpriteBatch batch) {
        if (!visible || slotId == null) {
            return;
        }

        boolean maxed = game.getPlacedTowerLevel(slotId) >= PlacedTower.MAX_LEVEL;
        int cost = game.getPlacedTowerUpgradeCost(slotId);
        boolean affordable = !maxed && game.canAffordPlacedTowerUpgrade(slotId);

        int mx = Gdx.input.getX();
        int my = screenH - Gdx.input.getY();
        arrowHovered = !maxed && hitUpgradeButton(mx, my);

        fontScaleBackup = font.getData().scaleX;
        font.getData().setScale(FONT_SCALE);
        String costLine = maxed ? "MAX" : "\u25C6 " + cost;
        glyphLayout.setText(font, costLine);

        float[] layout = layoutPositions();
        float centerX = layout[0];
        float goldBaseline = layout[1];
        float arrowCy = layout[2];

        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        uiCam.setToOrtho(false, screenW, screenH);
        uiCam.update();
        shapes.setProjectionMatrix(uiCam.combined);
        shapes.begin(ShapeType.Filled);
        if (!maxed) {
            drawUpArrow(centerX, arrowCy, arrowHovered && affordable, !affordable);
        }
        shapes.end();

        batch.setProjectionMatrix(uiCam.combined);
        batch.begin();
        if (maxed) {
            font.setColor(DISABLED);
        } else {
            font.setColor(affordable ? GameTheme.GOLD : DISABLED);
        }
        font.draw(batch, costLine, centerX - glyphLayout.width * 0.5f, goldBaseline);
        font.getData().setScale(fontScaleBackup);
        font.setColor(Color.WHITE);
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void resize(int w, int h) {
        this.screenW = w;
        this.screenH = h;
        this.uiCam.setToOrtho(false, w, h);
        this.uiCam.update();
    }

    public void dispose() {
        shapes.dispose();
    }

    private float estimateContentHeight() {
        font.getData().setScale(FONT_SCALE);
        float h = PAD + font.getCapHeight() + ARROW_GOLD_GAP + ARROW_HEIGHT + PAD;
        font.getData().setScale(1f);
        return h;
    }

    private boolean canUpgrade() {
        return game.getPlacedTowerUpgradeCost(slotId) > 0
            && game.canAffordPlacedTowerUpgrade(slotId);
    }

    private boolean hitUpgradeButton(int screenX, int drawY) {
        if (game.getPlacedTowerUpgradeCost(slotId) <= 0) {
            return false;
        }
        float[] layout = layoutPositions();
        float centerX = layout[0];
        float arrowCy = layout[2];
        float hitHalfW = ARROW_HALF_W + ARROW_HIT_PAD;
        float hitHalfH = ARROW_HEIGHT * 0.5f + ARROW_HIT_PAD;
        return screenX >= centerX - hitHalfW && screenX <= centerX + hitHalfW
            && drawY >= arrowCy - hitHalfH && drawY <= arrowCy + hitHalfH;
    }

    /** [0]=centerX, [1]=goldBaseline, [2]=arrowCenterY — gold внизу, стрелка наверху. */
    private float[] layoutPositions() {
        float centerX = barScreenX + BAR_W * 0.5f;
        float goldBaseline = barScreenY + PAD + font.getCapHeight();
        float arrowCy = goldBaseline + ARROW_GOLD_GAP + ARROW_HEIGHT * 0.5f;
        return new float[] {centerX, goldBaseline, arrowCy};
    }

    /** Заполненный треугольник ▲ (не крестик). */
    private void drawUpArrow(float cx, float cy, boolean hovered, boolean dimmed) {
        Color fill = dimmed ? DISABLED : (hovered ? NEON_UP_HOVER : NEON_UP);
        Color glow = dimmed ? new Color(0.35f, 0.35f, 0.42f, 0.22f) : NEON_UP_GLOW;

        float tipY = cy + ARROW_HEIGHT * 0.5f;
        float baseY = cy - ARROW_HEIGHT * 0.5f;
        float glowW = ARROW_HALF_W + 1.2f;
        float glowH = ARROW_HEIGHT + 1.2f;
        float glowTipY = cy + glowH * 0.5f;
        float glowBaseY = cy - glowH * 0.5f;

        shapes.setColor(glow);
        shapes.triangle(cx, glowTipY, cx - glowW, glowBaseY, cx + glowW, glowBaseY);
        shapes.setColor(fill);
        shapes.triangle(cx, tipY, cx - ARROW_HALF_W, baseY, cx + ARROW_HALF_W, baseY);
    }

    private BuildSlot findSlot(String id) {
        if (id == null) {
            return null;
        }
        for (BuildSlot slot : game.getBuildSlots()) {
            if (slot.getSlotId().equals(id)) {
                return slot;
            }
        }
        return null;
    }
}
