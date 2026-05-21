package com.ironhold.game.model;

/**
 * Runtime economy state skeleton.
 */
public final class EconomyState {

    private int gold;
    private final float killRewardMultiplier;
    private final float sellRecoveryRate;

    public EconomyState(int gold, float killRewardMultiplier, float sellRecoveryRate) {
        this.gold = gold;
        this.killRewardMultiplier = killRewardMultiplier;
        this.sellRecoveryRate = sellRecoveryRate;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public boolean canSpend(int amount) {
        return amount >= 0 && gold >= amount;
    }

    public boolean trySpend(int amount) {
        if (!canSpend(amount)) {
            return false;
        }
        gold -= amount;
        return true;
    }

    public void addGold(int amount) {
        if (amount <= 0) {
            return;
        }
        gold += amount;
    }

    public int calculateKillReward(int baseReward) {
        if (baseReward <= 0) {
            return 0;
        }
        int reward = Math.round(baseReward * killRewardMultiplier);
        return Math.max(0, reward);
    }

    public float getKillRewardMultiplier() {
        return killRewardMultiplier;
    }

    public float getSellRecoveryRate() {
        return sellRecoveryRate;
    }
}
