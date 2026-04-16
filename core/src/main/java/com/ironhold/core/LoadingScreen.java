package com.ironhold.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.ironhold.assets.AssetService;
import com.ironhold.game.screen.ScreenId;
import com.ironhold.game.screen.ScreenNavigator;

import java.util.Objects;

public final class LoadingScreen extends ScreenAdapter {

    private final ScreenNavigator navigator;
    private final AssetService assets;

    public LoadingScreen(ScreenNavigator navigator, AssetService assets) {
        this.navigator = Objects.requireNonNull(navigator, "navigator");
        this.assets = Objects.requireNonNull(assets, "assets");
    }

    @Override
    public void show() {
        assets.queueCoreAssets();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (assets.update()) {
            navigator.goTo(ScreenId.MENU);
        }
    }
}
