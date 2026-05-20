package com.ironhold.game.model;

/**
 * Enemy instance currently spawned on a level.
 */
public final class ActiveEnemy {

    private final String runtimeId;
    private final String enemyId;
    private final int maxHp;
    private int currentHp;
    private final float speed;
    private final int reward;
    private float x;
    private float y;
    private int targetWaypointIndex;

    // ── Visual feedback state (driven by CombatRuntimeSystem, read by EnemyShapeRenderer) ──
    /** Remaining time of the white-flash + scale-pulse triggered by a projectile hit. */
    private float hitFlashTtlSec;
    /** Duration assigned when the flash was triggered; used by the renderer to compute progress. */
    private float hitFlashMaxSec;

    public ActiveEnemy(
        String runtimeId,
        String enemyId,
        int maxHp,
        int currentHp,
        float speed,
        int reward,
        float x,
        float y,
        int targetWaypointIndex
    ) {
        this.runtimeId = runtimeId;
        this.enemyId = enemyId;
        this.maxHp = maxHp;
        this.currentHp = currentHp;
        this.speed = speed;
        this.reward = reward;
        this.x = x;
        this.y = y;
        this.targetWaypointIndex = targetWaypointIndex;
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public String getEnemyId() {
        return enemyId;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public float getSpeed() {
        return speed;
    }

    public int getReward() {
        return reward;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getTargetWaypointIndex() {
        return targetWaypointIndex;
    }

    public void setCurrentHp(int currentHp) {
        this.currentHp = currentHp;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setTargetWaypointIndex(int targetWaypointIndex) {
        this.targetWaypointIndex = targetWaypointIndex;
    }

    // ── Hit-flash state (visual feedback only — see EnemyShapeRenderer) ──

    public float getHitFlashTtlSec() {
        return hitFlashTtlSec;
    }

    public float getHitFlashMaxSec() {
        return hitFlashMaxSec;
    }

    /**
     * Triggers a fresh hit-flash. Stacks by taking the longer remaining lifetime so
     * that rapid fire keeps the flash visible without resetting the animation curve
     * mid-frame.
     */
    public void triggerHitFlash(float durationSec) {
        if (durationSec <= 0f) {
            return;
        }
        if (durationSec > this.hitFlashTtlSec) {
            this.hitFlashTtlSec = durationSec;
            this.hitFlashMaxSec = durationSec;
        }
    }

    /** Decays the flash timer; called once per tick by {@code CombatRuntimeSystem}. */
    public void tickHitFlash(float deltaSec) {
        if (this.hitFlashTtlSec > 0f) {
            this.hitFlashTtlSec = Math.max(0f, this.hitFlashTtlSec - deltaSec);
            if (this.hitFlashTtlSec == 0f) {
                this.hitFlashMaxSec = 0f;
            }
        }
    }
}
