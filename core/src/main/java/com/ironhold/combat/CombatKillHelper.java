package com.ironhold.combat;

import com.ironhold.events.EnemyKilledEvent;
import com.ironhold.events.EventBus;
import com.ironhold.game.GameRuntimeState;
import com.ironhold.game.model.ActiveEnemy;
import com.ironhold.game.model.EconomyState;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared kill rewards for instant-damage attack strategies (e.g. lightning).
 */
public final class CombatKillHelper {

    private static EventBus eventBus;
    private static EconomyState economy;

    private CombatKillHelper() {
    }

    public static void install(EventBus bus, EconomyState econ) {
        eventBus = bus;
        economy = econ;
    }

    public static void awardKill(GameRuntimeState state, ActiveEnemy enemy) {
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

    public static void awardAndRemoveDead(GameRuntimeState state, Iterable<ActiveEnemy> victims) {
        List<ActiveEnemy> killed = new ArrayList<>();
        for (ActiveEnemy enemy : victims) {
            if (enemy.getCurrentHp() <= 0 && !killed.contains(enemy)) {
                killed.add(enemy);
            }
        }
        for (ActiveEnemy enemy : killed) {
            awardKill(state, enemy);
        }
        state.getActiveEnemies().removeAll(killed);
    }
}
