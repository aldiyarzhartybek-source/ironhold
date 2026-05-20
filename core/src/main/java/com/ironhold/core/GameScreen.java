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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ironhold.core.fx.FxBloomPipeline;
import com.ironhold.game.GameFacade;
import com.ironhold.game.GameMode;
import com.ironhold.game.GameRuntimeView;
import com.ironhold.core.render.EnemyShapeRenderer;
import com.ironhold.core.render.GameplayMapRenderer;
import com.ironhold.core.render.HitEffectRenderer;
import com.ironhold.core.render.LightningRenderer;
import com.ironhold.core.render.FlameConeRenderer;
import com.ironhold.core.render.MortarExplosionRenderer;
import com.ironhold.core.render.ProjectileRenderer;
import com.ironhold.core.render.TowerShapeRenderer;
import com.ironhold.game.screen.ScreenId;
import com.ironhold.level.LevelStatus;
import com.ironhold.ui.GameTheme;
import com.ironhold.ui.UiLayer;

import java.util.Objects;

public final class GameScreen extends ScreenAdapter {


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
    private final EnemyShapeRenderer enemyShapes;
    private final TowerShapeRenderer towerShapes;
    private final ProjectileRenderer projectileRenderer;
    private final HitEffectRenderer hitEffectRenderer;
    private final LightningRenderer lightningRenderer;
    private final MortarExplosionRenderer mortarExplosionRenderer;
    private final FlameConeRenderer flameConeRenderer;
    private final FxBloomPipeline bloomPipeline;
    private final StageHud hud;
    private final GameplayUiFxReactor eventUiFx;
    private final WaveStartControls waveStartControls;
    private final GameSpeedControls gameSpeedControls;
    private final UiLayer endStateUi;
    private final UiLayer pauseUi;
    private final InputProcessor gameWorldInput;
    private boolean endOverlayVisible;
    private LevelStatus endOverlayStatus;
    private boolean isPaused;

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
        this.enemyShapes = new EnemyShapeRenderer(game.getEnemiesById());
        this.towerShapes = new TowerShapeRenderer();
        this.projectileRenderer = new ProjectileRenderer();
        this.hitEffectRenderer = new HitEffectRenderer();
        this.lightningRenderer = new LightningRenderer();
        this.mortarExplosionRenderer = new MortarExplosionRenderer();
        this.flameConeRenderer = new FlameConeRenderer();
        this.bloomPipeline = new FxBloomPipeline(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.hud = new StageHud(font, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.eventUiFx = new GameplayUiFxReactor(game.getEventBus());
        this.waveStartControls = new WaveStartControls(game);
        this.gameSpeedControls = new GameSpeedControls(game);
        this.endStateUi = new UiLayer(assetService.getSkin());
        this.pauseUi    = new UiLayer(assetService.getSkin());
        this.gameWorldInput = createGameWorldInput();
        this.endOverlayVisible = false;
        this.endOverlayStatus  = null;
        this.isPaused          = false;
        initPauseOverlay(assetService.getSkin());
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
        if (!isPaused) {
            game.updateLevel(delta);
        }
        eventUiFx.update(delta);
        GameRuntimeView view = game.getRuntimeView();
        if (!endOverlayVisible) {
            waveStartControls.sync(view, false);
            gameSpeedControls.sync(false);
        }
        syncEndStateOverlay(view);

        GameTheme.clearBackground();
        camera.update();

        // ══════════════════════════════════════════════════════════════════
        // Everything (map + entities) goes inside the bloom capture so that
        // vfxManager.renderToScreen() outputs the full scene.
        // gdx-vfx renders to screen with blending disabled (full replace),
        // so the path drawn BEFORE capture would be lost — capture everything.
        // ══════════════════════════════════════════════════════════════════
        bloomPipeline.beginCapture();

        // Tiled map base (covered by backdrop, but kept for correctness)
        mapRenderer.setView(camera);
        mapRenderer.render();

        // Ground backdrop + path + slots + markers
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawVisualBackdrop();
        // mapVisuals internally: batch.end → Filled shapes → Line shapes → batch.begin
        mapVisuals.render(batch, camera.combined, view);
        // batch is now in begin() state after mapVisuals

        // Enemies (internally: batch.end → shapes → batch.begin)
        enemyShapes.render(batch, camera.combined, view.getActiveEnemies(), view.getEnemyPath());
        // Towers (internally: batch.end → shapes → batch.begin)
        towerShapes.render(batch, camera.combined, view.getPlacedTowers());
        // Projectiles + hit effects
        drawFxLayer(view);
        batch.end();

        bloomPipeline.endCaptureAndRender();  // bloom applied, full scene output to screen

        // ── UI — drawn after bloom so it stays crisp ───────────────────────
        batch.begin();
        hud.render(batch, view, debugMode);
        drawEventOverlays();
        batch.end();

        if (endOverlayVisible) {
            endStateUi.act(delta);
            endStateUi.draw();
        } else if (isPaused) {
            pauseUi.act(delta);
            pauseUi.draw();
        } else {
            waveStartControls.act(delta);
            gameSpeedControls.act(delta);
            waveStartControls.draw();
            gameSpeedControls.draw();
        }
    }


    // ══════════════════════════════════════════════════════════════════════
    // Input
    // ══════════════════════════════════════════════════════════════════════

    private InputProcessor createGameWorldInput() {
        return new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (endOverlayVisible || button != Input.Buttons.LEFT) return false;
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
                if (keycode == Input.Keys.ESCAPE) {
                    showPauseOverlay();
                    return true;
                }
                if (endOverlayVisible) return false;
                if (keycode == Input.Keys.SPACE && game.getGameMode() != GameMode.RUSH) {
                    waveStartControls.tryStartNextWave();
                    return true;
                }
                if (keycode == Input.Keys.T) {
                    gameSpeedControls.toggleSpeed();
                    return true;
                }
                if (keycode >= Input.Keys.NUM_1 && keycode <= Input.Keys.NUM_9) {
                    game.selectTowerByIndex(keycode - Input.Keys.NUM_1);
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

    // ── Pause overlay ──────────────────────────────────────────────────────

    private void initPauseOverlay(com.badlogic.gdx.scenes.scene2d.ui.Skin skin) {
        Table root = new Table();
        root.setFillParent(true);

        Table panel = new Table();
        panel.defaults().pad(8f);

        Label title = new Label("Paused", skin, "title");
        panel.add(title).padBottom(24f).row();
        panel.add(pauseButton("Resume", new Runnable() {
            @Override public void run() { hidePauseOverlay(); }
        })).width(220f).height(52f).row();
        panel.add(pauseButton("Exit to Menu", new Runnable() {
            @Override public void run() {
                isPaused = false;
                game.getScreens().goTo(ScreenId.MENU);
            }
        })).width(220f).height(52f);

        root.add(panel).pad(48f);
        pauseUi.getStage().addActor(root);

        // Escape resumes the game from within the pause overlay.
        pauseUi.getStage().addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    hidePauseOverlay();
                    return true;
                }
                return false;
            }
        });
    }

    /** Creates a menu-style button used inside the pause overlay. */
    private TextButton pauseButton(String text, final Runnable onClick) {
        final TextButton btn = new TextButton(text, pauseUi.getSkin(), "menu-button");
        btn.setTransform(true);
        btn.getLabel().setColor(Color.WHITE);
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                onClick.run();
            }
        });
        btn.addListener(new InputListener() {
            @Override
            public void enter(InputEvent e, float x, float y, int pointer,
                              com.badlogic.gdx.scenes.scene2d.Actor from) {
                btn.clearActions();
                btn.addAction(Actions.scaleTo(1.05f, 1.05f, 0.13f, Interpolation.sineOut));
            }
            @Override
            public void exit(InputEvent e, float x, float y, int pointer,
                             com.badlogic.gdx.scenes.scene2d.Actor to) {
                btn.clearActions();
                btn.addAction(Actions.scaleTo(1f, 1f, 0.13f, Interpolation.sineOut));
            }
        });
        // Set scale origin to centre after a layout pass.
        pauseUi.getStage().act(0f);
        btn.setOrigin(btn.getWidth() / 2f, btn.getHeight() / 2f);
        return btn;
    }

    private void showPauseOverlay() {
        isPaused = true;
        Gdx.input.setInputProcessor(pauseUi.getStage());
    }

    private void hidePauseOverlay() {
        isPaused = false;
        bindGameplayInput();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Lifecycle
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        hud.resize(width, height);
        waveStartControls.resize(width, height);
        gameSpeedControls.resize(width, height);
        endStateUi.resize(width, height);
        pauseUi.resize(width, height);
        bloomPipeline.resize(width, height);
    }

    @Override
    public void dispose() {
        mapRenderer.dispose();
        mapVisuals.dispose();
        enemyShapes.dispose();
        towerShapes.dispose();
        projectileRenderer.dispose();
        hitEffectRenderer.dispose();
        lightningRenderer.dispose();
        mortarExplosionRenderer.dispose();
        flameConeRenderer.dispose();
        bloomPipeline.dispose();
        batch.dispose();
        eventUiFx.dispose();
        waveStartControls.dispose();
        gameSpeedControls.dispose();
        endStateUi.dispose();
        pauseUi.dispose();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Draw helpers
    // ══════════════════════════════════════════════════════════════════════

    private void drawFxLayer(GameRuntimeView view) {
        // Projectiles — oriented energy beams with fading trail
        projectileRenderer.render(batch, camera.combined, view.getActiveProjectiles());
        flameConeRenderer.render(batch, camera.combined, view.getFlameConeEffects());
        // Impact bursts — drawn after projectiles so the sparkles read above the beams
        hitEffectRenderer.render(batch, camera.combined, view.getHitEffects());
        // Chain-lightning bolts (instant flash, very short TTL)
        lightningRenderer.render(batch, camera.combined, view.getLightningEffects());
        mortarExplosionRenderer.render(batch, camera.combined, view.getMortarExplosions());
        drawFloatingRewardTexts();
    }

    private void drawVisualBackdrop() {
        float width  = camera.viewportWidth;
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
            float topY  = camera.viewportHeight - 90f;
            batch.setColor(GameTheme.multiplyAlpha(GameTheme.BANNER_BACKGROUND, banner.getAlpha()));
            batch.draw(testTexture, width * 0.5f - 150f, topY - 26f, 300f, 34f);
            font.setColor(GameTheme.multiplyAlpha(GameTheme.BANNER_TEXT, banner.getAlpha()));
            font.draw(batch, banner.getText(), width * 0.5f - 78f, topY - 3f);
            font.setColor(GameTheme.UI_TEXT);
        }

        // Toast stack — shifted down so it clears the Gold/Time HUD row
        float toastBaseY  = camera.viewportHeight - 62f;
        float toastSlotH  = 30f;
        for (GameplayUiFxReactor.ToastView toast : eventUiFx.getToastViews()) {
            float y = toastBaseY - toast.getSlotIndex() * toastSlotH;
            if (toast.isError()) {
                batch.setColor(GameTheme.multiplyAlpha(GameTheme.TOAST_ERROR_BACKGROUND, toast.getAlpha()));
                font.setColor(GameTheme.multiplyAlpha(GameTheme.TOAST_ERROR_TEXT, toast.getAlpha()));
            } else {
                batch.setColor(GameTheme.multiplyAlpha(GameTheme.TOAST_SUCCESS_BACKGROUND, toast.getAlpha()));
                font.setColor(GameTheme.multiplyAlpha(GameTheme.TOAST_SUCCESS_TEXT, toast.getAlpha()));
            }
            batch.draw(testTexture, camera.viewportWidth - 340f, y - 22f, 320f, 26f);
            font.draw(batch, toast.getText(), camera.viewportWidth - 332f, y - 4f);
            font.setColor(GameTheme.UI_TEXT);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // End-state overlay
    // ══════════════════════════════════════════════════════════════════════

    private void syncEndStateOverlay(GameRuntimeView view) {
        LevelStatus status = view.getLevelState().getStatus();
        if (status != LevelStatus.COMPLETED && status != LevelStatus.FAILED) return;
        if (endOverlayVisible && endOverlayStatus == status) return;
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
            root.add(new Label("Kills: "      + view.getTotalKilledEnemies(),    endStateUi.getSkin(), "label")).row();
            root.add(new Label("Gold spent: " + view.getTotalGoldSpent(),        endStateUi.getSkin(), "label")).row();
            root.add(new Label("Time: "       + view.getElapsedLevelTimeFormatted(), endStateUi.getSkin(), "label"))
                .padBottom(16f).row();

            TextButton continueButton = new TextButton("Continue", endStateUi.getSkin());
            continueButton.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                    hideEndOverlay();
                    game.getScreens().goTo(ScreenId.LEVEL_SELECT);
                }
            });
            root.defaults().height(52f);
            root.add(continueButton);
        } else {
            TextButton retryButton = new TextButton("Retry", endStateUi.getSkin());
            retryButton.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                    hideEndOverlay();
                    game.startLevel();
                }
            });
            TextButton levelSelectButton = new TextButton("Level Select", endStateUi.getSkin());
            levelSelectButton.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
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
        endOverlayStatus  = null;
        endStateUi.getStage().clear();
        eventUiFx.clearTransientState();
        bindGameplayInput();
    }
}
