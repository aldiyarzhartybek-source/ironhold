package com.ironhold.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ironhold.ui.UiLayer;
import com.ironhold.game.screen.ScreenId;
import com.ironhold.game.screen.ScreenNavigator;

import java.util.Objects;

public final class MenuScreen extends ScreenAdapter {

    private final ScreenNavigator navigator;
    private final UiLayer ui;

    public MenuScreen(ScreenNavigator navigator) {
        this.navigator = Objects.requireNonNull(navigator, "navigator");
        this.ui = new UiLayer();
        initButtons();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(ui.getStage());
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.06f, 0.06f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        ui.act(delta);
        ui.draw();
    }

    @Override
    public void resize(int width, int height) {
        ui.resize(width, height);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        ui.dispose();
    }

    private void initButtons() {
        TextButton startButton = new TextButton("Start", ui.getSkin());
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                navigator.goTo(ScreenId.GAME);
            }
        });

        TextButton exitButton = new TextButton("Exit", ui.getSkin());
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                Gdx.app.exit();
            }
        });

        Table root = new Table();
        root.setFillParent(true);
        root.defaults().width(220f).height(52f).pad(10f);
        Label title = new Label("IronHold", ui.getSkin(), "label");
        root.add(title).padBottom(22f).row();
        root.add(startButton).row();
        root.add(exitButton);
        ui.getStage().addActor(root);
    }
}
