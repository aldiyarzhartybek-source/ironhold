package com.ironhold.game.model;

/**
 * Runtime economy state skeleton.
 */
public final class EconomyState {

    private int gold;
    private final float killRewardMultiplier;
    private final float buildRefundRate;

    public EconomyState(int gold, float killRewardMultiplier, float buildRefundRate) {
        this.gold = gold;
        this.killRewardMultiplier = killRewardMultiplier;
        this.buildRefundRate = buildRefundRate;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public float getKillRewardMultiplier() {
        return killRewardMultiplier;
    }

    public float getBuildRefundRate() {
        return buildRefundRate;
    }
}
