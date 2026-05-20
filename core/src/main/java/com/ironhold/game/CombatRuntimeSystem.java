package com.ironhold.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.ironhold.events.EnemyKilledEvent;
import com.ironhold.events.EventBus;
import com.ironhold.game.model.ActiveEnemy;
import com.ironhold.game.model.ActiveProjectile;
import com.ironhold.game.model.EconomyState;
import com.ironhold.game.model.HitEffect;
import com.ironhold.game.model.LightningEffect;
import com.ironhold.game.model.PlacedTower;
import com.ironhold.game.model.TowerTargeting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Owns enemy movement, tower firing, projectile updates, and hit effects.
 */
public final class CombatRuntimeSystem {
    private static final float ENEMY_SPEED_MULTIPLIER = 10.0f;
    private static final float MIN_RUNTIME_ENEMY_SPEED = 0.1f;
    private static final float PROJECTILE_SPEED = 320f;
    private static final float PROJECTILE_HIT_RADIUS = 12f;

    private static final String LIGHTNING_TOWER_ID = "lightning_tower";
    /** Max enemies in one chain. */
    private static final int LIGHTNING_CHAIN_MAX = 3;
    /** How long the visual bolt stays on screen (seconds). */
    private static final float LIGHTNING_FLASH_SEC = 0.08f;
    /** Extra offset midpoints per bolt segment, pixels perpendicular to the direction. */
    private static final float LIGHTNING_ZAG_AMP = 14f;

    private final EventBus eventBus;
    private final EconomyState economy;

    public CombatRuntimeSystem(EventBus eventBus, EconomyState economy) {
        this.eventBus = Objects.requireNonNull(eventBus, "eventBus");
        this.economy  = Objects.requireNonNull(economy, "economy");
    }

    public boolean debugDefeatFirstEnemy(GameRuntimeState state) {
        if (state.getActiveEnemies().isEmpty()) {
            return false;
        }
        ActiveEnemy defeated = state.getActiveEnemies().remove(0);
        awardKill(state, defeated);
        return true;
    }

    public void update(GameRuntimeState state, float deltaSec) {
        float safeDeltaSec = Math.max(0f, deltaSec);
        updateEnemyMovement(state, safeDeltaSec);
        updateProjectiles(state, safeDeltaSec);
        updateTowerCombat(state, safeDeltaSec);
        updateHitEffects(state, safeDeltaSec);
        updateLightningEffects(state, safeDeltaSec);
    }

    private void updateEnemyMovement(GameRuntimeState state, float deltaSec) {
        if (deltaSec <= 0f || state.getActiveEnemies().isEmpty() || state.getEnemyPath().size() < 2) {
            return;
        }
        List<ActiveEnemy> escapedEnemies = new ArrayList<>();
        for (ActiveEnemy enemy : state.getActiveEnemies()) {
            if (advanceEnemyAlongPath(enemy, state.getEnemyPath(), deltaSec)) {
                escapedEnemies.add(enemy);
            }
        }
        if (!escapedEnemies.isEmpty()) {
            state.getActiveEnemies().removeAll(escapedEnemies);
            for (int i = 0; i < escapedEnemies.size(); i++) {
                state.getRuntimeLevelState().onEnemyEscaped();
            }
        }
    }

    /** How long the enemy hit-flash (white tint + scale pulse) lasts. */
    private static final float HIT_FLASH_DURATION_SEC = 0.18f;

    private void updateTowerCombat(GameRuntimeState state, float deltaSec) {
        if (deltaSec <= 0f || state.getPlacedTowers().isEmpty()) {
            for (PlacedTower tower : state.getPlacedTowers()) {
                tower.tickPulse(deltaSec);
            }
            return;
        }
        boolean hasTargets = !state.getActiveEnemies().isEmpty();
        for (PlacedTower tower : state.getPlacedTowers()) {
            tower.tickPulse(deltaSec);

            float cooldown = Math.max(0f, tower.getCooldownSec() - deltaSec);
            tower.setCooldownSec(cooldown);

            if (!hasTargets || cooldown > 0f) {
                continue;
            }

            if (LIGHTNING_TOWER_ID.equals(tower.getTowerId())) {
                fireLightning(state, tower);
            } else {
                fireProjectile(state, tower);
            }
        }
    }

    private void fireProjectile(GameRuntimeState state, PlacedTower tower) {
        ActiveEnemy target = pickTargetForTower(state, tower);
        if (target == null) return;
        tower.setCooldownSec(tower.getFireRateSec());
        state.getActiveProjectiles().add(new ActiveProjectile(
            "projectile-" + state.getNextProjectileInstanceId(),
            target.getRuntimeId(),
            tower.getDamage(),
            tower.getX(),
            tower.getY(),
            PROJECTILE_SPEED
        ));
    }

    private void fireLightning(GameRuntimeState state, PlacedTower tower) {
        List<ActiveEnemy> chain = buildChainTargets(state, tower);
        if (chain.isEmpty()) return;

        tower.setCooldownSec(tower.getFireRateSec());
        tower.setLockedTargetRuntimeId(chain.get(0).getRuntimeId());

        // Instant damage + hit flash for all chained enemies
        for (ActiveEnemy enemy : chain) {
            enemy.setCurrentHp(enemy.getCurrentHp() - tower.getDamage());
            enemy.triggerHitFlash(HIT_FLASH_DURATION_SEC);
            state.getHitEffects().add(new HitEffect(enemy.getX(), enemy.getY(), 0.22f));
        }

        // Remove killed enemies
        List<ActiveEnemy> killed = new ArrayList<>();
        for (ActiveEnemy enemy : chain) {
            if (enemy.getCurrentHp() <= 0 && !killed.contains(enemy)) {
                killed.add(enemy);
            }
        }
        for (ActiveEnemy enemy : killed) {
            awardKill(state, enemy);
        }
        state.getActiveEnemies().removeAll(killed);

        // Build zigzag waypoints: tower → enemy1 [→ enemy2 [→ enemy3]]
        state.getLightningEffects().add(buildLightningEffect(tower, chain));
    }

    /**
     * Finds up to {@link #LIGHTNING_CHAIN_MAX} enemies to chain between.
     * Primary = nearest to tower in range. Each subsequent hop picks the nearest
     * remaining enemy to the previous target (no range check after the first hit).
     */
    private List<ActiveEnemy> buildChainTargets(GameRuntimeState state, PlacedTower tower) {
        float rangeSq = tower.getRange() * tower.getRange();
        List<ActiveEnemy> chain = new ArrayList<>();
        List<ActiveEnemy> remaining = new ArrayList<>(state.getActiveEnemies());

        float cx = tower.getX();
        float cy = tower.getY();

        for (int step = 0; step < LIGHTNING_CHAIN_MAX && !remaining.isEmpty(); step++) {
            ActiveEnemy nearest = null;
            float bestSq = Float.MAX_VALUE;
            for (ActiveEnemy enemy : remaining) {
                float dx = cx - enemy.getX();
                float dy = cy - enemy.getY();
                float dsq = dx * dx + dy * dy;
                // First target must be in tower range; chained targets allowed anywhere
                if (step == 0 && dsq > rangeSq) continue;
                if (dsq < bestSq) {
                    bestSq = dsq;
                    nearest = enemy;
                }
            }
            if (nearest == null) break;
            chain.add(nearest);
            remaining.remove(nearest);
            cx = nearest.getX();
            cy = nearest.getY();
        }
        return chain;
    }

    /**
     * Builds a {@link LightningEffect} with pre-computed zigzag waypoints.
     * Each segment tower→e1, e1→e2, … gets one perpendicular midpoint offset
     * to create the characteristic bolt shape.
     */
    private static LightningEffect buildLightningEffect(PlacedTower tower, List<ActiveEnemy> chain) {
        // Total waypoints: tower + (midpoint + enemy) per chain step
        int segCount = chain.size();
        float[] wp = new float[(1 + segCount * 2) * 2]; // each node = 2 floats
        int idx = 0;

        float prevX = tower.getX();
        float prevY = tower.getY();
        wp[idx++] = prevX;
        wp[idx++] = prevY;

        for (int i = 0; i < segCount; i++) {
            float nextX = chain.get(i).getX();
            float nextY = chain.get(i).getY();

            // Perpendicular midpoint offset (alternating side per segment)
            float mx = (prevX + nextX) * 0.5f;
            float my = (prevY + nextY) * 0.5f;
            float dx = nextX - prevX;
            float dy = nextY - prevY;
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            float sign = (i % 2 == 0) ? 1f : -1f;
            if (len > 0.001f) {
                float perpX = -dy / len;
                float perpY =  dx / len;
                float amp = LIGHTNING_ZAG_AMP * (0.7f + 0.3f * MathUtils.random());
                mx += perpX * amp * sign;
                my += perpY * amp * sign;
            }
            wp[idx++] = mx;
            wp[idx++] = my;
            wp[idx++] = nextX;
            wp[idx++] = nextY;

            prevX = nextX;
            prevY = nextY;
        }
        return new LightningEffect(wp, LIGHTNING_FLASH_SEC);
    }

    private void updateLightningEffects(GameRuntimeState state, float deltaSec) {
        if (deltaSec <= 0f || state.getLightningEffects().isEmpty()) return;
        List<LightningEffect> expired = new ArrayList<>();
        for (LightningEffect fx : state.getLightningEffects()) {
            fx.setTtlSec(fx.getTtlSec() - deltaSec);
            if (fx.getTtlSec() <= 0f) expired.add(fx);
        }
        if (!expired.isEmpty()) state.getLightningEffects().removeAll(expired);
    }

    private void updateProjectiles(GameRuntimeState state, float deltaSec) {
        if (deltaSec <= 0f || state.getActiveProjectiles().isEmpty()) {
            return;
        }
        List<ActiveProjectile> finishedProjectiles = new ArrayList<>();
        List<ActiveEnemy> killedEnemies = new ArrayList<>();
        List<String> hitEffectSpawnedFor = new ArrayList<>();

        for (ActiveProjectile projectile : state.getActiveProjectiles()) {
            ActiveEnemy target = findActiveEnemyByRuntimeId(state, projectile.getTargetEnemyRuntimeId());
            if (target == null) {
                finishedProjectiles.add(projectile);
                continue;
            }

            float dx = target.getX() - projectile.getX();
            float dy = target.getY() - projectile.getY();
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            float step = projectile.getSpeed() * deltaSec;
            if (distance <= PROJECTILE_HIT_RADIUS || step >= distance) {
                target.setCurrentHp(target.getCurrentHp() - projectile.getDamage());
                // Self-contained per-enemy visual feedback (white flash + scale pulse).
                // Lives on ActiveEnemy so EnemyShapeRenderer can read it directly
                // without coupling to the world hit-effect list.
                target.triggerHitFlash(HIT_FLASH_DURATION_SEC);
                // Spawn at most one burst per enemy per tick — multiple projectiles
                // landing the same frame (e.g. two towers) must not double the effect.
                if (!hitEffectSpawnedFor.contains(target.getRuntimeId())) {
                    state.getHitEffects().add(new HitEffect(
                        target.getX(), target.getY(), 0.28f));
                    hitEffectSpawnedFor.add(target.getRuntimeId());
                }
                finishedProjectiles.add(projectile);
                if (target.getCurrentHp() <= 0 && !killedEnemies.contains(target)) {
                    killedEnemies.add(target);
                }
                continue;
            }

            float ratio = step / distance;
            projectile.setPosition(
                projectile.getX() + dx * ratio,
                projectile.getY() + dy * ratio
            );
        }

        if (!finishedProjectiles.isEmpty()) {
            state.getActiveProjectiles().removeAll(finishedProjectiles);
        }
        if (!killedEnemies.isEmpty()) {
            for (ActiveEnemy enemy : killedEnemies) {
                awardKill(state, enemy);
            }
            state.getActiveEnemies().removeAll(killedEnemies);
        }
    }

    private void updateHitEffects(GameRuntimeState state, float deltaSec) {
        if (deltaSec <= 0f) {
            return;
        }
        // Tick per-enemy hit-flash timers — kept here (rather than inside enemy
        // movement) so all visual feedback timers advance from one place.
        for (ActiveEnemy enemy : state.getActiveEnemies()) {
            enemy.tickHitFlash(deltaSec);
        }
        if (state.getHitEffects().isEmpty()) {
            return;
        }
        List<HitEffect> expired = new ArrayList<>();
        for (HitEffect hitEffect : state.getHitEffects()) {
            hitEffect.setTtlSec(hitEffect.getTtlSec() - deltaSec);
            if (hitEffect.getTtlSec() <= 0f) {
                expired.add(hitEffect);
            }
        }
        if (!expired.isEmpty()) {
            state.getHitEffects().removeAll(expired);
        }
    }

    private ActiveEnemy pickTargetForTower(GameRuntimeState state, PlacedTower tower) {
        float rangeSq = tower.getRange() * tower.getRange();
        String lockedId = tower.getLockedTargetRuntimeId();
        if (lockedId != null) {
            ActiveEnemy locked = findActiveEnemyByRuntimeId(state, lockedId);
            if (locked != null && isEnemyInTowerRange(tower, locked, rangeSq)) {
                return locked;
            }
            tower.setLockedTargetRuntimeId(null);
        }

        ActiveEnemy chosen;
        switch (tower.getTargetingPriority()) {
            case NEAREST:
                chosen = pickNearestInRange(state.getActiveEnemies(), tower, rangeSq);
                break;
            case FIRST:
                chosen = pickFirstAlongPathInRange(state.getActiveEnemies(), state.getEnemyPath(), tower, rangeSq);
                break;
            case STRONGEST:
                chosen = pickStrongestInRange(state.getActiveEnemies(), tower, rangeSq);
                break;
            default:
                chosen = pickNearestInRange(state.getActiveEnemies(), tower, rangeSq);
                break;
        }
        if (chosen != null) {
            tower.setLockedTargetRuntimeId(chosen.getRuntimeId());
        }
        return chosen;
    }

    private static boolean isEnemyInTowerRange(PlacedTower tower, ActiveEnemy enemy, float rangeSq) {
        return TowerTargeting.distanceSquaredToTower(tower.getX(), tower.getY(), enemy) <= rangeSq;
    }

    private static ActiveEnemy pickNearestInRange(List<ActiveEnemy> enemies, PlacedTower tower, float rangeSq) {
        ActiveEnemy best = null;
        for (ActiveEnemy enemy : enemies) {
            if (!isEnemyInTowerRange(tower, enemy, rangeSq)) {
                continue;
            }
            if (best == null || TowerTargeting.compareNearest(tower.getX(), tower.getY(), enemy, best) < 0) {
                best = enemy;
            }
        }
        return best;
    }

    private static ActiveEnemy pickFirstAlongPathInRange(
        List<ActiveEnemy> enemies,
        List<Vector2> enemyPath,
        PlacedTower tower,
        float rangeSq
    ) {
        ActiveEnemy best = null;
        for (ActiveEnemy enemy : enemies) {
            if (!isEnemyInTowerRange(tower, enemy, rangeSq)) {
                continue;
            }
            if (best == null || TowerTargeting.compareFirstAlongPath(enemyPath, enemy, best) > 0) {
                best = enemy;
            }
        }
        return best;
    }

    private static ActiveEnemy pickStrongestInRange(List<ActiveEnemy> enemies, PlacedTower tower, float rangeSq) {
        ActiveEnemy best = null;
        for (ActiveEnemy enemy : enemies) {
            if (!isEnemyInTowerRange(tower, enemy, rangeSq)) {
                continue;
            }
            if (best == null || TowerTargeting.compareStrongest(enemy, best) > 0) {
                best = enemy;
            }
        }
        return best;
    }

    private static boolean advanceEnemyAlongPath(ActiveEnemy enemy, List<Vector2> enemyPath, float deltaSec) {
        float safeSpeed = Math.max(MIN_RUNTIME_ENEMY_SPEED, enemy.getSpeed());
        float remainingDistance = safeSpeed * ENEMY_SPEED_MULTIPLIER * deltaSec;
        while (remainingDistance > 0f) {
            int targetIndex = enemy.getTargetWaypointIndex();
            if (targetIndex >= enemyPath.size()) {
                return true;
            }
            Vector2 target = enemyPath.get(targetIndex);
            float dx = target.x - enemy.getX();
            float dy = target.y - enemy.getY();
            float distanceToTarget = (float) Math.sqrt(dx * dx + dy * dy);
            if (distanceToTarget <= 0.001f) {
                enemy.setPosition(target.x, target.y);
                enemy.setTargetWaypointIndex(targetIndex + 1);
                continue;
            }
            if (remainingDistance >= distanceToTarget) {
                enemy.setPosition(target.x, target.y);
                enemy.setTargetWaypointIndex(targetIndex + 1);
                remainingDistance -= distanceToTarget;
            } else {
                float ratio = remainingDistance / distanceToTarget;
                enemy.setPosition(
                    enemy.getX() + dx * ratio,
                    enemy.getY() + dy * ratio
                );
                remainingDistance = 0f;
            }
        }
        return enemy.getTargetWaypointIndex() >= enemyPath.size();
    }

    private void awardKill(GameRuntimeState state, ActiveEnemy enemy) {
        int reward = economy.calculateKillReward(enemy.getReward());
        economy.addGold(reward);
        state.addGoldEarned(reward);
        state.setLastAwardedGold(reward);
        state.getSessionStats().recordKill();
        eventBus.publish(new EnemyKilledEvent(
            enemy.getRuntimeId(),
            enemy.getEnemyId(),
            reward,
            enemy.getX(),
            enemy.getY()
        ));
    }

    private static ActiveEnemy findActiveEnemyByRuntimeId(GameRuntimeState state, String runtimeId) {
        for (ActiveEnemy enemy : state.getActiveEnemies()) {
            if (enemy.getRuntimeId().equals(runtimeId)) {
                return enemy;
            }
        }
        return null;
    }
}
