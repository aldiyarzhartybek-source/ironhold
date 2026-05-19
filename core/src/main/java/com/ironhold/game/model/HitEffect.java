package com.ironhold.game.model;

/**
 * Short-lived impact marker — position and remaining lifetime only.
 * Visual rendering deferred to a future implementation.
 */
public final class HitEffect {

    private final float x;
    private final float y;
    private float ttlSec;

    public HitEffect(float x, float y, float ttlSec) {
        this.x      = x;
        this.y      = y;
        this.ttlSec = ttlSec;
    }

    public float getX()      { return x; }
    public float getY()      { return y; }
    public float getTtlSec() { return ttlSec; }

    public void setTtlSec(float ttlSec) {
        this.ttlSec = ttlSec;
    }
}
