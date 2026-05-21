package com.ironhold.combat;

import com.ironhold.game.GameRuntimeState;
import com.ironhold.game.model.ActiveEnemy;
import com.ironhold.game.model.ActiveProjectile;
import com.ironhold.game.model.PlacedTower;

/** Mortar — медленный снаряд + splash при падении. */
public final class MortarAttackStrategy implements AttackStrategy {

    private static final float MORTAR_SHELL_SPEED = 95f;
    private static final float MORTAR_SPLASH_RADIUS = 52f;

    @Override
    public void fire(PlacedTower tower, ActiveEnemy primary, GameRuntimeState state) {
        state.getActiveProjectiles().add(ActiveProjectile.mortarShell(
            "mortar-" + state.getNextProjectileInstanceId(),
            tower.getDamage(),
            tower.getX(),
            tower.getY(),
            primary.getX(),
            primary.getY(),
            MORTAR_SPLASH_RADIUS,
            MORTAR_SHELL_SPEED
        ));
    }
}
