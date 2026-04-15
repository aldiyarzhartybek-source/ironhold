package com.ironhold.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.ironhold.game.screen.ScreenId;
import com.ironhold.game.screen.ScreenNavigator;

import java.util.Objects;

public final class MenuScreen extends ScreenAdapter {

    private final ScreenNavigator navigator;
    private final Stage stage;
    private final Skin skin;
    private final Texture uiTexture;

    public MenuScreen(ScreenNavigator navigator) {
        this.navigator = Objects.requireNonNull(navigator, "navigator");
        this.stage = new Stage(new ScreenViewport());
        this.skin = new Skin();
        this.uiTexture = createUiTexture();
        initSkin();
        initButtons();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.06f, 0.06f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        uiTexture.dispose();
    }

    private Texture createUiTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void initSkin() {
        skin.add("default-font", new BitmapFont());
        skin.add("button-base", uiTexture);

        Drawable up = skin.newDrawable("button-base", new Color(0.2f, 0.2f, 0.28f, 1f));
        Drawable over = skin.newDrawable("button-base", new Color(0.28f, 0.28f, 0.38f, 1f));
        Drawable down = skin.newDrawable("button-base", new Color(0.16f, 0.16f, 0.22f, 1f));

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.up = up;
        style.over = over;
        style.down = down;
        style.font = skin.getFont("default-font");
        skin.add("default", style);
    }

    private void initButtons() {
        TextButton startButton = new TextButton("Start", skin);
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                navigator.goTo(ScreenId.GAME);
            }
        });

        TextButton exitButton = new TextButton("Exit", skin);
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                Gdx.app.exit();
            }
        });

        Table root = new Table();
        root.setFillParent(true);
        root.defaults().width(220f).height(52f).pad(10f);
        root.add(startButton).row();
        root.add(exitButton);
        stage.addActor(root);
    }
}
