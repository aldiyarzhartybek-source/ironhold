package com.ironhold.level;

/**
 * Wave lifecycle within a running level ({@link LevelStatus#RUNNING}).
 * Terminal level outcomes remain in {@link LevelStatus}.
 */
public enum WavePhase {
    BETWEEN_WAVES,
    WAVE_ACTIVE
}
