package com.ironhold.game.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

import java.util.EnumMap;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Регистрация фабрик экранов и переключение через {@link Game#setScreen(Screen)}.
 */
public final class ScreenManager implements ScreenNavigator {

    private final Game game;
    private final EnumMap<ScreenId, Supplier<Screen>> factories = new EnumMap<>(ScreenId.class);

    public ScreenManager(Game game) {
        this.game = Objects.requireNonNull(game, "game");
    }

    public void register(ScreenId id, Supplier<Screen> factory) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(factory, "factory");
        factories.put(id, factory);
    }

    @Override
    public void goTo(ScreenId id) {
        Supplier<Screen> factory = factories.get(id);
        if (factory == null) {
            throw new IllegalStateException("Screen not registered: " + id);
        }
        game.setScreen(factory.get());
    }
}
