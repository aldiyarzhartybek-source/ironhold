package com.ironhold.game.model;

/**
 * Runtime tower model (skeleton for MVP mechanics).
 */
public final class Tower {

    private final String id;
    private final int cost;
    private final float range;
    private final int damage;
    private final float fireRateSec;

    public Tower(String id, int cost, float range, int damage, float fireRateSec) {
        this.id = id;
        this.cost = cost;
        this.range = range;
        this.damage = damage;
        this.fireRateSec = fireRateSec;
    }

    public String getId() {
        return id;
    }

    public int getCost() {
        return cost;
    }

    public float getRange() {
        return range;
    }

    public int getDamage() {
        return damage;
    }

    public float getFireRateSec() {
        return fireRateSec;
    }
}
