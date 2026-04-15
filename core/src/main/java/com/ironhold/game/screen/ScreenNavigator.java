package com.ironhold.game.screen;

/**
 * Абстракция переключения экранов; реализация живёт в {@link ScreenManager}.
 */
public interface ScreenNavigator {

    void goTo(ScreenId id);
}
