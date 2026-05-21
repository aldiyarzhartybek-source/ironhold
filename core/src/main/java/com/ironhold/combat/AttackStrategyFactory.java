package com.ironhold.combat;

/**
 * Factory Method: создаёт нужную AttackStrategy по towerId.
 * Единственное место, где towerId влияет на поведение атаки.
 */
public final class AttackStrategyFactory {

    private AttackStrategyFactory() {
    }

    public static AttackStrategy create(String towerId) {
        if (towerId == null) {
            return new ProjectileAttackStrategy();
        }
        switch (towerId) {
            case "lightning_tower":
                return new LightningAttackStrategy();
            case "mortar_tower":
                return new MortarAttackStrategy();
            case "flamethrower_tower":
                return new FlamethrowerAttackStrategy();
            default:
                return new ProjectileAttackStrategy();
        }
    }
}
