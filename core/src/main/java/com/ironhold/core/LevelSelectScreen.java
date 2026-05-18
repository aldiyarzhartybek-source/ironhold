package com.ironhold.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ironhold.game.GameFacade;
import com.ironhold.game.screen.ScreenId;
import com.ironhold.save.ProgressService;
import com.ironhold.ui.GameTheme;
import com.ironhold.ui.UiLayer;

import java.util.Objects;

/**
 * Level and mode selection (stub for Stage 4 task 3; navigation shell for task 2).
 */
public final class LevelSelectScreen extends ScreenAdapter {

    private final GameFacade game;
    private final UiLayer ui;
    private Label progressLabel;

    public LevelSelectScreen(GameFacade game) {
        this.game = Objects.requireNonNull(game, "game");
        this.ui = new UiLayer(game.getAssets().getSkin());
        initLayout();
    }

    @Override
    public void show() {
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

        TextButton backButton = new TextButton("Back", ui.getSkin());
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.getScreens().goTo(ScreenId.MENU);
            }
        });

        Table root = new Table();
        root.setFillParent(true);
        root.defaults().pad(10f);
        root.add(title).padBottom(12f).row();
        root.add(progressLabel).padBottom(28f).row();
        root.add(backButton).width(240f).height(52f);
        ui.getStage().addActor(root);
    }

    private void refreshProgressLabel() {
        int highest = game.getHighestUnlockedLevel();
        progressLabel.setText("Unlocked levels: 1-" + highest + " of " + ProgressService.MAX_LEVELS);
    }
}
