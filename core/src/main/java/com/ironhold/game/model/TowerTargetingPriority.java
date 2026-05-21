package com.ironhold.game.model;

import com.badlogic.gdx.math.Vector2;

import java.util.List;
import java.util.Locale;

/**
 * How a tower picks enemies inside attack range.
 */
public enum TowerTargetingPriority {

    /** Closest to the tower (Euclidean). */
    NEAREST,

    /** Furthest along the enemy path toward the base. */
    FIRST,

    /** Highest current HP among enemies in range. */
    STRONGEST;

    /**
     * Parses config strings ({@code nearest}, {@code first}, {@code strongest}). Unknown values fall back to {@link #NEAREST}.
     */
    public static TowerTargetingPriority fromConfig(String raw) {
        TowerTargetingPriority parsed = tryParse(raw);
        return parsed != null ? parsed : NEAREST;
    }

    static TowerTargetingPriority tryParse(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        if ("nearest".equals(normalized)) {
            return NEAREST;
        }
        if ("first".equals(normalized)) {
            return FIRST;
        }
        if ("strongest".equals(normalized)) {
            return STRONGEST;
        }
        return null;
    }

    public String getUiLabel() {
        switch (this) {
            case NEAREST:
                return "near";
            case FIRST:
                return "first";
            case STRONGEST:
                return "strong";
            default:
                return name().toLowerCase(Locale.ROOT);
        }
    }

    /**
     * Picks the best enemy in range for this strategy.
     */
    public ActiveEnemy pickInRange(
        List<ActiveEnemy> enemies,
        PlacedTower tower,
        List<Vector2> enemyPath,
        float rangeSq
    ) {
        switch (this) {
            case NEAREST:
                return pickNearest(enemies, tower, rangeSq);
            case FIRST:
                return pickFirst(enemies, enemyPath, tower, rangeSq);
            case STRONGEST:
                return pickStrongest(enemies, tower, rangeSq);
            default:
                throw new IllegalStateException("Unhandled targeting: " + this);
        }
    }

    private static ActiveEnemy pickNearest(List<ActiveEnemy> enemies, PlacedTower tower, float rangeSq) {
        ActiveEnemy best = null;
        for (ActiveEnemy enemy : enemies) {
            if (!inRange(tower, enemy, rangeSq)) {
                continue;
            }
            if (best == null || TowerTargeting.compareNearest(tower.getX(), tower.getY(), enemy, best) < 0) {
                best = enemy;
            }
        }
        return best;
    }

    private static ActiveEnemy pickFirst(
        List<ActiveEnemy> enemies,
        List<Vector2> enemyPath,
        PlacedTower tower,
        float rangeSq
    ) {
        ActiveEnemy best = null;
        for (ActiveEnemy enemy : enemies) {
            if (!inRange(tower, enemy, rangeSq)) {
                continue;
            }
            if (best == null || TowerTargeting.compareFirstAlongPath(enemyPath, enemy, best) > 0) {
                best = enemy;
            }
        }
        return best;
    }

    private static ActiveEnemy pickStrongest(List<ActiveEnemy> enemies, PlacedTower tower, float rangeSq) {
        ActiveEnemy best = null;
        for (ActiveEnemy enemy : enemies) {
            if (!inRange(tower, enemy, rangeSq)) {
                continue;
            }
            if (best == null || TowerTargeting.compareStrongest(enemy, best) > 0) {
                best = enemy;
            }
        }
        return best;
    }

    private static boolean inRange(PlacedTower tower, ActiveEnemy enemy, float rangeSq) {
        return TowerTargeting.distanceSquaredToTower(tower.getX(), tower.getY(), enemy) <= rangeSq;
    }
}
