package com.ironhold.game;

import com.ironhold.events.EventBus;

import java.util.Objects;

/**
 * Shared runtime context for game-wide services.
 */
public final class GameContext {

    private final EventBus eventBus;

    public GameContext(EventBus eventBus) {
        this.eventBus = Objects.requireNonNull(eventBus, "eventBus");
    }

    public EventBus getEventBus() {
        return eventBus;
    }
}
