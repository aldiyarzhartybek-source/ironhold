package com.ironhold.combat;

import com.ironhold.game.GameRuntimeState;
import com.ironhold.game.model.ActiveEnemy;
import com.ironhold.game.model.PlacedTower;

/**
 * Strategy pattern: инкапсулирует логику одного выстрела башни.
 *
 * Вызывается ТОЛЬКО когда кулдаун == 0 и цель уже выбрана общим
 * пайплайном таргетинга. Стратегия отвечает за:
 *   — добавление снарядов / эффектов в state
 *   — нанесение урона (для мгновенных атак)
 *   — цепочку / AOE (lightning, mortar, flame)
 *
 * Намеренно НЕ отвечает за кулдаун и первичный выбор цели —
 * это зона CombatRuntimeSystem (общий пайплайн).
 */
public interface AttackStrategy {

    /**
     * @param tower   стреляющая башня (позиция, урон, дальность)
     * @param primary первичная цель, выбранная pickTargetForTower()
     * @param state   изменяемое состояние игры
     */
    void fire(PlacedTower tower, ActiveEnemy primary, GameRuntimeState state);
}
