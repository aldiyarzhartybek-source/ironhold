package com.ironhold.core;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ironhold.game.GameFacade;
import com.ironhold.game.GameMode;
import com.ironhold.game.GameRuntimeView;
import com.ironhold.level.LevelStatus;
import com.ironhold.ui.UiLayer;

import java.util.Objects;

/**
 * Gameplay HUD control to start the next wave (Scene2D button + label sync).
 */
public final class WaveStartControls {

    private static final float BUTTON_WIDTH = 340f;
    private static final float BUTTON_HEIGHT = 52f;
    private static final float BUTTON_BOTTOM_PAD = 24f;

    private final GameFacade game;
    private final UiLayer ui;
    private final TextButton startWaveButton;

    public WaveStartControls(GameFacade game) {
        this.game = Objects.requireNonNull(game, "game");
        this.ui = new UiLayer(game.getAssets().getSkin());
        this.startWaveButton = new TextButton("Start wave 1 [Space]", ui.getSkin());
        this.startWaveButton.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        this.startWaveButton.setTouchable(Touchable.enabled);
        this.startWaveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                tryStartNextWave();
            }
        });
        ui.getStage().addActor(startWaveButton);
    }

    public void sync(GameRuntimeView view, boolean endOverlayVisible) {
        boolean running = view.getLevelState().getStatus() == LevelStatus.RUNNING;
        boolean manualWaves = game.getGameMode() != GameMode.RUSH;
        boolean visible = running && !endOverlayVisible && manualWaves;
        startWaveButton.setVisible(visible);
        if (!visible) {
            return;
        }

        int waveNumber = view.getLevelState().getCurrentWaveNumber();
        boolean canStart = game.canStartNextWave();
        startWaveButton.setDisabled(!canStart);
        startWaveButton.setText("Start wave " + waveNumber + " [Space]");
    }

    public void tryStartNextWave() {
        if (game.getGameMode() == GameMode.RUSH) {
            return;
        }
        if (game.canStartNextWave()) {
            game.startNextWave();
        }
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
        startWaveButton.setPosition((width - BUTTON_WIDTH) * 0.5f, BUTTON_BOTTOM_PAD);
        startWaveButton.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    public void dispose() {
        ui.dispose();
    }
}
