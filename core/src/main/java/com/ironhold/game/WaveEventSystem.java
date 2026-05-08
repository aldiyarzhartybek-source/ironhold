package com.ironhold.game;

import com.ironhold.events.EventBus;
import com.ironhold.events.WaveCompletedEvent;
import com.ironhold.events.WaveStartedEvent;

import java.util.Objects;

/**
 * Publishes wave lifecycle events from runtime level state.
 */
public final class WaveEventSystem {
    private final EventBus eventBus;

    public WaveEventSystem(EventBus eventBus) {
        this.eventBus = Objects.requireNonNull(eventBus, "eventBus");
    }

    public void publishPendingWaveEvents(GameRuntimeState state) {
        for (int waveNumber : state.getRuntimeLevelState().consumePendingWaveStartedNumbers()) {
            eventBus.publish(new WaveStartedEvent(waveNumber, state.getRuntimeLevelState().getTotalWaves()));
        }
        for (int waveNumber : state.getRuntimeLevelState().consumePendingWaveCompletedNumbers()) {
            eventBus.publish(new WaveCompletedEvent(waveNumber, state.getRuntimeLevelState().getTotalWaves()));
        }
    }
}
