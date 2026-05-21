package com.ironhold.combat;

import com.ironhold.game.GameRuntimeState;
import com.ironhold.game.model.ActiveEnemy;
import com.ironhold.game.model.ActiveProjectile;
import com.ironhold.game.model.PlacedTower;

/** Dart / basic_tower — быстрый луч к одной цели. */
public final class ProjectileAttackStrategy implements AttackStrategy {

    private static final float PROJECTILE_SPEED = 320f;

    @Override
    public void fire(PlacedTower tower, ActiveEnemy primary, GameRuntimeState state) {
        state.getActiveProjectiles().add(ActiveProjectile.beam(
            "projectile-" + state.getNextProjectileInstanceId(),
            primary.getRuntimeId(),
            tower.getDamage(),
            tower.getX(),
            tower.getY(),
            PROJECTILE_SPEED
        ));
    }
}
