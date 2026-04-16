package com.ironhold.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.Objects;

/**
 * Базовый UI-слой: держит {@link Stage} и общий {@link Skin}.
 *
 * Экраны должны рендерить UI через stage, а виджеты создавать из skin.
 */
public final class UiLayer {

    private final Stage stage;
    private final Skin skin;

    public UiLayer(Skin skin) {
        this.stage = new Stage(new ScreenViewport());
        this.skin = Objects.requireNonNull(skin, "skin");
    }

    public Stage getStage() {
        return stage;
    }

    public Skin getSkin() {
        return skin;
    }

    public void act(float delta) {
        stage.act(delta);
    }

    public void draw() {
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
    }
}
