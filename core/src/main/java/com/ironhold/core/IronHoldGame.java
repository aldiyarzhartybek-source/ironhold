package com.ironhold.core;

import com.badlogic.gdx.Game;
import com.ironhold.assets.AssetService;
import com.ironhold.config.GameConfig;
import com.ironhold.game.screen.ScreenId;
import com.ironhold.game.screen.ScreenManager;

/**
 * Точка входа игры в модуле core: жизненный цикл LibGDX и делегирование экранов {@link ScreenManager}.
 */
public class IronHoldGame extends Game {

    private AssetService assets;
    private ScreenManager screens;
    private GameConfig config;

    @Override
    public void create() {
        config = GameConfig.loadDefault();
        assets = new AssetService();
        screens = new ScreenManager(this);
        screens.register(ScreenId.LOADING, () -> new LoadingScreen(screens, assets));
        screens.register(ScreenId.MENU, () -> new MenuScreen(screens, assets));
        screens.register(ScreenId.GAME, () -> new GameScreen(assets));
        screens.goTo(ScreenId.LOADING);
    }

    @Override
    public void dispose() {
        super.dispose();
        assets.dispose();
    }

    public GameConfig getConfig() {
        return config;
    }
}
