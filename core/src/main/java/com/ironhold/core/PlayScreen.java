package com.ironhold.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.ironhold.game.screen.ScreenNavigator;
import com.ironhold.ui.GameTheme;

import java.util.Objects;

public class PlayScreen extends ScreenAdapter {

    private final ScreenNavigator navigator;

    public PlayScreen(ScreenNavigator navigator) {
        this.navigator = Objects.requireNonNull(navigator, "navigator");
    }

    @Override
    public void render(float delta) {
        GameTheme.clearBackground();
    }
}
