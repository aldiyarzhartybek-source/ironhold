package com.ironhold.core;

import com.badlogic.gdx.Game;
import com.ironhold.assets.AssetService;
import com.ironhold.events.GameStartedEvent;
import com.ironhold.events.SimpleEventBus;
import com.ironhold.game.GameContext;
import com.ironhold.game.GameFacade;
import com.ironhold.game.screen.ScreenId;
import com.ironhold.game.screen.ScreenManager;

/**
 * Точка входа игры в модуле core: жизненный цикл LibGDX и делегирование экранов {@link ScreenManager}.
 */
public class IronHoldGame extends Game {

    private AssetService assets;
    private ScreenManager screens;
    private GameContext context;
    private GameFacade facade;

    @Override
    public void create() {
        context = new GameContext(new SimpleEventBus());
        assets = new AssetService();
        screens = new ScreenManager(this);
        facade = new GameFacade(context, assets, screens);
        screens.register(ScreenId.LOADING, () -> new LoadingScreen(facade));
        screens.register(ScreenId.MENU, () -> new MenuScreen(facade));
        screens.register(ScreenId.GAME, () -> new GameScreen(facade));
        facade.getEventBus().publish(new GameStartedEvent());
        screens.goTo(ScreenId.LOADING);
    }

    @Override
    public void dispose() {
        super.dispose();
        facade.getEventBus().clear();
        assets.dispose();
    }

    public GameContext getContext() {
        return context;
    }

    public GameFacade getFacade() {
        return facade;
    }
}
