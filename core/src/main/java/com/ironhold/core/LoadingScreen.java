package com.ironhold.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.ironhold.game.screen.ScreenId;
import com.ironhold.game.screen.ScreenNavigator;

import java.util.Objects;

public final class LoadingScreen extends ScreenAdapter {

    private static final float LOADING_SECONDS = 0.6f;

    private final ScreenNavigator navigator;
    private float elapsed;

    public LoadingScreen(ScreenNavigator navigator) {
        this.navigator = Objects.requireNonNull(navigator, "navigator");
    }

    @Override
    public void render(float delta) {
        elapsed += delta;

        Gdx.gl.glClearColor(0.02f, 0.02f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (elapsed >= LOADING_SECONDS) {
            navigator.goTo(ScreenId.MENU);
        }
    }
}
