package com.ironhold.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ironhold.ui.UiLayer;
import com.ironhold.game.GameFacade;
import com.ironhold.game.screen.ScreenId;

import java.util.Objects;

public final class MenuScreen extends ScreenAdapter {

    private final GameFacade game;
    private final UiLayer ui;

    public MenuScreen(GameFacade game) {
        this.game = Objects.requireNonNull(game, "game");
        this.ui = new UiLayer(game.getAssets().getSkin());
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
                game.getScreens().goTo(ScreenId.GAME);
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
