package com.ironhold.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.ironhold.game.screen.ScreenNavigator;

import java.util.Objects;

public class PlayScreen extends ScreenAdapter {

    private final ScreenNavigator navigator;

    public PlayScreen(ScreenNavigator navigator) {
        this.navigator = Objects.requireNonNull(navigator, "navigator");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
}
