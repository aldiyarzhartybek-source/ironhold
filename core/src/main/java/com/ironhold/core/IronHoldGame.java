package com.ironhold.core;

import com.badlogic.gdx.Game;
import com.ironhold.assets.AssetService;
import com.ironhold.events.GameStartedEvent;
import com.ironhold.events.SimpleEventBus;
import com.ironhold.game.GameContext;
import com.ironhold.game.screen.ScreenId;
import com.ironhold.game.screen.ScreenManager;

/**
 * Точка входа игры в модуле core: жизненный цикл LibGDX и делегирование экранов {@link ScreenManager}.
 */
public class IronHoldGame extends Game {

    private AssetService assets;
    private ScreenManager screens;
    private GameContext context;

    @Override
    public void create() {
        context = new GameContext(new SimpleEventBus());
        assets = new AssetService();
        screens = new ScreenManager(this);
        screens.register(ScreenId.LOADING, () -> new LoadingScreen(screens, assets));
        screens.register(ScreenId.MENU, () -> new MenuScreen(screens, assets));
        screens.register(ScreenId.GAME, () -> new GameScreen(assets));
        context.getEventBus().publish(new GameStartedEvent());
        screens.goTo(ScreenId.LOADING);
    }

    @Override
    public void dispose() {
        super.dispose();
        context.getEventBus().clear();
        assets.dispose();
    }

    public GameContext getContext() {
        return context;
    }
}
