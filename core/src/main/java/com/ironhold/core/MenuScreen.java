package com.ironhold.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Input;
import com.ironhold.game.screen.ScreenId;
import com.ironhold.game.screen.ScreenNavigator;

import java.util.Objects;

public final class MenuScreen extends ScreenAdapter {

    private final ScreenNavigator navigator;

    public MenuScreen(ScreenNavigator navigator) {
        this.navigator = Objects.requireNonNull(navigator, "navigator");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.06f, 0.06f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.justTouched()) {
            navigator.goTo(ScreenId.GAME);
        }
    }
}
