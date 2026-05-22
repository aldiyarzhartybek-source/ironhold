package com.ironhold.core;

import com.badlogic.gdx.Game;
import com.ironhold.assets.AssetService;
import com.ironhold.events.SimpleEventBus;
import com.ironhold.game.GameContext;
import com.ironhold.game.GameFacade;
import com.ironhold.game.model.GameModelMapper;
import com.ironhold.config.GameConfig;
import com.ironhold.config.LevelCatalog;
import com.ironhold.game.screen.ScreenId;
import com.ironhold.game.screen.ScreenManager;
import com.ironhold.save.LocalFileProgressRepository;
import com.ironhold.save.ProgressService;

/**
 * Точка входа игры в модуле core: жизненный цикл LibGDX и делегирование экранов {@link ScreenManager}.
 */
public class IronHoldGame extends Game {

    private AssetService assets;
    private ScreenManager screens;
    private GameContext context;
    private GameFacade facade;
    private GameConfig config;

    @Override
    public void create() {
        ProgressService progressService = new ProgressService(new LocalFileProgressRepository());
        progressService.load();
        config = GameConfig.loadDefault();
        context = new GameContext(new SimpleEventBus(), progressService, config.isDebugMode());
        LevelCatalog levelCatalog = LevelCatalog.load(config);

        assets = new AssetService();
        screens = new ScreenManager(this);
        facade = new GameFacade(
            context,
            assets,
            screens,
            GameModelMapper.mapEnemies(config),
            GameModelMapper.mapTowers(config),
            levelCatalog,
            GameModelMapper.mapEconomy(config)
        );
        screens.register(ScreenId.LOADING, () -> new LoadingScreen(facade));
        screens.register(ScreenId.MENU, () -> new MenuScreen(facade));
        screens.register(ScreenId.LEVEL_SELECT, () -> new LevelSelectScreen(facade));
        screens.register(ScreenId.GAME, () -> new GameScreen(facade));
        screens.goTo(ScreenId.LOADING);
    }

    @Override
    public void dispose() {
        super.dispose();
        facade.dispose();
        facade.getEventBus().clear();
        assets.dispose();
    }

    public GameContext getContext() {
        return context;
    }

    public GameConfig getConfig() {
        return config;
    }

    public GameFacade getFacade() {
        return facade;
    }
}
