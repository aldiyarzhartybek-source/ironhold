package com.ironhold.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ironhold.game.GameFacade;
import com.ironhold.game.GameMode;
import com.ironhold.game.screen.ScreenId;
import com.ironhold.save.ProgressService;
import com.ironhold.ui.GameTheme;
import com.ironhold.ui.UiLayer;

import java.util.Objects;

/**
 * Level and game mode selection before starting a run.
 */
public final class LevelSelectScreen extends ScreenAdapter {

    private static final float BG_R = 0.04f, BG_G = 0.04f, BG_B = 0.13f;

    private static final float LEVEL_CARD_WIDTH = 132f;
    private static final float LEVEL_CARD_HEIGHT = 88f;
    private static final float ACTION_BUTTON_WIDTH = 260f;
    private static final float ACTION_BUTTON_HEIGHT = 52f;
    private static final String[] MODE_LABELS = {"Classic", "One Life", "Rush"};

    private final GameFacade game;
    private final UiLayer ui;
    private final ShapeRenderer shapes;
    private final TextButton[] levelButtons = new TextButton[ProgressService.MAX_LEVELS];
    private final Label[] levelStatusLabels = new Label[ProgressService.MAX_LEVELS];

    private SelectBox<String> modeSelect;
    private TextButton startButton;
    private Label progressLabel;
    private int selectedLevel = 1;

    public LevelSelectScreen(GameFacade game) {
        this.game   = Objects.requireNonNull(game, "game");
        this.ui     = new UiLayer(game.getAssets().getSkin());
        this.shapes = new ShapeRenderer();
        initLayout();
    }

    @Override
    public void show() {
        selectedLevel = firstUnlockedLevel();
        syncModeSelectToFacade();
        refreshLevelCards();
        refreshStartButton();
        refreshProgressLabel();
        Gdx.input.setInputProcessor(ui.getStage());
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(BG_R, BG_G, BG_B, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        drawGradientBackground();
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

    private void initLayout() {
        Label title = new Label("Level Select", ui.getSkin(), "title");
        progressLabel = new Label("", ui.getSkin(), "label-muted");

        Label modeLabel = new Label("Mode", ui.getSkin(), "label");
        modeSelect = new SelectBox<>(ui.getSkin());
        modeSelect.setItems(MODE_LABELS);
        modeSelect.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.setGameMode(modeFromLabel(modeSelect.getSelected()));
            }
        });

        Table levelGrid = new Table();
        levelGrid.defaults().pad(6f);
        for (int level = 1; level <= ProgressService.MAX_LEVELS; level++) {
            levelButtons[level - 1] = createLevelButton(level);
            Label statusLabel = new Label("", ui.getSkin(), "label-muted");
            statusLabel.setAlignment(Align.center);
            levelStatusLabels[level - 1] = statusLabel;
            Table card = new Table();
            card.defaults().pad(2f);
            card.add(levelButtons[level - 1]).size(LEVEL_CARD_WIDTH, LEVEL_CARD_HEIGHT).row();
            card.add(statusLabel).width(LEVEL_CARD_WIDTH).center();
            levelGrid.add(card);
            if (level % 3 == 0) {
                levelGrid.row();
            }
        }

        startButton = new TextButton("Start", ui.getSkin(), "menu-button");
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                tryStartSelectedLevel();
            }
        });

        TextButton backButton = new TextButton("Back", ui.getSkin(), "menu-button");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.getScreens().goTo(ScreenId.MENU);
            }
        });

        Table root = new Table();
        root.setFillParent(true);
        root.add(title).padBottom(2f).row();
        root.add(progressLabel).padBottom(20f).row();

        Table modeRow = new Table();
        modeRow.add(modeLabel).padRight(12f);
        modeRow.add(modeSelect).width(220f).height(44f);
        root.add(modeRow).padBottom(20f).row();

        root.add(levelGrid).padBottom(24f).row();

        Table actions = new Table();
        actions.defaults().width(ACTION_BUTTON_WIDTH).height(ACTION_BUTTON_HEIGHT).pad(6f);
        actions.add(startButton).row();
        actions.add(backButton);
        root.add(actions);

        ui.getStage().addActor(root);

        ui.getStage().addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    game.getScreens().goTo(ScreenId.MENU);
                    return true;
                }
                return false;
            }
        });
    }

    private TextButton createLevelButton(int levelNumber) {
        TextButton button = new TextButton("Level " + levelNumber, ui.getSkin(), "menu-button");
        final int level = levelNumber;
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                if (!game.isLevelUnlocked(level)) {
                    return;
                }
                selectedLevel = level;
                refreshLevelCards();
                refreshStartButton();
            }
        });
        return button;
    }

    private void tryStartSelectedLevel() {
        if (!game.isLevelUnlocked(selectedLevel)) {
            return;
        }
        GameMode mode = modeFromLabel(modeSelect.getSelected());
        game.setCurrentLevelNumber(selectedLevel);
        game.setGameMode(mode);
        game.getScreens().goTo(ScreenId.GAME);
    }

    private void refreshLevelCards() {
        for (int level = 1; level <= ProgressService.MAX_LEVELS; level++) {
            boolean unlocked  = game.isLevelUnlocked(level);
            boolean completed = game.getProgressService().isLevelCompleted(level);
            boolean selected  = level == selectedLevel;

            TextButton button = levelButtons[level - 1];
            button.setDisabled(!unlocked);
            button.setTouchable(unlocked ? Touchable.enabled : Touchable.disabled);

            if (!unlocked) {
                // Locked — genuinely faded out so the player understands it's unavailable.
                button.setColor(1f, 1f, 1f, 0.35f);
            } else if (selected) {
                // Selected — subtle teal tint to highlight choice.
                button.setColor(GameTheme.PATH_TEAL.r, GameTheme.PATH_TEAL.g,
                    GameTheme.PATH_TEAL.b, 1f);
            } else {
                button.setColor(Color.WHITE);
            }

            Label status = levelStatusLabels[level - 1];
            status.setAlignment(Align.center);
            if (!unlocked) {
                status.setText("Locked");
                status.setColor(GameTheme.UI_TEXT_MUTED.r, GameTheme.UI_TEXT_MUTED.g,
                    GameTheme.UI_TEXT_MUTED.b, 0.35f);
            } else if (completed) {
                status.setColor(GameTheme.UI_TEXT_MUTED);
                status.setText("Done  " + game.getLevelStartingGold(level) + "g");
            } else {
                status.setColor(GameTheme.UI_TEXT_MUTED);
                status.setText(game.getLevelStartingGold(level) + "g start");
            }
        }
    }

    private void refreshStartButton() {
        boolean canStart = game.isLevelUnlocked(selectedLevel);
        startButton.setDisabled(!canStart);
        startButton.setTouchable(canStart ? Touchable.enabled : Touchable.disabled);
    }

    private void refreshProgressLabel() {
        int highest = game.getHighestUnlockedLevel();
        progressLabel.setText("Unlocked: 1-" + highest + " of " + ProgressService.MAX_LEVELS);
    }

    private void syncModeSelectToFacade() {
        String label = labelFromMode(game.getGameMode());
        modeSelect.setSelected(label);
    }

    private int firstUnlockedLevel() {
        for (int level = 1; level <= ProgressService.MAX_LEVELS; level++) {
            if (game.isLevelUnlocked(level)) {
                return level;
            }
        }
        return 1;
    }

    private static GameMode modeFromLabel(String label) {
        if ("One Life".equals(label)) {
            return GameMode.ONE_LIFE;
        }
        if ("Rush".equals(label)) {
            return GameMode.RUSH;
        }
        return GameMode.CLASSIC;
    }

    private static String labelFromMode(GameMode mode) {
        if (mode == GameMode.ONE_LIFE) {
            return "One Life";
        }
        if (mode == GameMode.RUSH) {
            return "Rush";
        }
        return "Classic";
    }
}
