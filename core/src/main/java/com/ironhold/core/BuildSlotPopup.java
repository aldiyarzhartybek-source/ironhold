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
import com.ironhold.game.model.Tower;
import com.ironhold.ui.GameTheme;

import java.util.List;
import java.util.Objects;

/**
 * Compact tower picker popup anchored near a build slot.
 */
public final class BuildSlotPopup {

    private static final int CORNER_SEGS = 8;

    private static final float ROW_W        = 164f;
    private static final float ROW_H        = 50f;
    private static final float ROW_GAP      = 6f;
    private static final float STACK_PAD    = 4f;
    private static final float ICON_COL_W   = 40f;
    private static final float TEXT_PAD     = 6f;
    private static final float CARD_PAD_V    = 8f;
    private static final float NAME_COST_GAP = 2f;
    private static final float ICON_SCALE   = 0.64f;
    private static final float HOVER_SCALE  = 1.05f;
    private static final float HOVER_LERP   = 14f;
    private static final float FONT_SCALE   = 0.65f;
    private static final float SLOT_OFFSET_X = 10f;

    private static final Color STACK_DIM     = new Color(0.04f, 0.05f, 0.12f, 0.28f);
    private static final Color CARD_NORMAL   = new Color(0.20f, 0.26f, 0.24f, 0.78f);
    private static final Color CARD_HOVER    = new Color(0.16f, 0.90f, 0.82f, 0.88f);
    private static final Color CARD_DISABLED = new Color(0.14f, 0.17f, 0.19f, 0.52f);
    private static final Color COST_OK       = GameTheme.GOLD;
    private static final Color COST_NO       = new Color(0.55f, 0.55f, 0.58f, 0.75f);
    /** Dark label on mint hover plate — teal-on-teal was invisible. */
    private static final Color NAME_ON_HOVER = new Color(0.04f, 0.14f, 0.12f, 1f);
    private static final Color NAME_OVER_ORANGE = GameTheme.ENEMY_ACCENT;

    private static final float PULSE_PHASE = 0.35f;

    private final GameFacade game;
    private final List<Tower> towers;
    private final BitmapFont font;
    private final ShapeRenderer shapes;
    private final OrthographicCamera uiCam;
    private final float[] cardScales;
    private final GlyphLayout glyphLayout = new GlyphLayout();

    private boolean visible;
    private String pendingSlotId;
    private float popupScreenX;
    private float popupScreenY;
    private int screenW;
    private int screenH;
    private int hoverIndex = -1;
    private float fontScaleBackup;

    public BuildSlotPopup(GameFacade game, BitmapFont font, int screenW, int screenH) {
        this.game = Objects.requireNonNull(game, "game");
        this.font = Objects.requireNonNull(font, "font");
        this.towers = game.getTowers();
        this.shapes = new ShapeRenderer();
        this.uiCam = new OrthographicCamera();
        this.cardScales = new float[Math.max(1, towers.size())];
        resize(screenW, screenH);
    }

    public void show(String slotId, float worldX, float worldY, OrthographicCamera camera) {
        Vector3 screen = new Vector3(worldX, worldY, 0f);
        camera.project(screen);

        this.popupScreenX = screen.x + SLOT_OFFSET_X;
        this.popupScreenY = screen.y - totalHeight() * 0.5f + ROW_H * 0.5f;

        this.popupScreenX = Math.min(popupScreenX, screenW - ROW_W - 8f);
        this.popupScreenX = Math.max(popupScreenX, 8f);
        this.popupScreenY = Math.min(popupScreenY, screenH - totalHeight() - 8f);
        this.popupScreenY = Math.max(popupScreenY, 8f);

        this.pendingSlotId = slotId;
        this.visible = true;
        this.hoverIndex = -1;
        for (int i = 0; i < cardScales.length; i++) {
            cardScales[i] = 1f;
        }
    }

    public void hide() {
        visible = false;
        pendingSlotId = null;
        hoverIndex = -1;
    }

    public boolean isVisible() {
        return visible;
    }

    public String getPendingSlotId() {
        return pendingSlotId;
    }

    public boolean handleTouchDown(int screenX, int gdxScreenY, OrthographicCamera camera) {
        if (!visible) {
            return false;
        }

        int drawY = screenH - gdxScreenY;
        int gold = game.getRuntimeView().getGold();

        for (int i = 0; i < towers.size(); i++) {
            if (!hitCard(screenX, drawY, i)) {
                continue;
            }
            Tower t = towers.get(i);
            if (gold >= t.getCost()) {
                game.selectTower(t.getId());
                BuildSlot slot = findSlot(pendingSlotId);
                if (slot != null) {
                    game.tryPlaceTower(slot.getX(), slot.getY(), t.getId());
                }
            }
            hide();
            return true;
        }

        hide();
        return false;
    }

    public void render(SpriteBatch batch) {
        if (!visible || towers.isEmpty()) {
            return;
        }

        float dt = Gdx.graphics.getDeltaTime();
        updateHover(dt);

        int gold = game.getRuntimeView().getGold();

        fontScaleBackup = font.getData().scaleX;
        font.getData().setScale(fontScaleBackup * FONT_SCALE);
        final float capH = font.getCapHeight();
        final float lineH = font.getLineHeight();

        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        uiCam.setToOrtho(false, screenW, screenH);
        uiCam.update();
        shapes.setProjectionMatrix(uiCam.combined);
        shapes.begin(ShapeType.Filled);

        drawSolidRect(
            popupScreenX - STACK_PAD, popupScreenY - STACK_PAD,
            ROW_W + STACK_PAD * 2f, totalHeight() + STACK_PAD * 2f, STACK_DIM);

        for (int i = 0; i < towers.size(); i++) {
            Tower t = towers.get(i);
            boolean afford = gold >= t.getCost();
            boolean hover = i == hoverIndex && afford;
            float scale = cardScales[i];

            float cx = popupScreenX + ROW_W * 0.5f;
            float cy = cardCenterY(i);
            float cw = ROW_W * scale;
            float ch = ROW_H * scale;
            float left = cx - cw * 0.5f;
            float bottom = cy - ch * 0.5f;

            Color cardFill = afford
                ? (hover ? CARD_HOVER : CARD_NORMAL)
                : CARD_DISABLED;
            shapes.setColor(0f, 0f, 0f, 0.20f);
            drawSolidRect(left + 2f, bottom - 2f, cw, ch);

            drawSolidRect(left, bottom, cw, ch, cardFill);

            float iconCx = left + ICON_COL_W * 0.5f;
            float iconCy = cy;
            drawMiniTower(t.getId(), iconCx, iconCy);

        }

        shapes.end();

        batch.begin();
        for (int i = 0; i < towers.size(); i++) {
            Tower t = towers.get(i);
            boolean afford = gold >= t.getCost();
            boolean hover = i == hoverIndex && afford;

            float cx = popupScreenX + ROW_W * 0.5f;
            float cy = cardCenterY(i);
            float scale = cardScales[i];
            float cw = ROW_W * scale;
            float ch = ROW_H * scale;
            float left = cx - cw * 0.5f;
            float cardBottom = cy - ch * 0.5f;
            float cardTop = cardBottom + ch;
            float textLeft = left + ICON_COL_W + TEXT_PAD;
            float textRight = left + cw - TEXT_PAD;

            float[] baselines = textBaselines(cardBottom, cardTop, capH, lineH);
            float nameBaseline = baselines[0];
            float costBaseline = baselines[1];

            String name = displayName(t.getId());
            font.setColor(afford
                ? (hover ? NAME_ON_HOVER : GameTheme.UI_TEXT)
                : COST_NO);
            glyphLayout.setText(font, name);
            float nameX = textLeft;
            if (glyphLayout.width > textRight - textLeft) {
                nameX = textRight - glyphLayout.width;
            }
            font.draw(batch, name, nameX, nameBaseline);

            if (hover) {
                font.setColor(NAME_OVER_ORANGE);
            } else {
                font.setColor(afford ? COST_OK : COST_NO);
            }
            String costLine = "\u25C6 " + t.getCost();
            glyphLayout.setText(font, costLine);
            float costX = textLeft;
            if (glyphLayout.width > textRight - textLeft) {
                costX = textRight - glyphLayout.width;
            }
            font.draw(batch, costLine, costX, costBaseline);
        }

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

    private void updateHover(float dt) {
        int mx = Gdx.input.getX();
        int my = screenH - Gdx.input.getY();
        int nextHover = -1;
        for (int i = 0; i < towers.size(); i++) {
            if (hitCard(mx, my, i)) {
                nextHover = i;
                break;
            }
        }
        hoverIndex = nextHover;

        for (int i = 0; i < towers.size(); i++) {
            float target = (i == hoverIndex) ? HOVER_SCALE : 1f;
            cardScales[i] = MathUtils.lerp(cardScales[i], target, Math.min(1f, dt * HOVER_LERP));
        }
    }

    private boolean hitCard(int screenX, int drawY, int index) {
        float left = popupScreenX;
        float bottom = cardBottomY(index);
        return screenX >= left && screenX <= left + ROW_W
            && drawY >= bottom && drawY <= bottom + ROW_H;
    }

    private float totalHeight() {
        if (towers.isEmpty()) {
            return 0f;
        }
        return towers.size() * ROW_H + (towers.size() - 1) * ROW_GAP;
    }

    private float cardBottomY(int index) {
        float top = popupScreenY + totalHeight();
        return top - (index + 1) * ROW_H - index * ROW_GAP;
    }

    private float cardCenterY(int index) {
        return cardBottomY(index) + ROW_H * 0.5f;
    }

    /** [0] = название, [1] = «◆ 75» — обе строки по центру карточки по вертикали. */
    private static float[] textBaselines(float cardBottom, float cardTop,
                                         float capH, float lineH) {
        float mid = (cardBottom + cardTop) * 0.5f;
        float totalTextH = capH + lineH;
        float nameBaseline = mid + totalTextH * 0.5f;
        float costBaseline = nameBaseline - lineH - NAME_COST_GAP;
        return new float[] {nameBaseline, costBaseline};
    }

    private BuildSlot findSlot(String slotId) {
        if (slotId == null) {
            return null;
        }
        for (BuildSlot slot : game.getBuildSlots()) {
            if (slot.getSlotId().equals(slotId)) {
                return slot;
            }
        }
        return null;
    }

    private void drawMiniTower(String towerId, float cx, float cy) {
        switch (towerId) {
            case "lightning_tower":
                drawMiniLightning(cx, cy);
                break;
            case "mortar_tower":
                drawMiniMortar(cx, cy);
                break;
            case "flamethrower_tower":
                drawMiniFlame(cx, cy);
                break;
            default:
                drawMiniDart(cx, cy);
                break;
        }
    }

    private void drawMiniDart(float cx, float cy) {
        float s = GameTheme.Draw.TOWER_SIZE * ICON_SCALE;
        float t = GameTheme.Draw.TOWER_OUTLINE_THICK * ICON_SCALE;
        float cr = GameTheme.Draw.TOWER_CORNER_R * ICON_SCALE;
        float half = s * 0.5f;

        shapes.setColor(GameTheme.TOWER_OUTLINE);
        drawRoundedRect(cx - half, cy - half, s, s, cr);

        float innerS = s - t * 2f;
        float innerCr = Math.max(1f, cr - t * 0.6f);
        shapes.setColor(GameTheme.SLOT_RECESS);
        drawRoundedRect(cx - half + t, cy - half + t, innerS, innerS, innerCr);

        drawMiniCore(cx, cy,
            GameTheme.TOWER_CORE_BASE, GameTheme.TOWER_CORE_HIGHLIGHT, 1f);
    }

    private void drawMiniFlame(float cx, float cy) {
        float half = GameTheme.Draw.TOWER_SIZE * ICON_SCALE * 0.5f;
        float t = GameTheme.Draw.TOWER_OUTLINE_THICK * ICON_SCALE;

        shapes.setColor(GameTheme.FLAME_OUTLINE);
        drawFilledTriangleUp(cx, cy, half);

        shapes.setColor(GameTheme.SLOT_RECESS);
        drawFilledTriangleUp(cx, cy, Math.max(3f, half - t));

        float coreY = cy - GameTheme.Draw.TOWER_SIZE * ICON_SCALE * 0.08f;
        drawMiniCore(cx, coreY, GameTheme.FLAME_CORE_GLOW, GameTheme.FLAME_CORE, 1.2f);
    }

    private void drawMiniMortar(float cx, float cy) {
        float half = GameTheme.Draw.TOWER_SIZE * ICON_SCALE * 0.5f
            * GameTheme.Draw.MORTAR_TOWER_SCALE;
        float t = (GameTheme.Draw.TOWER_OUTLINE_THICK + 0.5f) * ICON_SCALE;

        shapes.setColor(GameTheme.MORTAR_OUTLINE);
        drawFilledHexagon(cx, cy, half);

        shapes.setColor(GameTheme.SLOT_RECESS);
        drawFilledHexagon(cx, cy, Math.max(3f, half - t));

        drawMiniCore(cx, cy, GameTheme.MORTAR_CORE_GLOW, GameTheme.MORTAR_CORE, 1.35f);
    }

    private void drawMiniLightning(float cx, float cy) {
        float half = GameTheme.Draw.TOWER_SIZE * ICON_SCALE * 0.5f;
        float t = GameTheme.Draw.TOWER_OUTLINE_THICK * ICON_SCALE;

        shapes.setColor(GameTheme.LIGHTNING_OUTLINE);
        drawFilledDiamond(cx, cy, half);

        shapes.setColor(GameTheme.SLOT_RECESS);
        drawFilledDiamond(cx, cy, Math.max(3f, half - t));

        drawMiniCore(cx, cy, GameTheme.LIGHTNING_GLOW, GameTheme.LIGHTNING_CORE, 1f);
    }

    private void drawMiniCore(float cx, float cy, Color glow, Color core, float radiusMult) {
        float sin = (float) Math.sin(PULSE_PHASE * 1.4f * (Math.PI * 2.0));
        float intensity = 0.775f + 0.225f * sin;
        float radius = GameTheme.Draw.TOWER_CORE_RADIUS * ICON_SCALE * radiusMult * intensity;

        shapes.setColor(blendAlpha(glow, intensity * 0.85f));
        shapes.circle(cx, cy, radius, 10);
        shapes.setColor(blendAlpha(core, intensity));
        shapes.circle(cx, cy, radius * 0.5f, 8);
    }

    private void drawFilledTriangleUp(float cx, float cy, float half) {
        float topY = cy + half;
        float baseY = cy - half;
        shapes.triangle(cx, topY, cx - half, baseY, cx + half, baseY);
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

    private void drawFilledDiamond(float cx, float cy, float half) {
        shapes.triangle(cx, cy, cx + half, cy, cx, cy + half);
        shapes.triangle(cx, cy, cx - half, cy, cx, cy + half);
        shapes.triangle(cx, cy, cx + half, cy, cx, cy - half);
        shapes.triangle(cx, cy, cx - half, cy, cx, cy - half);
    }

    private void drawSolidRect(float x, float y, float w, float h) {
        shapes.rect(x, y, w, h);
    }

    private void drawSolidRect(float x, float y, float w, float h, Color c) {
        shapes.setColor(c);
        drawSolidRect(x, y, w, h);
    }

    private void drawRoundedRect(float x, float y, float w, float h, float r) {
        shapes.rect(x + r, y, w - r * 2f, h);
        shapes.rect(x, y + r, r, h - r * 2f);
        shapes.rect(x + w - r, y + r, r, h - r * 2f);
        shapes.circle(x + r, y + r, r, CORNER_SEGS);
        shapes.circle(x + w - r, y + r, r, CORNER_SEGS);
        shapes.circle(x + r, y + h - r, r, CORNER_SEGS);
        shapes.circle(x + w - r, y + h - r, r, CORNER_SEGS);
    }

    private static Color blendAlpha(Color source, float factor) {
        Color c = new Color(source);
        float a = source.a * factor;
        if (a < 0f) a = 0f;
        if (a > 1f) a = 1f;
        c.a = a;
        return c;
    }

    private static String displayName(String towerId) {
        if ("basic_tower".equals(towerId)) return "Dart";
        if ("mortar_tower".equals(towerId)) return "Mortar";
        if ("lightning_tower".equals(towerId)) return "Lightning";
        if ("flamethrower_tower".equals(towerId)) return "Flame";
        if (towerId == null || towerId.isEmpty()) return "Tower";
        String[] parts = towerId.split("_");
        String first = parts[0];
        return Character.toUpperCase(first.charAt(0)) + first.substring(1);
    }
}
