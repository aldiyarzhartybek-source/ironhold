package com.ironhold.game;

import com.ironhold.events.EventBus;
import com.ironhold.save.ProgressService;

import java.util.Objects;

/**
 * Shared runtime context for game-wide services.
 */
public final class GameContext {

    private final EventBus eventBus;
    private final ProgressService progressService;
    private final boolean debugMode;

    public GameContext(EventBus eventBus, ProgressService progressService, boolean debugMode) {
        this.eventBus = Objects.requireNonNull(eventBus, "eventBus");
        this.progressService = Objects.requireNonNull(progressService, "progressService");
        this.debugMode = debugMode;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public ProgressService getProgressService() {
        return progressService;
    }

    public boolean isDebugMode() {
        return debugMode;
    }
}
