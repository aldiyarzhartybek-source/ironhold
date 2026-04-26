package com.ironhold.events;

public final class WaveStartedEvent implements GameEvent {

    private final int waveNumber;
    private final int totalWaves;

    public WaveStartedEvent(int waveNumber, int totalWaves) {
        this.waveNumber = waveNumber;
        this.totalWaves = totalWaves;
    }

    public int getWaveNumber() {
        return waveNumber;
    }

    public int getTotalWaves() {
        return totalWaves;
    }
}
