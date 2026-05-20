package com.ironhold.game.model;

/**
 * Short-lived chain-lightning visual spawned when a Lightning tower fires.
 *
 * <p>Stores pre-computed zigzag waypoints for the full chain
 * (tower → enemy1 → enemy2 → …). Waypoints are interleaved (x0,y0,x1,y1,…)
 * with extra offset midpoints inserted per segment to create the bolt shape.
 * Pre-computing avoids per-frame randomness that would cause flickering.
 */
public final class LightningEffect {

    private final float[] waypoints;   // interleaved x,y pairs
    private float ttlSec;
    private final float maxTtlSec;

    public LightningEffect(float[] waypoints, float ttlSec) {
        this.waypoints  = waypoints;
        this.maxTtlSec  = ttlSec;
        this.ttlSec     = ttlSec;
    }

    /** Interleaved (x,y) pairs forming the full zigzag path. */
    public float[] getWaypoints() { return waypoints; }

    public float getTtlSec() { return ttlSec; }

    public void setTtlSec(float v) { this.ttlSec = v; }

    /** {@code 0} at spawn → {@code 1} at extinction. */
    public float getProgress() {
        if (maxTtlSec <= 0f) return 1f;
        float p = 1f - ttlSec / maxTtlSec;
        return Math.max(0f, Math.min(1f, p));
    }
}
