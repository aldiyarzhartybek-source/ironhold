package com.ironhold.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.ironhold.game.GameFacade;
import com.ironhold.game.model.ActiveEnemy;
import com.ironhold.game.model.BuildSlot;
import com.ironhold.game.model.PlacedTower;
import com.ironhold.level.RuntimeLevelState;

import java.util.Objects;

public final class GameScreen extends ScreenAdapter {
    private final GameFacade game;
    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final Texture testTexture;
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final Vector3 touchWorld;
    private final StageHud hud;

    public GameScreen(GameFacade game) {
        this.game = Objects.requireNonNull(game, "game");
        var assetService = game.getAssets();
        this.camera = new OrthographicCamera();
        this.batch = new SpriteBatch();
        this.font = assetService.getFont();
        this.testTexture = assetService.getTestTexture();
        this.map = assetService.getLevel0Map();
        this.mapRenderer = new OrthogonalTiledMapRenderer(map, 1f, batch);
        this.touchWorld = new Vector3();
        this.hud = new StageHud(font, game, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void show() {
        game.startLevel();
    }

    @Override
    public void render(float delta) {
        handleBuildPlacementInput();
        handleDebugEnemyKillInput();
        game.updateLevel(delta);
        RuntimeLevelState level = game.getRuntimeLevelState();

        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.begin();
        batch.draw(testTexture, 24f, 24f, 64f, 64f);
        for (BuildSlot slot : game.getBuildSlots()) {
            float slotSize = slot.isOccupied() ? 24f : 16f;
            batch.draw(testTexture, slot.getX() - slotSize / 2f, slot.getY() - slotSize / 2f, slotSize, slotSize);
        }
        for (PlacedTower tower : game.getPlacedTowers()) {
            batch.draw(testTexture, tower.getX() - 12f, tower.getY() - 12f, 24f, 24f);
        }
        for (ActiveEnemy enemy : game.getActiveEnemies()) {
            batch.draw(testTexture, enemy.getX(), enemy.getY(), 20f, 20f);
        }
        hud.render(batch, level);
        batch.end();
    }

    private void handleBuildPlacementInput() {
        if (!Gdx.input.justTouched()) {
            return;
        }
        touchWorld.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        camera.unproject(touchWorld);
        game.tryPlaceTowerAt(touchWorld.x, touchWorld.y);
    }

    private void handleDebugEnemyKillInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            game.debugDefeatFirstEnemy();
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        hud.resize(width, height);
    }

    @Override
    public void dispose() {
        mapRenderer.dispose();
        batch.dispose();
    }
}
