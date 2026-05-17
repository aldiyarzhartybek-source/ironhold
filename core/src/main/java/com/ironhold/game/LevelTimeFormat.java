package com.ironhold.game;

/**
 * Formats wall-clock level duration for HUD and end-of-level UI.
 */
public final class LevelTimeFormat {

    private LevelTimeFormat() {
    }

    public static String formatMmSs(float elapsedSec) {
        int totalSeconds = Math.max(0, (int) elapsedSec);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
