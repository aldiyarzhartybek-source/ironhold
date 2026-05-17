package com.ironhold.game;

import com.badlogic.gdx.math.Vector2;
import com.ironhold.events.EnemyKilledEvent;
import com.ironhold.events.EventBus;
import com.ironhold.game.model.ActiveEnemy;
import com.ironhold.game.model.ActiveProjectile;
import com.ironhold.game.model.EconomyState;
import com.ironhold.game.model.HitEffect;
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
    private static final float HIT_EFFECT_TTL_SEC = 0.14f;

    private final EventBus eventBus;
    private final EconomyState economy;

    public CombatRuntimeSystem(EventBus eventBus, EconomyState economy) {
        this.eventBus = Objects.requireNonNull(eventBus, "eventBus");
        this.economy = Objects.requireNonNull(economy, "economy");
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

    private void updateTowerCombat(GameRuntimeState state, float deltaSec) {
        if (deltaSec <= 0f || state.getPlacedTowers().isEmpty() || state.getActiveEnemies().isEmpty()) {
            return;
        }
        for (PlacedTower tower : state.getPlacedTowers()) {
            float cooldown = Math.max(0f, tower.getCooldownSec() - deltaSec);
            tower.setCooldownSec(cooldown);
            if (cooldown > 0f) {
                continue;
            }
            ActiveEnemy target = pickTargetForTower(state, tower);
            if (target == null) {
                continue;
            }
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
    }

    private void updateProjectiles(GameRuntimeState state, float deltaSec) {
        if (deltaSec <= 0f || state.getActiveProjectiles().isEmpty()) {
            return;
        }
        List<ActiveProjectile> finishedProjectiles = new ArrayList<>();
        List<ActiveEnemy> killedEnemies = new ArrayList<>();

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
                state.getHitEffects().add(new HitEffect(target.getX() + 10f, target.getY() + 10f, HIT_EFFECT_TTL_SEC));
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
        if (deltaSec <= 0f || state.getHitEffects().isEmpty()) {
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
