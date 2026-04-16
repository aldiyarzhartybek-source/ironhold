package com.ironhold.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
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
    private final Texture uiTexture;

    public UiLayer() {
        this.stage = new Stage(new ScreenViewport());
        this.uiTexture = createUiTexture();
        this.skin = createSkin(uiTexture);
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
        skin.dispose();
        uiTexture.dispose();
    }

    private static Texture createUiTexture() {
        // 1x1 текстура — база для "плоских" прямоугольных кнопок (до ассетов в задаче 6).
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private static Skin createSkin(Texture uiTexture) {
        Objects.requireNonNull(uiTexture, "uiTexture");

        Skin skin = new Skin();
        BitmapFont font = new BitmapFont();
        skin.add("default-font", font);

        // Label style (критерий задачи: "кнопки/лейблы отображаются корректно").
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        skin.add("label", labelStyle);

        // Button style.
        skin.add("button-base", uiTexture);
        Drawable up = skin.newDrawable("button-base", new Color(0.2f, 0.2f, 0.28f, 1f));
        Drawable over = skin.newDrawable("button-base", new Color(0.28f, 0.28f, 0.38f, 1f));
        Drawable down = skin.newDrawable("button-base", new Color(0.16f, 0.16f, 0.22f, 1f));

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up = up;
        buttonStyle.over = over;
        buttonStyle.down = down;
        buttonStyle.font = font;
        skin.add("default", buttonStyle);

        return skin;
    }
}
