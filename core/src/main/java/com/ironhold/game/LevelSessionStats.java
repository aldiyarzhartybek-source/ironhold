package com.ironhold.game;

/**
 * Per-level session metrics: wall-clock time, kills, and gross gold spent.
 */
public final class LevelSessionStats {

    private long levelStartNanos;
    private Long levelEndNanos;
    private int kills;
    private int goldSpent;

    public void reset() {
        levelStartNanos = 0L;
        levelEndNanos = null;
        kills = 0;
        goldSpent = 0;
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

    public void recordKill() {
        kills++;
    }

    /**
     * Gross gold spent on builds/upgrades; never reduced on sell.
     */
    public void addGoldSpent(int amount) {
        if (amount > 0) {
            goldSpent += amount;
        }
    }

    public int getKills() {
        return kills;
    }

    public int getGoldSpent() {
        return goldSpent;
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
