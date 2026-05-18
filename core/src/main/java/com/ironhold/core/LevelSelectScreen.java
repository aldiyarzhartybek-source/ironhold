package com.ironhold.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
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

    private static final float LEVEL_CARD_WIDTH = 132f;
    private static final float LEVEL_CARD_HEIGHT = 88f;
    private static final float ACTION_BUTTON_WIDTH = 260f;
    private static final float ACTION_BUTTON_HEIGHT = 52f;
    private static final String[] MODE_LABELS = {"Classic", "One Life", "Rush"};

    private final GameFacade game;
    private final UiLayer ui;
    private final TextButton[] levelButtons = new TextButton[ProgressService.MAX_LEVELS];
    private final Label[] levelStatusLabels = new Label[ProgressService.MAX_LEVELS];

    private SelectBox<String> modeSelect;
    private TextButton startButton;
    private Label progressLabel;
    private int selectedLevel = 1;

    public LevelSelectScreen(GameFacade game) {
        this.game = Objects.requireNonNull(game, "game");
        this.ui = new UiLayer(game.getAssets().getSkin());
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
        GameTheme.clearBackground();
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
        ui.dispose();
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
            levelStatusLabels[level - 1] = new Label("", ui.getSkin(), "label-muted");
            Table card = new Table();
            card.defaults().pad(2f);
            card.add(levelButtons[level - 1]).size(LEVEL_CARD_WIDTH, LEVEL_CARD_HEIGHT).row();
            card.add(levelStatusLabels[level - 1]);
            levelGrid.add(card);
            if (level % 3 == 0) {
                levelGrid.row();
            }
        }

        startButton = new TextButton("Start", ui.getSkin());
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                tryStartSelectedLevel();
            }
        });

        TextButton backButton = new TextButton("Back", ui.getSkin());
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.getScreens().goTo(ScreenId.MENU);
            }
        });

        Table root = new Table();
        root.setFillParent(true);
        root.defaults().pad(8f);
        root.add(title).padBottom(4f).row();
        root.add(progressLabel).padBottom(12f).row();

        Table modeRow = new Table();
        modeRow.add(modeLabel).padRight(12f);
        modeRow.add(modeSelect).width(220f).height(44f);
        root.add(modeRow).padBottom(16f).row();

        root.add(levelGrid).padBottom(20f).row();

        Table actions = new Table();
        actions.defaults().width(ACTION_BUTTON_WIDTH).height(ACTION_BUTTON_HEIGHT).pad(6f);
        actions.add(startButton).row();
        actions.add(backButton);
        root.add(actions);

        ui.getStage().addActor(root);
    }

    private TextButton createLevelButton(int levelNumber) {
        TextButton button = new TextButton("Level " + levelNumber, ui.getSkin());
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
            boolean unlocked = game.isLevelUnlocked(level);
            boolean completed = game.getProgressService().isLevelCompleted(level);
            boolean selected = level == selectedLevel;

            TextButton button = levelButtons[level - 1];
            button.setDisabled(!unlocked);
            button.setTouchable(unlocked ? Touchable.enabled : Touchable.disabled);
            if (selected && unlocked) {
                button.setColor(GameTheme.PATH_TEAL);
            } else if (unlocked) {
                button.setColor(GameTheme.UI_TEXT);
            } else {
                button.setColor(GameTheme.UI_TEXT_MUTED);
            }

            Label status = levelStatusLabels[level - 1];
            if (!unlocked) {
                status.setText("Locked");
            } else if (completed) {
                status.setText("Completed · " + game.getLevelStartingGold(level) + "g");
            } else {
                status.setText("Start: " + game.getLevelStartingGold(level) + " gold");
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
