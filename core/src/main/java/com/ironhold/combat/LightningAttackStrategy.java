package com.ironhold.combat;

import com.badlogic.gdx.math.MathUtils;
import com.ironhold.game.GameRuntimeState;
import com.ironhold.game.model.ActiveEnemy;
import com.ironhold.game.model.HitEffect;
import com.ironhold.game.model.LightningEffect;
import com.ironhold.game.model.PlacedTower;

import java.util.ArrayList;
import java.util.List;

/**
 * Lightning tower — мгновенный урон по цепочке до 3 врагов.
 * primary (от pickTargetForTower) используется как якорь цепи.
 */
public final class LightningAttackStrategy implements AttackStrategy {

    private static final int MAX_CHAIN = 3;
    private static final float HIT_FLASH_SEC = 0.18f;
    private static final float LIGHTNING_FLASH_SEC = 0.08f;
    private static final float LIGHTNING_ZAG_AMP = 14f;

    @Override
    public void fire(PlacedTower tower, ActiveEnemy primary, GameRuntimeState state) {
        List<ActiveEnemy> chain = buildChain(primary, state.getActiveEnemies(), MAX_CHAIN);

        for (ActiveEnemy target : chain) {
            target.setCurrentHp(target.getCurrentHp() - tower.getDamage());
            target.triggerHitFlash(HIT_FLASH_SEC);
            state.getHitEffects().add(new HitEffect(target.getX(), target.getY(), 0.22f));
        }

        CombatKillHelper.awardAndRemoveDead(state, chain);
        state.getLightningEffects().add(buildLightningEffect(tower, chain));
    }

    private static List<ActiveEnemy> buildChain(ActiveEnemy anchor, List<ActiveEnemy> all, int maxLen) {
        List<ActiveEnemy> chain = new ArrayList<>();
        if (anchor == null || maxLen <= 0) {
            return chain;
        }
        chain.add(anchor);
        List<ActiveEnemy> remaining = new ArrayList<>(all);
        remaining.remove(anchor);

        float cx = anchor.getX();
        float cy = anchor.getY();

        for (int step = 1; step < maxLen && !remaining.isEmpty(); step++) {
            ActiveEnemy nearest = null;
            float bestSq = Float.MAX_VALUE;
            for (ActiveEnemy enemy : remaining) {
                float dx = cx - enemy.getX();
                float dy = cy - enemy.getY();
                float dsq = dx * dx + dy * dy;
                if (dsq < bestSq) {
                    bestSq = dsq;
                    nearest = enemy;
                }
            }
            if (nearest == null) {
                break;
            }
            chain.add(nearest);
            remaining.remove(nearest);
            cx = nearest.getX();
            cy = nearest.getY();
        }
        return chain;
    }

    private static LightningEffect buildLightningEffect(PlacedTower tower, List<ActiveEnemy> chain) {
        int segCount = chain.size();
        float[] wp = new float[(1 + segCount * 2) * 2];
        int idx = 0;

        float prevX = tower.getX();
        float prevY = tower.getY();
        wp[idx++] = prevX;
        wp[idx++] = prevY;

        for (int i = 0; i < segCount; i++) {
            float nextX = chain.get(i).getX();
            float nextY = chain.get(i).getY();

            float mx = (prevX + nextX) * 0.5f;
            float my = (prevY + nextY) * 0.5f;
            float dx = nextX - prevX;
            float dy = nextY - prevY;
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            float sign = (i % 2 == 0) ? 1f : -1f;
            if (len > 0.001f) {
                float perpX = -dy / len;
                float perpY = dx / len;
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
}
