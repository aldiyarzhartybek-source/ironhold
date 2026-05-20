package com.ironhold.game.model;

/**
 * Short-lived splash ring at a mortar impact point.
 *
 * <p>{@link #blastRadius} is the full AoE radius shown when the ring finishes expanding.
 */
public final class MortarExplosionEffect {

    private final float x;
    private final float y;
    private final float blastRadius;
    private final float maxTtlSec;
    private float ttlSec;

    public MortarExplosionEffect(float x, float y, float blastRadius, float ttlSec) {
        this.x = x;
        this.y = y;
        this.blastRadius = blastRadius;
        this.maxTtlSec = ttlSec;
        this.ttlSec = ttlSec;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getBlastRadius() { return blastRadius; }
    public float getTtlSec() { return ttlSec; }

    public void setTtlSec(float v) { this.ttlSec = v; }

    /** {@code 0} at spawn → {@code 1} at extinction. */
    public float getProgress() {
        if (maxTtlSec <= 0f) return 1f;
        float p = 1f - ttlSec / maxTtlSec;
        return Math.max(0f, Math.min(1f, p));
    }
}
