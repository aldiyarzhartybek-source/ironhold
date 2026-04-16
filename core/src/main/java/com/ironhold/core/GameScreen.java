package com.ironhold.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.ironhold.assets.AssetService;

import java.util.Objects;

public final class GameScreen extends ScreenAdapter {
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final Texture testTexture;

    public GameScreen(AssetService assets) {
        AssetService assetService = Objects.requireNonNull(assets, "assets");
        this.batch = new SpriteBatch();
        this.font = assetService.getFont();
        this.testTexture = assetService.getTestTexture();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(testTexture, 24f, 24f, 64f, 64f);
        font.draw(batch, "GameScreen placeholder", 24f, Gdx.graphics.getHeight() - 24f);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
