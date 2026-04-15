package com.ironhold.core;

import com.badlogic.gdx.Game;
import com.ironhold.game.screen.ScreenId;
import com.ironhold.game.screen.ScreenManager;

/**
 * Точка входа игры в модуле core: жизненный цикл LibGDX и делегирование экранов {@link ScreenManager}.
 */
public class IronHoldGame extends Game {

    private ScreenManager screens;

    @Override
    public void create() {
        screens = new ScreenManager(this);
        screens.register(ScreenId.PLAY, () -> new PlayScreen(screens));
        screens.goTo(ScreenId.PLAY);
    }
}
