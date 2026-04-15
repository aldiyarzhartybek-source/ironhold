package com.ironhold.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;

public final class GameScreen extends ScreenAdapter {

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
}
