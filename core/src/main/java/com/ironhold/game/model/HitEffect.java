package com.ironhold.game.model;

/**
 * Short-lived impact effect marker rendered in the FX layer.
 */
public final class HitEffect {

    private float x;
    private float y;
    private float ttlSec;

    public HitEffect(float x, float y, float ttlSec) {
        this.x = x;
        this.y = y;
        this.ttlSec = ttlSec;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getTtlSec() {
        return ttlSec;
    }

    public void setTtlSec(float ttlSec) {
        this.ttlSec = ttlSec;
    }
}
