package com.ironhold.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ironhold.game.GameFacade;
import com.ironhold.game.screen.ScreenId;
import com.ironhold.ui.UiLayer;

import java.util.Objects;

/**
 * Main menu screen.
 *
 * <p>Visual layers:
 * <ul>
 *   <li>Teal grid ({@link ShapeRenderer}) behind Stage — ties menu to in-game aesthetic.</li>
 *   <li>Title pop-in (scale+fade via {@link Actions}), then time-driven breathing
 *       with {@link MathUtils#sin} in {@link #render}.</li>
 *   <li>Buttons: gold border, opaque fill, 1.05× hover scale + orange text tint.</li>
 * </ul>
 */
public final class MenuScreen extends ScreenAdapter {

    private static final float BG_R = 0.04f, BG_G = 0.04f, BG_B = 0.13f;

    private static final float BTN_HOVER_SCALE = 1.05f;
    private static final float BTN_ANIM_SEC    = 0.13f;
    /** Time (s) to wait before starting the breathing pulse (lets pop-in finish). */
    private static final float BREATH_DELAY    = 0.65f;

    private final GameFacade game;
    private final UiLayer ui;
    private final ShapeRenderer shapes;

    private Label titleLabel;
    /** Accumulates elapsed time; drives the breathing sine wave. */
    private float titleTime;

    private TextButton playButton;
    private TextButton quitButton;

    public MenuScreen(GameFacade game) {
        this.game   = Objects.requireNonNull(game, "game");
        this.ui     = new UiLayer(game.getAssets().getSkin());
        this.shapes = new ShapeRenderer();
        initLayout();
    }

    @Override
    public void show() {
        titleTime = 0f;
        Gdx.input.setInputProcessor(ui.getStage());
        ui.getStage().act(0f);       // layout pass so button sizes are resolved
        centreOrigin(playButton);
        centreOrigin(quitButton);
    }

    @Override
    public void render(float delta) {
        titleTime += delta;

        Gdx.gl.glClearColor(BG_R, BG_G, BG_B, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        drawGradientBackground();

        // Breathing pulse starts after the pop-in animation has finished.
        if (titleTime > BREATH_DELAY) {
            float t     = (titleTime - BREATH_DELAY) * 2f;   // frequency factor
            float pulse = 1f + 0.025f * MathUtils.sin(t);
            titleLabel.setScale(pulse);
        }

        ui.act(delta);
        ui.draw();
    }

    @Override
    public void resize(int width, int height) {
        ui.resize(width, height);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        ui.dispose();
    }

    // ── Background gradient ────────────────────────────────────────────────

    /**
     * Simulates a soft radial gradient by drawing concentric ellipses with tiny alpha.
     * Two "light sources": upper-right (blue-violet) and lower-left (deep indigo).
     */
    private void drawGradientBackground() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(ui.getStage().getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        final int STEPS = 16;

        // Upper-right glow — blue-violet tint
        for (int i = STEPS; i >= 1; i--) {
            float t  = i / (float) STEPS;
            float rw = w * 0.90f * t;
            float rh = h * 0.80f * t;
            shapes.setColor(0.11f, 0.09f, 0.30f, 0.028f * t);
            shapes.ellipse(w * 0.58f - rw * 0.5f, h * 0.52f - rh * 0.5f, rw, rh);
        }

        // Lower-left glow — darker indigo accent
        for (int i = STEPS; i >= 1; i--) {
            float t  = i / (float) STEPS;
            float rw = w * 0.65f * t;
            float rh = h * 0.60f * t;
            shapes.setColor(0.07f, 0.05f, 0.22f, 0.020f * t);
            shapes.ellipse(-rw * 0.25f, -rh * 0.15f, rw, rh);
        }

        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    // ── UI layout ──────────────────────────────────────────────────────────

    private void initLayout() {
        titleLabel = new Label("IronHold", ui.getSkin(), "title");
        Label subtitle = new Label("Tower Defense", ui.getSkin(), "label-muted");

        // Pop-in: start invisible + scaled down, zoom to 100 %.
        // Breathing is handled by render() after BREATH_DELAY seconds.
        titleLabel.setOrigin(com.badlogic.gdx.utils.Align.center);
        titleLabel.getColor().a = 0f;
        titleLabel.setScale(0.55f);
        titleLabel.addAction(Actions.sequence(
            Actions.delay(0.1f),
            Actions.parallel(
                Actions.fadeIn(0.50f, Interpolation.fade),
                Actions.scaleTo(1f, 1f, 0.50f, Interpolation.swingOut)
            )
        ));

        playButton = buildMenuButton("Play", new Runnable() {
            @Override public void run() { game.getScreens().goTo(ScreenId.LEVEL_SELECT); }
        });
        quitButton = buildMenuButton("Quit", new Runnable() {
            @Override public void run() { Gdx.app.exit(); }
        });

        Table root = new Table();
        root.setFillParent(true);
        root.add(titleLabel).padBottom(4f).row();
        root.add(subtitle).padBottom(52f).row();
        root.add(playButton).width(240f).height(52f).pad(8f).row();
        root.add(quitButton).width(240f).height(52f).pad(8f);
        ui.getStage().addActor(root);
    }

    /**
     * Creates a styled menu button with:
     * <ul>
     *   <li>1.05× scale on hover (via {@link Actions#scaleTo}).</li>
     *   <li>Text colour animates from gold to orange on enter, back on exit.</li>
     * </ul>
     */
    private TextButton buildMenuButton(String text, final Runnable onClick) {
        final TextButton btn = new TextButton(text, ui.getSkin(), "menu-button");
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
            public void enter(InputEvent event, float x, float y, int pointer,
                              com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                btn.clearActions();
                btn.addAction(Actions.scaleTo(BTN_HOVER_SCALE, BTN_HOVER_SCALE,
                    BTN_ANIM_SEC, Interpolation.sineOut));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer,
                             com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                btn.clearActions();
                btn.addAction(Actions.scaleTo(1f, 1f, BTN_ANIM_SEC, Interpolation.sineOut));
            }
        });

        return btn;
    }

    private static void centreOrigin(TextButton btn) {
        btn.setOrigin(btn.getWidth() / 2f, btn.getHeight() / 2f);
    }
}
