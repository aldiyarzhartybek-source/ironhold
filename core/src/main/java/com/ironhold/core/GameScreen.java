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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ironhold.game.GameFacade;
import com.ironhold.game.GameRuntimeView;
import com.ironhold.game.model.ActiveEnemy;
import com.ironhold.game.model.BuildSlot;
import com.ironhold.game.model.PlacedTower;
import com.ironhold.game.screen.ScreenId;
import com.ironhold.level.LevelStatus;
import com.ironhold.ui.UiLayer;

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
    private final UiLayer endStateUi;
    private boolean endOverlayVisible;
    private LevelStatus endOverlayStatus;

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
        this.hud = new StageHud(font, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.endStateUi = new UiLayer(assetService.getSkin());
        this.endOverlayVisible = false;
        this.endOverlayStatus = null;
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void show() {
        hideEndOverlay();
        game.startLevel();
    }

    @Override
    public void render(float delta) {
        if (!endOverlayVisible) {
            handleBuildPlacementInput();
            handleDebugEnemyKillInput();
        }
        game.updateLevel(delta);
        GameRuntimeView view = game.getRuntimeView();
        syncEndStateOverlay(view);

        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.begin();
        batch.draw(testTexture, 24f, 24f, 64f, 64f);
        for (BuildSlot slot : view.getBuildSlots()) {
            float slotSize = slot.isOccupied() ? 24f : 16f;
            batch.draw(testTexture, slot.getX() - slotSize / 2f, slot.getY() - slotSize / 2f, slotSize, slotSize);
        }
        for (PlacedTower tower : view.getPlacedTowers()) {
            batch.draw(testTexture, tower.getX() - 12f, tower.getY() - 12f, 24f, 24f);
        }
        for (ActiveEnemy enemy : view.getActiveEnemies()) {
            batch.draw(testTexture, enemy.getX(), enemy.getY(), 20f, 20f);
        }
        hud.render(batch, view);
        batch.end();

        if (endOverlayVisible) {
            endStateUi.act(delta);
            endStateUi.draw();
        }
    }

    private void handleBuildPlacementInput() {
        if (!Gdx.input.justTouched()) {
            return;
        }
        touchWorld.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        camera.unproject(touchWorld);
        game.handlePrimaryAction(touchWorld.x, touchWorld.y);
    }

    private void handleDebugEnemyKillInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            game.handleDebugKillAction();
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        hud.resize(width, height);
        endStateUi.resize(width, height);
    }

    @Override
    public void dispose() {
        mapRenderer.dispose();
        batch.dispose();
        endStateUi.dispose();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    private void syncEndStateOverlay(GameRuntimeView view) {
        LevelStatus status = view.getLevelState().getStatus();
        if (status != LevelStatus.COMPLETED && status != LevelStatus.FAILED) {
            return;
        }
        if (endOverlayVisible && endOverlayStatus == status) {
            return;
        }
        endOverlayStatus = status;
        showEndOverlay(status);
    }

    private void showEndOverlay(LevelStatus status) {
        endStateUi.getStage().clear();
        endOverlayVisible = true;

        String titleText = status == LevelStatus.COMPLETED ? "Victory!" : "Defeat";
        String subtitleText = status == LevelStatus.COMPLETED
            ? "All waves are cleared."
            : "Base lives are depleted.";

        Label title = new Label(titleText, endStateUi.getSkin(), "label");
        Label subtitle = new Label(subtitleText, endStateUi.getSkin(), "label");

        TextButton restartButton = new TextButton("Restart", endStateUi.getSkin());
        restartButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                hideEndOverlay();
                game.startLevel();
            }
        });

        TextButton backButton = new TextButton("Back to menu", endStateUi.getSkin());
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                hideEndOverlay();
                game.getScreens().goTo(ScreenId.MENU);
            }
        });

        Table root = new Table();
        root.setFillParent(true);
        root.defaults().width(260f).height(52f).pad(8f);
        root.add(title).padBottom(4f).row();
        root.add(subtitle).padBottom(16f).row();
        root.add(restartButton).row();
        root.add(backButton);
        endStateUi.getStage().addActor(root);
        Gdx.input.setInputProcessor(endStateUi.getStage());
    }

    private void hideEndOverlay() {
        endOverlayVisible = false;
        endOverlayStatus = null;
        endStateUi.getStage().clear();
        Gdx.input.setInputProcessor(null);
    }
}
