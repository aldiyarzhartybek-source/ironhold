package com.ironhold.game.model;

/**
 * Short-lived impact marker spawned at the point a projectile strikes an enemy.
 *
 * <p>Stores its <em>original</em> lifetime alongside the remaining one so renderers
 * can compute a normalised progress {@code (ttl / maxTtl) ∈ [0, 1]} for animation
 * curves (radial burst expansion, fade-out, ring scale, etc.) without needing to
 * thread the original duration through every call site.
 */
public final class HitEffect {

    private final float x;
    private final float y;
    private final float maxTtlSec;
    private float ttlSec;

    public HitEffect(float x, float y, float ttlSec) {
        this.x         = x;
        this.y         = y;
        this.maxTtlSec = ttlSec;
        this.ttlSec    = ttlSec;
    }

    public float getX()         { return x; }
    public float getY()         { return y; }
    public float getTtlSec()    { return ttlSec; }
    public float getMaxTtlSec() { return maxTtlSec; }

    public void setTtlSec(float ttlSec) {
        this.ttlSec = ttlSec;
    }

    /**
     * Progress through the effect's lifetime, {@code 0} at spawn and {@code 1} at
     * extinction. Safe against zero-duration effects.
     */
    public float getProgress() {
        if (maxTtlSec <= 0f) return 1f;
        float p = 1f - (ttlSec / maxTtlSec);
        if (p < 0f) return 0f;
        if (p > 1f) return 1f;
        return p;
    }
}
