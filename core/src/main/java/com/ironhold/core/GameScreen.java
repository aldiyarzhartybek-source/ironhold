package com.ironhold.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ironhold.game.GameFacade;
import com.ironhold.game.GameMode;
import com.ironhold.game.GameRuntimeView;
import com.ironhold.game.model.ActiveEnemy;
import com.ironhold.game.model.ActiveProjectile;
import com.ironhold.game.model.HitEffect;
import com.ironhold.core.render.GameplayMapRenderer;
import com.ironhold.game.model.PlacedTower;
import com.ironhold.game.screen.ScreenId;
import com.ironhold.level.LevelStatus;
import com.ironhold.ui.GameTheme;
import com.ironhold.ui.UiLayer;

import java.util.Objects;

public final class GameScreen extends ScreenAdapter {

    private enum RenderLayer {
        GROUND,
        PROPS,
        ENEMIES,
        TOWERS,
        FX,
        UI
    }

    private final GameFacade game;
    private final boolean debugMode;
    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final Texture testTexture;
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final Vector3 touchWorld;
    private final GameplayMapRenderer mapVisuals;
    private final StageHud hud;
    private final GameplayUiFxReactor eventUiFx;
    private final WaveStartControls waveStartControls;
    private final GameSpeedControls gameSpeedControls;
    private final UiLayer endStateUi;
    private final InputProcessor gameWorldInput;
    private boolean endOverlayVisible;
    private LevelStatus endOverlayStatus;

    public GameScreen(GameFacade game) {
        this.game = Objects.requireNonNull(game, "game");
        this.debugMode = game.isDebugMode();
        var assetService = game.getAssets();
        this.camera = new OrthographicCamera();
        this.batch = new SpriteBatch();
        this.font = assetService.getFont();
        this.testTexture = assetService.getTestTexture();
        this.map = assetService.getLevel0Map();
        this.mapRenderer = new OrthogonalTiledMapRenderer(map, 1f, batch);
        this.touchWorld = new Vector3();
        this.mapVisuals = new GameplayMapRenderer();
        this.hud = new StageHud(font, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.eventUiFx = new GameplayUiFxReactor(game.getEventBus());
        this.waveStartControls = new WaveStartControls(game);
        this.gameSpeedControls = new GameSpeedControls(game);
        this.endStateUi = new UiLayer(assetService.getSkin());
        this.gameWorldInput = createGameWorldInput();
        this.endOverlayVisible = false;
        this.endOverlayStatus = null;
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void show() {
        hideEndOverlay();
        game.startLevel();
        bindGameplayInput();
    }

    @Override
    public void render(float delta) {
        game.updateLevel(delta);
        eventUiFx.update(delta);
        GameRuntimeView view = game.getRuntimeView();
        if (!endOverlayVisible) {
            waveStartControls.sync(view, false);
            gameSpeedControls.sync(false);
        }
        syncEndStateOverlay(view);

        GameTheme.clearBackground();

        camera.update();
        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.begin();
        renderWorldLayers(view);
        batch.end();

        if (endOverlayVisible) {
            endStateUi.act(delta);
            endStateUi.draw();
        } else {
            waveStartControls.act(delta);
            gameSpeedControls.act(delta);
            waveStartControls.draw();
            gameSpeedControls.draw();
        }
    }

    private InputProcessor createGameWorldInput() {
        return new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (endOverlayVisible || button != Input.Buttons.LEFT) {
                    return false;
                }
                if (waveStartControls.getUi().getStage().hit(screenX, screenY, true) != null
                    || gameSpeedControls.getUi().getStage().hit(screenX, screenY, true) != null) {
                    return false;
                }
                touchWorld.set(screenX, screenY, 0f);
                camera.unproject(touchWorld);
                game.handlePrimaryAction(touchWorld.x, touchWorld.y);
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (endOverlayVisible) {
                    return false;
                }
                if (keycode == Input.Keys.SPACE && game.getGameMode() != GameMode.RUSH) {
                    waveStartControls.tryStartNextWave();
                    return true;
                }
                if (keycode == Input.Keys.T) {
                    gameSpeedControls.toggleSpeed();
                    return true;
                }
                if (debugMode && keycode == Input.Keys.K) {
                    game.handleDebugKillAction();
                    return true;
                }
                return false;
            }
        };
    }

    private void bindGameplayInput() {
        Gdx.input.setInputProcessor(new InputMultiplexer(
            waveStartControls.getUi().getStage(),
            gameSpeedControls.getUi().getStage(),
            gameWorldInput
        ));
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        hud.resize(width, height);
        waveStartControls.resize(width, height);
        gameSpeedControls.resize(width, height);
        endStateUi.resize(width, height);
    }

    @Override
    public void dispose() {
        mapRenderer.dispose();
        mapVisuals.dispose();
        batch.dispose();
        eventUiFx.dispose();
        waveStartControls.dispose();
        gameSpeedControls.dispose();
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
        GameRuntimeView view = game.getRuntimeView();
        boolean victory = status == LevelStatus.COMPLETED;

        Label title = new Label(victory ? "Victory!" : "Defeat", endStateUi.getSkin(), "label");

        Table root = new Table();
        root.setFillParent(true);
        root.defaults().width(280f).pad(8f);
        root.add(title).padBottom(victory ? 12f : 20f).row();

        if (victory) {
            root.defaults().height(28f);
            root.add(new Label("Kills: " + view.getTotalKilledEnemies(), endStateUi.getSkin(), "label")).row();
            root.add(new Label("Gold spent: " + view.getTotalGoldSpent(), endStateUi.getSkin(), "label")).row();
            root.add(new Label("Time: " + view.getElapsedLevelTimeFormatted(), endStateUi.getSkin(), "label"))
                .padBottom(16f)
                .row();

            TextButton continueButton = new TextButton("Continue", endStateUi.getSkin());
            continueButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                    hideEndOverlay();
                    game.getScreens().goTo(ScreenId.LEVEL_SELECT);
                }
            });
            root.defaults().height(52f);
            root.add(continueButton);
        } else {
            TextButton retryButton = new TextButton("Retry", endStateUi.getSkin());
            retryButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                    hideEndOverlay();
                    game.startLevel();
                }
            });

            TextButton levelSelectButton = new TextButton("Level Select", endStateUi.getSkin());
            levelSelectButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                    hideEndOverlay();
                    game.getScreens().goTo(ScreenId.LEVEL_SELECT);
                }
            });

            root.defaults().height(52f);
            root.add(retryButton).row();
            root.add(levelSelectButton);
        }

        endStateUi.getStage().addActor(root);
        Gdx.input.setInputProcessor(endStateUi.getStage());
    }

    private void hideEndOverlay() {
        endOverlayVisible = false;
        endOverlayStatus = null;
        endStateUi.getStage().clear();
        eventUiFx.clearTransientState();
        bindGameplayInput();
    }

    private void renderWorldLayers(GameRuntimeView view) {
        // Fixed layer list keeps depth order deterministic.
        for (RenderLayer layer : RenderLayer.values()) {
            renderLayer(layer, view);
        }
        batch.setColor(GameTheme.TINT_WHITE);
    }

    private void renderLayer(RenderLayer layer, GameRuntimeView view) {
        switch (layer) {
            case GROUND:
                drawVisualBackdrop();
                break;
            case PROPS:
                mapVisuals.render(batch, camera.combined, view);
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
                hud.render(batch, view, debugMode);
                drawEventOverlays();
                break;
            default:
                break;
        }
    }

    private void drawEnemies(GameRuntimeView view) {
        int pathSegments = Math.max(1, view.getEnemyPath().size() - 1);
        for (ActiveEnemy enemy : view.getActiveEnemies()) {
            batch.setColor(GameTheme.ENEMY_ACCENT);
            batch.draw(testTexture, enemy.getX(), enemy.getY(), GameTheme.Draw.ENEMY_SIZE, GameTheme.Draw.ENEMY_SIZE);
            drawEnemyHpBar(enemy);
            drawEnemyProgressBar(enemy, pathSegments);
        }
    }

    private void drawEnemyHpBar(ActiveEnemy enemy) {
        float barX = enemy.getX();
        float barY = enemy.getY() + GameTheme.Draw.ENEMY_SIZE + 3f;
        float hpRatio = enemy.getMaxHp() <= 0
            ? 0f
            : Math.max(0f, Math.min(1f, enemy.getCurrentHp() / (float) enemy.getMaxHp()));

        batch.setColor(GameTheme.HP_BAR_BACKGROUND);
        batch.draw(testTexture, barX, barY, GameTheme.Draw.ENEMY_HP_BAR_WIDTH, GameTheme.Draw.ENEMY_HP_BAR_HEIGHT);
        batch.setColor(GameTheme.HP_BAR_FILL);
        batch.draw(testTexture, barX, barY, GameTheme.Draw.ENEMY_HP_BAR_WIDTH * hpRatio, GameTheme.Draw.ENEMY_HP_BAR_HEIGHT);
    }

    private void drawEnemyProgressBar(ActiveEnemy enemy, int pathSegments) {
        float barX = enemy.getX() + 2f;
        float barY = enemy.getY() + GameTheme.Draw.ENEMY_SIZE + 8f;
        float progressRatio = Math.max(0f, Math.min(1f, enemy.getTargetWaypointIndex() / (float) pathSegments));

        batch.setColor(GameTheme.PROGRESS_BAR_BACKGROUND);
        batch.draw(testTexture, barX, barY, GameTheme.Draw.ENEMY_PROGRESS_BAR_WIDTH, GameTheme.Draw.ENEMY_PROGRESS_BAR_HEIGHT);
        batch.setColor(GameTheme.PROGRESS_BAR_FILL);
        batch.draw(testTexture, barX, barY, GameTheme.Draw.ENEMY_PROGRESS_BAR_WIDTH * progressRatio, GameTheme.Draw.ENEMY_PROGRESS_BAR_HEIGHT);
    }

    private void drawTowers(GameRuntimeView view) {
        for (PlacedTower tower : view.getPlacedTowers()) {
            batch.setColor(GameTheme.TOWER_BLUE);
            float size = GameTheme.Draw.TOWER_SIZE;
            batch.draw(testTexture, tower.getX() - size / 2f, tower.getY() - size / 2f, size, size);
        }
    }

    private void drawFxLayer(GameRuntimeView view) {
        for (ActiveProjectile projectile : view.getActiveProjectiles()) {
            batch.setColor(GameTheme.PROJECTILE);
            batch.draw(testTexture, projectile.getX() - 3f, projectile.getY() - 3f, 6f, 6f);
        }
        for (HitEffect hitEffect : view.getHitEffects()) {
            float alpha = Math.min(1f, Math.max(0f, hitEffect.getTtlSec() / 0.14f));
            batch.setColor(GameTheme.multiplyAlpha(GameTheme.HIT_EFFECT, alpha));
            batch.draw(testTexture, hitEffect.getX() - 10f, hitEffect.getY() - 10f, 20f, 20f);
        }
        drawFloatingRewardTexts();
    }

    private void drawVisualBackdrop() {
        float width = camera.viewportWidth;
        float height = camera.viewportHeight;

        batch.setColor(GameTheme.BACKDROP_BASE);
        batch.draw(testTexture, 0f, 0f, width, height);
        batch.setColor(GameTheme.BACKDROP_TOP_GLOW);
        batch.draw(testTexture, 0f, 0f, width, height * 0.22f);
        batch.setColor(GameTheme.BACKDROP_FRAME);
        batch.draw(testTexture, 16f, 16f, width - 32f, height - 32f);
    }

    private void drawFloatingRewardTexts() {
        for (GameplayUiFxReactor.FloatingTextView floating : eventUiFx.getFloatingTextViews()) {
            batch.setColor(GameTheme.multiplyAlpha(GameTheme.REWARD_FLOAT, floating.getAlpha()));
            font.setColor(GameTheme.multiplyAlpha(GameTheme.REWARD_FLOAT, floating.getAlpha()));
            font.draw(batch, floating.getText(), floating.getX(), floating.getY());
            font.setColor(GameTheme.UI_TEXT);
        }
    }

    private void drawEventOverlays() {
        GameplayUiFxReactor.BannerView banner = eventUiFx.getBannerView();
        if (banner != null) {
            float width = camera.viewportWidth;
            float topY = camera.viewportHeight - 90f;
            batch.setColor(GameTheme.multiplyAlpha(GameTheme.BANNER_BACKGROUND, banner.getAlpha()));
            batch.draw(testTexture, width * 0.5f - 150f, topY - 26f, 300f, 34f);
            font.setColor(GameTheme.multiplyAlpha(GameTheme.BANNER_TEXT, banner.getAlpha()));
            font.draw(batch, banner.getText(), width * 0.5f - 78f, topY - 3f);
            font.setColor(GameTheme.UI_TEXT);
        }

        GameplayUiFxReactor.ToastView toast = eventUiFx.getToastView();
        if (toast != null) {
            float width = camera.viewportWidth;
            float y = camera.viewportHeight - 18f;
            if (toast.isError()) {
                batch.setColor(GameTheme.multiplyAlpha(GameTheme.TOAST_ERROR_BACKGROUND, toast.getAlpha()));
                font.setColor(GameTheme.multiplyAlpha(GameTheme.TOAST_ERROR_TEXT, toast.getAlpha()));
            } else {
                batch.setColor(GameTheme.multiplyAlpha(GameTheme.TOAST_SUCCESS_BACKGROUND, toast.getAlpha()));
                font.setColor(GameTheme.multiplyAlpha(GameTheme.TOAST_SUCCESS_TEXT, toast.getAlpha()));
            }
            batch.draw(testTexture, width - 340f, y - 22f, 320f, 26f);
            font.draw(batch, toast.getText(), width - 332f, y - 4f);
            font.setColor(GameTheme.UI_TEXT);
        }
    }
}
