package com.ironhold.game;

import com.ironhold.level.RuntimeLevelState;

import java.util.Objects;

/**
 * Inputs for a single level simulation frame (Template Method).
 */
public final class LevelUpdateFrameContext {

    private final GameRuntimeState state;
    private final float scaledDeltaSec;

    public LevelUpdateFrameContext(GameRuntimeState state, float scaledDeltaSec) {
        this.state = Objects.requireNonNull(state, "state");
        this.scaledDeltaSec = Math.max(0f, scaledDeltaSec);
    }

    public GameRuntimeState getState() {
        return state;
    }

    public RuntimeLevelState getLevelState() {
        return state.getRuntimeLevelState();
    }

    public float getScaledDeltaSec() {
        return scaledDeltaSec;
    }

    public boolean isFieldEmpty() {
        return state.getActiveEnemies().isEmpty();
    }
}
