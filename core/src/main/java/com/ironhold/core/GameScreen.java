package com.ironhold.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
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
    private static final float ROAD_WIDTH = 42f;
    private static final float ROAD_MARKER_STEP = 24f;

    private enum RenderLayer {
        GROUND,
        PROPS,
        ENEMIES,
        TOWERS,
        FX,
        UI
    }

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
        renderWorldLayers(view);
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

    private void renderWorldLayers(GameRuntimeView view) {
        // Fixed layer list keeps depth order deterministic.
        for (RenderLayer layer : RenderLayer.values()) {
            renderLayer(layer, view);
        }
        batch.setColor(Color.WHITE);
    }

    private void renderLayer(RenderLayer layer, GameRuntimeView view) {
        switch (layer) {
            case GROUND:
                drawVisualBackdrop();
                break;
            case PROPS:
                drawPathOverlay(view);
                drawBaseAndSpawnMarkers(view);
                drawBuildSlots(view);
                break;
            case ENEMIES:
                drawEnemies(view);
                break;
            case TOWERS:
                drawTowers(view);
                break;
            case FX:
                drawFxLayer(view);
                break;
            case UI:
                hud.render(batch, view);
                break;
            default:
                break;
        }
    }

    private void drawBuildSlots(GameRuntimeView view) {
        for (BuildSlot slot : view.getBuildSlots()) {
            float slotSize = slot.isOccupied() ? 28f : 20f;
            batch.setColor(slot.isOccupied() ? 0.32f : 0.24f, slot.isOccupied() ? 0.7f : 0.5f, 0.3f, 0.95f);
            batch.draw(testTexture, slot.getX() - slotSize / 2f, slot.getY() - slotSize / 2f, slotSize, slotSize);
            batch.setColor(0.06f, 0.09f, 0.12f, 0.95f);
            batch.draw(testTexture, slot.getX() - 7f, slot.getY() - 7f, 14f, 14f);
        }
    }

    private void drawEnemies(GameRuntimeView view) {
        for (ActiveEnemy enemy : view.getActiveEnemies()) {
            batch.setColor(0.88f, 0.3f, 0.3f, 1f);
            batch.draw(testTexture, enemy.getX(), enemy.getY(), 20f, 20f);
        }
    }

    private void drawTowers(GameRuntimeView view) {
        for (PlacedTower tower : view.getPlacedTowers()) {
            batch.setColor(0.42f, 0.53f, 0.95f, 1f);
            batch.draw(testTexture, tower.getX() - 12f, tower.getY() - 12f, 24f, 24f);
        }
    }

    private void drawFxLayer(GameRuntimeView view) {
        if (view.getActiveEnemies().isEmpty()) {
            return;
        }
        // Minimal reserved layer marker to keep FX insertion point explicit.
        ActiveEnemy first = view.getActiveEnemies().get(0);
        batch.setColor(0.95f, 0.95f, 0.95f, 0.12f);
        batch.draw(testTexture, first.getX() - 2f, first.getY() - 2f, 24f, 24f);
    }

    private void drawVisualBackdrop() {
        float width = camera.viewportWidth;
        float height = camera.viewportHeight;

        batch.setColor(0.07f, 0.11f, 0.12f, 1f);
        batch.draw(testTexture, 0f, 0f, width, height);
        batch.setColor(0.11f, 0.14f, 0.1f, 1f);
        batch.draw(testTexture, 0f, 0f, width, height * 0.22f);
        batch.setColor(0.09f, 0.13f, 0.09f, 0.35f);
        batch.draw(testTexture, 16f, 16f, width - 32f, height - 32f);
    }

    private void drawPathOverlay(GameRuntimeView view) {
        var path = view.getEnemyPath();
        if (path.size() < 2) {
            return;
        }
        for (int i = 0; i < path.size() - 1; i++) {
            Vector2 from = path.get(i);
            Vector2 to = path.get(i + 1);
            drawPathSegment(from, to);
        }
    }

    private void drawPathSegment(Vector2 from, Vector2 to) {
        float dx = to.x - from.x;
        float dy = to.y - from.y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length < 0.01f) {
            return;
        }

        // thick route body
        batch.setColor(0.35f, 0.29f, 0.18f, 0.96f);
        if (Math.abs(dx) >= Math.abs(dy)) {
            float y = Math.min(from.y, to.y) - ROAD_WIDTH / 2f;
            batch.draw(testTexture, Math.min(from.x, to.x), y, Math.abs(dx), ROAD_WIDTH);
        } else {
            float x = Math.min(from.x, to.x) - ROAD_WIDTH / 2f;
            batch.draw(testTexture, x, Math.min(from.y, to.y), ROAD_WIDTH, Math.abs(dy));
        }

        // lane highlights
        batch.setColor(0.56f, 0.47f, 0.28f, 0.4f);
        int markerCount = Math.max(1, (int) (length / ROAD_MARKER_STEP));
        for (int m = 0; m <= markerCount; m++) {
            float t = (float) m / markerCount;
            float x = from.x + dx * t;
            float y = from.y + dy * t;
            batch.draw(testTexture, x - 3f, y - 3f, 6f, 6f);
        }
    }

    private void drawBaseAndSpawnMarkers(GameRuntimeView view) {
        var path = view.getEnemyPath();
        if (path.isEmpty()) {
            return;
        }
        Vector2 spawn = path.get(0);
        Vector2 base = path.get(path.size() - 1);

        batch.setColor(0.96f, 0.82f, 0.34f, 0.95f);
        batch.draw(testTexture, spawn.x - 12f, spawn.y - 12f, 24f, 24f);
        batch.setColor(0.72f, 0.18f, 0.18f, 0.95f);
        batch.draw(testTexture, base.x - 14f, base.y - 14f, 28f, 28f);
    }
}
