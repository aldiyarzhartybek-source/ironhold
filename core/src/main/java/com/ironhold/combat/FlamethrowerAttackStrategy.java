package com.ironhold.combat;

import com.ironhold.game.GameRuntimeState;
import com.ironhold.game.model.ActiveEnemy;
import com.ironhold.game.model.FlameConeEffect;
import com.ironhold.game.model.PlacedTower;

/** Flamethrower — конус AOE; урон наносится в updateFlameCones(). */
public final class FlamethrowerAttackStrategy implements AttackStrategy {

    private static final float FLAME_CONE_DURATION_SEC = 0.32f;
    private static final float FLAME_CONE_HALF_ANGLE_RAD = 0.26f;
    private static final float FLAME_CONE_RANGE_MULT = 0.72f;

    @Override
    public void fire(PlacedTower tower, ActiveEnemy primary, GameRuntimeState state) {
        float dx = primary.getX() - tower.getX();
        float dy = primary.getY() - tower.getY();
        float aim = (float) Math.atan2(dy, dx);
        float coneRange = tower.getRange() * FLAME_CONE_RANGE_MULT;
        state.getFlameConeEffects().add(new FlameConeEffect(
            tower.getX(),
            tower.getY(),
            aim,
            coneRange,
            FLAME_CONE_HALF_ANGLE_RAD,
            tower.getDamage(),
            FLAME_CONE_DURATION_SEC
        ));
    }
}
