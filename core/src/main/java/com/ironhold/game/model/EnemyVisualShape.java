package com.ironhold.game.model;

import java.util.Locale;

/**
 * Geometric silhouette for enemy rendering (Stage 5).
 */
public enum EnemyVisualShape {
    TRIANGLE,
    SQUARE,
    PENTAGON,
    HEXAGON;

    public static EnemyVisualShape fromConfig(String raw) {
        if (raw == null || raw.isBlank()) {
            return SQUARE;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        try {
            return EnemyVisualShape.valueOf(normalized);
        } catch (IllegalArgumentException ignored) {
            return SQUARE;
        }
    }
}
