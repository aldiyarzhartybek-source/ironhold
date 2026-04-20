package com.ironhold.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.ironhold.game.GameFacade;

import java.util.Objects;

public final class GameScreen extends ScreenAdapter {
    private final GameFacade game;
    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final Texture testTexture;
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;

    public GameScreen(GameFacade game) {
        this.game = Objects.requireNonNull(game, "game");
        var assetService = game.getAssets();
        this.camera = new OrthographicCamera();
        this.batch = new SpriteBatch();
        this.font = assetService.getFont();
        this.testTexture = assetService.getTestTexture();
        this.map = assetService.getLevel0Map();
        this.mapRenderer = new OrthogonalTiledMapRenderer(map, 1f, batch);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.begin();
        batch.draw(testTexture, 24f, 24f, 64f, 64f);
        font.draw(batch, "GameScreen placeholder", 24f, Gdx.graphics.getHeight() - 24f);
        font.draw(batch, "Enemies: " + game.getEnemies().size(), 24f, Gdx.graphics.getHeight() - 52f);
        font.draw(batch, "Towers: " + game.getTowers().size(), 24f, Gdx.graphics.getHeight() - 80f);
        font.draw(batch, "Waves: " + game.getWaves().size(), 24f, Gdx.graphics.getHeight() - 108f);
        font.draw(batch, "BuildSlots: " + game.getBuildSlots().size(), 24f, Gdx.graphics.getHeight() - 136f);
        font.draw(batch, "Gold: " + game.getEconomy().getGold(), 24f, Gdx.graphics.getHeight() - 164f);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void dispose() {
        mapRenderer.dispose();
        batch.dispose();
    }
}
