package com.ironhold.game;

/**
 * Per-level session metrics based on wall-clock time (not scaled game delta).
 */
public final class LevelSessionStats {

    private long levelStartNanos;
    private Long levelEndNanos;

    public void reset() {
        levelStartNanos = 0L;
        levelEndNanos = null;
    }

    public void markStarted() {
        levelStartNanos = System.nanoTime();
        levelEndNanos = null;
    }

    public void markEnded() {
        if (levelEndNanos == null && levelStartNanos > 0L) {
            levelEndNanos = System.nanoTime();
        }
    }

    public float getElapsedSec() {
        if (levelStartNanos <= 0L) {
            return 0f;
        }
        long endNanos = levelEndNanos != null ? levelEndNanos : System.nanoTime();
        return Math.max(0f, (endNanos - levelStartNanos) / 1_000_000_000f);
    }

    public String getElapsedFormatted() {
        return LevelTimeFormat.formatMmSs(getElapsedSec());
    }
}
