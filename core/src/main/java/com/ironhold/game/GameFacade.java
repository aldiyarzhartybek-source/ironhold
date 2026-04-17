package com.ironhold.game;

import com.ironhold.assets.AssetService;
import com.ironhold.events.EventBus;
import com.ironhold.game.screen.ScreenNavigator;

import java.util.Objects;

/**
 * Единая точка доступа к runtime-сервисам (фасад для экранов и будущих систем).
 */
public final class GameFacade {

    private final GameContext context;
    private final AssetService assets;
    private final ScreenNavigator screens;

    public GameFacade(GameContext context, AssetService assets, ScreenNavigator screens) {
        this.context = Objects.requireNonNull(context, "context");
        this.assets = Objects.requireNonNull(assets, "assets");
        this.screens = Objects.requireNonNull(screens, "screens");
    }

    public GameContext getContext() {
        return context;
    }

    public EventBus getEventBus() {
        return context.getEventBus();
    }

    public AssetService getAssets() {
        return assets;
    }

    public ScreenNavigator getScreens() {
        return screens;
    }
}
