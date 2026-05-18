package com.ironhold.game;

import java.util.Objects;

/**
 * Default production implementation of the level frame update template.
 */
public final class DefaultLevelUpdateTemplate extends LevelUpdateTemplate {

    private final WaveEventSystem waveEventSystem;
    private final SpawnSystem spawnSystem;
    private final CombatRuntimeSystem combatSystem;
    private final LevelUpdatePostCombatHandler postCombatHandler;

    public DefaultLevelUpdateTemplate(
        WaveEventSystem waveEventSystem,
        SpawnSystem spawnSystem,
        CombatRuntimeSystem combatSystem,
        LevelUpdatePostCombatHandler postCombatHandler
    ) {
        this.waveEventSystem = Objects.requireNonNull(waveEventSystem, "waveEventSystem");
        this.spawnSystem = Objects.requireNonNull(spawnSystem, "spawnSystem");
        this.combatSystem = Objects.requireNonNull(combatSystem, "combatSystem");
        this.postCombatHandler = Objects.requireNonNull(postCombatHandler, "postCombatHandler");
    }

    @Override
    protected void updateLevelState(LevelUpdateFrameContext context) {
        context.getLevelState().update(context.getScaledDeltaSec());
    }

    @Override
    protected void publishWaveEvents(LevelUpdateFrameContext context) {
        waveEventSystem.publishPendingWaveEvents(context.getState());
    }

    @Override
    protected void processSpawns(LevelUpdateFrameContext context) {
        spawnSystem.processPendingSpawns(context.getState());
    }

    @Override
    protected void updateCombat(LevelUpdateFrameContext context) {
        combatSystem.update(context.getState(), context.getScaledDeltaSec());
    }

    @Override
    protected void afterCombat(LevelUpdateFrameContext context) {
        context.getLevelState().tryCompleteActiveWave(context.isFieldEmpty());
        waveEventSystem.publishPendingWaveEvents(context.getState());
        postCombatHandler.afterCombat(context.getState(), context.getLevelState());
    }
}
