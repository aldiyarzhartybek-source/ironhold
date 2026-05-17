package com.ironhold.core;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ironhold.game.GameFacade;
import com.ironhold.level.LevelStatus;
import com.ironhold.ui.UiLayer;

import java.util.Objects;

/**
 * HUD toggle for game simulation speed (x1 / x2). Wall-clock level timer is unaffected.
 */
public final class GameSpeedControls {

    private static final float BUTTON_WIDTH = 160f;
    private static final float BUTTON_HEIGHT = 44f;
    private static final float BUTTON_BOTTOM_PAD = 24f;
    private static final float BUTTON_RIGHT_PAD = 24f;

    private final GameFacade game;
    private final UiLayer ui;
    private final TextButton speedButton;

    public GameSpeedControls(GameFacade game) {
        this.game = Objects.requireNonNull(game, "game");
        this.ui = new UiLayer(game.getAssets().getSkin());
        this.speedButton = new TextButton("Speed x1 [T]", ui.getSkin());
        this.speedButton.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        this.speedButton.setTouchable(Touchable.enabled);
        this.speedButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                toggleSpeed();
            }
        });
        ui.getStage().addActor(speedButton);
    }

    public void sync(boolean endOverlayVisible) {
        boolean running = game.getRuntimeLevelState().getStatus() == LevelStatus.RUNNING;
        speedButton.setVisible(running && !endOverlayVisible);
        if (!speedButton.isVisible()) {
            return;
        }
        speedButton.setText(game.isFastTimeScale() ? "Speed x2" : "Speed x1 [T]");
    }

    public void toggleSpeed() {
        game.toggleTimeScale();
        speedButton.setText(game.isFastTimeScale() ? "Speed x2" : "Speed x1 [T]");
    }

    public UiLayer getUi() {
        return ui;
    }

    public void act(float delta) {
        ui.act(delta);
    }

    public void draw() {
        ui.draw();
    }

    public void resize(int width, int height) {
        ui.resize(width, height);
        speedButton.setPosition(width - BUTTON_WIDTH - BUTTON_RIGHT_PAD, BUTTON_BOTTOM_PAD);
        speedButton.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    public void dispose() {
        ui.dispose();
    }
}
