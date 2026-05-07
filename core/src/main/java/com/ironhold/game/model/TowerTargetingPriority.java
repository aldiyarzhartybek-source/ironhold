package com.ironhold.game.model;

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
}
