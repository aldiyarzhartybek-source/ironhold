package com.ironhold.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.ironhold.game.GameFacade;
import com.ironhold.game.screen.ScreenId;

import java.util.Objects;

public final class LoadingScreen extends ScreenAdapter {

    private final GameFacade game;

    public LoadingScreen(GameFacade game) {
        this.game = Objects.requireNonNull(game, "game");
    }

    @Override
    public void show() {
        game.getAssets().queueCoreAssets();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (game.getAssets().update()) {
            game.getScreens().goTo(ScreenId.MENU);
        }
    }
}
