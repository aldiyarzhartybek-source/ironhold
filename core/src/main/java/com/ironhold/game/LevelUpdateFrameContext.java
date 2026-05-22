package com.ironhold.game;

import com.ironhold.level.RuntimeLevelState;

import java.util.Objects;

/**
 * Inputs for a single level simulation frame (Template Method).
 */
public final class LevelUpdateFrameContext {

    private final GameRuntimeState state;
    /** Delta for combat, projectiles, effects — scaled by game speed (1x / 2x). */
    private final float scaledDeltaSec;
    /** Wall-clock delta for wave spawn timer — same real spawn rate at 1x and 2x speed. */
    private final float realDeltaSec;

    public LevelUpdateFrameContext(GameRuntimeState state, float scaledDeltaSec, float realDeltaSec) {
        this.state = Objects.requireNonNull(state, "state");
        this.scaledDeltaSec = Math.max(0f, scaledDeltaSec);
        this.realDeltaSec = Math.max(0f, realDeltaSec);
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

    public float getRealDeltaSec() {
        return realDeltaSec;
    }

    public boolean isFieldEmpty() {
        return state.getActiveEnemies().isEmpty();
    }
}
