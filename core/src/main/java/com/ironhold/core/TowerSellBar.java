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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.ironhold.game.GameFacade;
import com.ironhold.game.model.BuildSlot;
import com.ironhold.ui.GameTheme;

import java.util.Objects;

/**
 * Compact sell control shown below a selected placed tower (cross + refund only).
 */
public final class TowerSellBar {

    private static final float BAR_W = 48f;
    private static final float WORLD_BELOW_OFFSET = 36f;
    /** Extra screen-space drop so the control sits lower under the tower. */
    private static final float SCREEN_DROP = 12f;
    private static final float FONT_SCALE = 0.68f;
    private static final float PAD = 3f;
    private static final float X_GOLD_GAP = 2f;
    /** Lifts cross + gold together (screen space). */
    private static final float CONTENT_LIFT = 18f;
    /** Arm length along diagonal — elongated X. */
    private static final float X_ARM_LEN = 16f;
    /** Arm thickness — slimmer than before. */
    private static final float X_ARM_THICK = 2.7f;
    private static final float X_HIT_PAD = 6f;
    private static final Color NEON_X = Color.valueOf("ff4757");
    private static final Color NEON_X_HOVER = Color.valueOf("ff6677");
    private static final Color NEON_X_GLOW = new Color(1f, 0.35f, 0.42f, 0.32f);

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
    private boolean xHovered;
    private float fontScaleBackup;

    public TowerSellBar(GameFacade game, BitmapFont font, int screenW, int screenH) {
        this.game = Objects.requireNonNull(game, "game");
        this.font = Objects.requireNonNull(font, "font");
        this.shapes = new ShapeRenderer();
        this.uiCam = new OrthographicCamera();
        resize(screenW, screenH);
    }

    public void show(String slotId, float worldX, float worldY, OrthographicCamera worldCam) {
        Vector3 screen = new Vector3(worldX, worldY - WORLD_BELOW_OFFSET, 0f);
        worldCam.project(screen);
        screen.y -= SCREEN_DROP;
        screen.y += CONTENT_LIFT;

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
        this.xHovered = false;
    }

    public void hide() {
        visible = false;
        slotId = null;
        xHovered = false;
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
        if (hitSellButton(screenX, drawY)) {
            if (game.trySellPlacedTower(slotId)) {
                hide();
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

        int refund = game.getSellRefundForSlot(slotId);
        int mx = Gdx.input.getX();
        int my = screenH - Gdx.input.getY();
        xHovered = hitSellButton(mx, my);

        fontScaleBackup = font.getData().scaleX;
        font.getData().setScale(FONT_SCALE);
        String costLine = "\u25C6 " + refund;
        glyphLayout.setText(font, costLine);

        float[] layout = layoutPositions();
        float centerX = layout[0];
        float goldBaseline = layout[1];
        float xCy = layout[2];

        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        uiCam.setToOrtho(false, screenW, screenH);
        uiCam.update();
        shapes.setProjectionMatrix(uiCam.combined);
        shapes.begin(ShapeType.Filled);
        drawSellCross(centerX, xCy, xHovered);
        shapes.end();

        batch.setProjectionMatrix(uiCam.combined);
        batch.begin();
        font.setColor(GameTheme.GOLD);
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
        float h = PAD + X_ARM_LEN + X_GOLD_GAP + font.getCapHeight() + PAD;
        font.getData().setScale(1f);
        return h;
    }

    private boolean hitSellButton(int screenX, int drawY) {
        float[] layout = layoutPositions();
        float centerX = layout[0];
        float xCy = layout[2];
        float hitHalf = X_ARM_LEN * 0.5f + X_HIT_PAD;
        return screenX >= centerX - hitHalf && screenX <= centerX + hitHalf
            && drawY >= xCy - hitHalf && drawY <= xCy + hitHalf;
    }

    /** [0]=centerX, [1]=goldBaseline, [2]=crossCenterY — cross on top, gold centered below. */
    private float[] layoutPositions() {
        float centerX = barScreenX + BAR_W * 0.5f;
        float xCy = barScreenY + PAD + X_ARM_LEN * 0.5f;
        float crossBottom = xCy - X_ARM_LEN * 0.5f;
        float goldBaseline = crossBottom - X_GOLD_GAP;
        return new float[] {centerX, goldBaseline, xCy};
    }

    private void drawSellCross(float cx, float cy, boolean hovered) {
        Color fill = hovered ? NEON_X_HOVER : NEON_X;
        shapes.setColor(NEON_X_GLOW);
        drawCrossArmQuad(cx, cy, X_ARM_LEN + 0.8f, X_ARM_THICK + 0.6f, 45f);
        drawCrossArmQuad(cx, cy, X_ARM_LEN + 0.8f, X_ARM_THICK + 0.6f, -45f);
        shapes.setColor(fill);
        drawCrossArmQuad(cx, cy, X_ARM_LEN, X_ARM_THICK, 45f);
        drawCrossArmQuad(cx, cy, X_ARM_LEN, X_ARM_THICK, -45f);
    }

    private void drawCrossArmQuad(float cx, float cy, float length, float thickness, float degrees) {
        float rad = degrees * MathUtils.degreesToRadians;
        float ux = MathUtils.cos(rad);
        float uy = MathUtils.sin(rad);
        float px = -uy;
        float py = ux;
        float hl = length * 0.5f;
        float ht = thickness * 0.5f;

        float x1 = cx + ux * hl + px * ht;
        float y1 = cy + uy * hl + py * ht;
        float x2 = cx + ux * hl - px * ht;
        float y2 = cy + uy * hl - py * ht;
        float x3 = cx - ux * hl - px * ht;
        float y3 = cy - uy * hl - py * ht;
        float x4 = cx - ux * hl + px * ht;
        float y4 = cy - uy * hl + py * ht;
        shapes.triangle(x1, y1, x2, y2, x3, y3);
        shapes.triangle(x1, y1, x3, y3, x4, y4);
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
