package com.ironhold.game;

/**
 * Template Method for the fixed per-frame level update algorithm.
 */
public abstract class LevelUpdateTemplate {

    /**
     * Template method: order of steps must not change without an explicit design decision.
     */
    public final void updateFrame(LevelUpdateFrameContext context) {
        updateLevelState(context);
        publishWaveEvents(context);
        processSpawns(context);
        updateCombat(context);
        afterCombat(context);
    }

    protected abstract void updateLevelState(LevelUpdateFrameContext context);

    protected abstract void publishWaveEvents(LevelUpdateFrameContext context);

    protected abstract void processSpawns(LevelUpdateFrameContext context);

    protected abstract void updateCombat(LevelUpdateFrameContext context);

    protected abstract void afterCombat(LevelUpdateFrameContext context);
}
