package com.ironhold.events;

public final class WaveCompletedEvent implements GameEvent {

    private final int waveNumber;
    private final int totalWaves;

    public WaveCompletedEvent(int waveNumber, int totalWaves) {
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
