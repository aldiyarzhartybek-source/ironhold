package com.ironhold.game;

import com.badlogic.gdx.math.Vector2;
import com.ironhold.combat.CombatKillHelper;
import com.ironhold.events.EventBus;
import com.ironhold.game.model.ActiveEnemy;
import com.ironhold.game.model.ActiveProjectile;
import com.ironhold.game.model.EconomyState;
import com.ironhold.game.model.HitEffect;
import com.ironhold.game.model.LightningEffect;
import com.ironhold.game.model.FlameConeEffect;
import com.ironhold.game.model.MortarExplosionEffect;
import com.ironhold.game.model.PlacedTower;
import com.ironhold.game.model.ProjectileKind;
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
    private static final float PROJECTILE_HIT_RADIUS = 12f;
    private static final float MORTAR_SHELL_HIT_RADIUS = 14f;
    private static final float MORTAR_EXPLOSION_TTL_SEC = 0.28f;

    public CombatRuntimeSystem(EventBus eventBus, EconomyState economy) {
        CombatKillHelper.install(
            Objects.requireNonNull(eventBus, "eventBus"),
            Objects.requireNonNull(economy, "economy")
        );
    }

    public boolean debugDefeatFirstEnemy(GameRuntimeState state) {
        if (state.getActiveEnemies().isEmpty()) {
            return false;
        }
        ActiveEnemy defeated = state.getActiveEnemies().remove(0);
        CombatKillHelper.awardKill(state, defeated);
        return true;
    }

    public void update(GameRuntimeState state, float deltaSec) {
        float safeDeltaSec = Math.max(0f, deltaSec);
        updateEnemyMovement(state, safeDeltaSec);
        updateProjectiles(state, safeDeltaSec);
        updateTowerCombat(state, safeDeltaSec);
        updateHitEffects(state, safeDeltaSec);
        updateLightningEffects(state, safeDeltaSec);
        updateMortarExplosions(state, safeDeltaSec);
        updateFlameCones(state, safeDeltaSec);
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

        for (PlacedTower tower : state.getPlacedTowers()) {
            tower.tickPulse(deltaSec);

            float cooldown = Math.max(0f, tower.getCooldownSec() - deltaSec);
            tower.setCooldownSec(cooldown);
            if (cooldown > 0f) {
                continue;
            }

            ActiveEnemy target = pickTargetForTower(state, tower);
            if (target == null) {
                continue;
            }

            tower.getAttackStrategy().fire(tower, target, state);
            tower.setCooldownSec(tower.getFireRateSec());
        }
    }

    private void updateMortarShell(
        GameRuntimeState state,
        ActiveProjectile shell,
        float deltaSec,
        List<ActiveProjectile> finishedProjectiles,
        List<ActiveEnemy> killedEnemies
    ) {
        float dx = shell.getLandingX() - shell.getX();
        float dy = shell.getLandingY() - shell.getY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float step = shell.getSpeed() * deltaSec;
        if (distance <= MORTAR_SHELL_HIT_RADIUS || step >= distance) {
            detonateMortar(state, shell, killedEnemies);
            finishedProjectiles.add(shell);
            return;
        }
        float ratio = step / distance;
        shell.setPosition(
            shell.getX() + dx * ratio,
            shell.getY() + dy * ratio
        );
    }

    private void detonateMortar(
        GameRuntimeState state,
        ActiveProjectile shell,
        List<ActiveEnemy> killedEnemies
    ) {
        float lx = shell.getLandingX();
        float ly = shell.getLandingY();
        float radius = shell.getSplashRadius();
        float radiusSq = radius * radius;

        state.getMortarExplosions().add(new MortarExplosionEffect(
            lx, ly, radius, MORTAR_EXPLOSION_TTL_SEC));

        for (ActiveEnemy enemy : state.getActiveEnemies()) {
            float edx = enemy.getX() - lx;
            float edy = enemy.getY() - ly;
            if (edx * edx + edy * edy > radiusSq) {
                continue;
            }
            enemy.setCurrentHp(enemy.getCurrentHp() - shell.getDamage());
            enemy.triggerHitFlash(HIT_FLASH_DURATION_SEC);
            if (enemy.getCurrentHp() <= 0 && !killedEnemies.contains(enemy)) {
                killedEnemies.add(enemy);
            }
        }
    }

    private void updateFlameCones(GameRuntimeState state, float deltaSec) {
        if (deltaSec <= 0f || state.getFlameConeEffects().isEmpty()) return;

        List<FlameConeEffect> expired = new ArrayList<>();
        List<ActiveEnemy> killed = new ArrayList<>();

        for (FlameConeEffect cone : state.getFlameConeEffects()) {
            cone.setTtlSec(cone.getTtlSec() - deltaSec);
            float reach = cone.getReachDistance();
            for (ActiveEnemy enemy : state.getActiveEnemies()) {
                if (cone.hasDamaged(enemy.getRuntimeId())) {
                    continue;
                }
                if (!isEnemyInFlameCone(enemy, cone, reach)) {
                    continue;
                }
                enemy.setCurrentHp(enemy.getCurrentHp() - cone.getDamage());
                enemy.triggerHitFlash(HIT_FLASH_DURATION_SEC);
                cone.markDamaged(enemy.getRuntimeId());
                if (enemy.getCurrentHp() <= 0 && !killed.contains(enemy)) {
                    killed.add(enemy);
                }
            }
            if (cone.getTtlSec() <= 0f) {
                expired.add(cone);
            }
        }

        if (!expired.isEmpty()) {
            state.getFlameConeEffects().removeAll(expired);
        }
        if (!killed.isEmpty()) {
            for (ActiveEnemy enemy : killed) {
                CombatKillHelper.awardKill(state, enemy);
            }
            state.getActiveEnemies().removeAll(killed);
        }
    }

    private static boolean isEnemyInFlameCone(ActiveEnemy enemy, FlameConeEffect cone, float reach) {
        if (reach <= 0.5f) return false;
        float dx = enemy.getX() - cone.getOriginX();
        float dy = enemy.getY() - cone.getOriginY();
        float cos = (float) Math.cos(cone.getAimAngleRad());
        float sin = (float) Math.sin(cone.getAimAngleRad());
        float along = dx * cos + dy * sin;
        if (along < 0f || along > reach) {
            return false;
        }
        float perp = Math.abs(-dx * sin + dy * cos);
        return perp <= along * (float) Math.tan(cone.getHalfAngleRad());
    }

    private void updateMortarExplosions(GameRuntimeState state, float deltaSec) {
        if (deltaSec <= 0f || state.getMortarExplosions().isEmpty()) return;
        List<MortarExplosionEffect> expired = new ArrayList<>();
        for (MortarExplosionEffect fx : state.getMortarExplosions()) {
            fx.setTtlSec(fx.getTtlSec() - deltaSec);
            if (fx.getTtlSec() <= 0f) expired.add(fx);
        }
        if (!expired.isEmpty()) state.getMortarExplosions().removeAll(expired);
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
            if (projectile.getKind() == ProjectileKind.MORTAR_SHELL) {
                updateMortarShell(state, projectile, deltaSec, finishedProjectiles, killedEnemies);
                continue;
            }

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
                CombatKillHelper.awardKill(state, enemy);
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

        ActiveEnemy chosen = tower.getTargetingPriority().pickInRange(
            state.getActiveEnemies(),
            tower,
            state.getEnemyPath(),
            rangeSq
        );
        if (chosen != null) {
            tower.setLockedTargetRuntimeId(chosen.getRuntimeId());
        }
        return chosen;
    }

    private static boolean isEnemyInTowerRange(PlacedTower tower, ActiveEnemy enemy, float rangeSq) {
        return TowerTargeting.distanceSquaredToTower(tower.getX(), tower.getY(), enemy) <= rangeSq;
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

    private static ActiveEnemy findActiveEnemyByRuntimeId(GameRuntimeState state, String runtimeId) {
        for (ActiveEnemy enemy : state.getActiveEnemies()) {
            if (enemy.getRuntimeId().equals(runtimeId)) {
                return enemy;
            }
        }
        return null;
    }
}
