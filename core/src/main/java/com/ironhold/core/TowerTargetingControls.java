package com.ironhold.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ironhold.game.GameFacade;
import com.ironhold.game.model.BuildSlot;
import com.ironhold.game.model.TowerTargetingPriority;
import com.ironhold.ui.UiLayer;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Compact vertical targeting picker to the right of a selected tower.
 */
public final class TowerTargetingControls {

    private static final float WORLD_RIGHT_OFFSET = 34f;
    private static final float SCREEN_GAP_RIGHT = 10f;
    private static final float BTN_W = 68f;
    private static final float BTN_H = 22f;
    private static final float BTN_PAD = 2f;
    private static final float LABEL_SCALE = 0.72f;
    private static final float LABEL_PAD_V = 3f;
    private static final float LABEL_PAD_H = 10f;

    /** Top-to-bottom: near, first, strong. */
    private static final TowerTargetingPriority[] UI_ORDER = {
        TowerTargetingPriority.NEAREST,
        TowerTargetingPriority.FIRST,
        TowerTargetingPriority.STRONGEST
    };

    private final GameFacade game;
    private final UiLayer ui;
    private final Table root;
    private final Map<TowerTargetingPriority, TextButton> buttons =
        new EnumMap<>(TowerTargetingPriority.class);

    private boolean visible;
    private String slotId;

    public TowerTargetingControls(GameFacade game) {
        this.game = Objects.requireNonNull(game, "game");
        this.ui = new UiLayer(game.getAssets().getSkin());
        this.root = new Table();
        this.root.setTouchable(Touchable.enabled);
        this.root.defaults().pad(BTN_PAD).width(BTN_W).height(BTN_H);

        for (TowerTargetingPriority mode : UI_ORDER) {
            TextButton button = new TextButton(mode.getUiLabel(), ui.getSkin(), "menu-button");
            button.getLabel().setColor(com.badlogic.gdx.graphics.Color.WHITE);
            button.getLabel().setFontScale(LABEL_SCALE);
            button.getLabelCell().pad(LABEL_PAD_V, LABEL_PAD_H, LABEL_PAD_V, LABEL_PAD_H);
            button.setTouchable(Touchable.enabled);
            final TowerTargetingPriority targetMode = mode;
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                    if (slotId == null) {
                        return;
                    }
                    game.setPlacedTowerTargeting(slotId, targetMode);
                    refreshButtonStyles();
                }
            });
            buttons.put(mode, button);
            root.add(button).uniformX().row();
        }

        ui.getStage().addActor(root);
        root.setVisible(false);
        visible = false;
    }

    public void show(String slotId, float worldX, float worldY, OrthographicCamera worldCam) {
        Vector3 screen = new Vector3(worldX + WORLD_RIGHT_OFFSET, worldY, 0f);
        worldCam.project(screen);
        screen.x += SCREEN_GAP_RIGHT;

        this.slotId = slotId;
        this.visible = true;
        root.setVisible(true);
        root.pack();

        float panelW = root.getWidth();
        float panelH = root.getHeight();
        float x = Math.max(8f, Math.min(screen.x, Gdx.graphics.getWidth() - panelW - 8f));
        float y = Math.max(8f, Math.min(screen.y - panelH * 0.5f, Gdx.graphics.getHeight() - panelH - 8f));
        root.setPosition(x, y);
        refreshButtonStyles();
    }

    public void hide() {
        visible = false;
        slotId = null;
        root.setVisible(false);
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

    public UiLayer getUi() {
        return ui;
    }

    public void act(float delta) {
        ui.act(delta);
    }

    public void draw() {
        if (!visible) {
            return;
        }
        ui.draw();
    }

    public void resize(int width, int height) {
        ui.resize(width, height);
    }

    public void dispose() {
        ui.dispose();
    }

    private void refreshButtonStyles() {
        TowerTargetingPriority active = slotId != null
            ? game.getPlacedTowerTargeting(slotId)
            : null;
        for (Map.Entry<TowerTargetingPriority, TextButton> entry : buttons.entrySet()) {
            TextButton button = entry.getValue();
            boolean isActive = entry.getKey() == active;
            if (isActive) {
                button.setStyle(ui.getSkin().get("targeting-button-active", TextButton.TextButtonStyle.class));
            } else {
                button.setStyle(ui.getSkin().get("menu-button", TextButton.TextButtonStyle.class));
            }
            button.getLabel().setColor(com.badlogic.gdx.graphics.Color.WHITE);
            button.getLabel().setFontScale(LABEL_SCALE);
            button.getLabelCell().pad(LABEL_PAD_V, LABEL_PAD_H, LABEL_PAD_V, LABEL_PAD_H);
        }
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
