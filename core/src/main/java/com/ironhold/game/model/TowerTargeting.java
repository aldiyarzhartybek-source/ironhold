package com.ironhold.game.model;

import com.badlogic.gdx.math.Vector2;

import java.util.List;

/**
 * Pure helpers for tower targeting strategies (no game loop state).
 */
public final class TowerTargeting {

    private TowerTargeting() {
    }

    public static float distanceSquaredToTower(float towerX, float towerY, ActiveEnemy enemy) {
        float dx = enemy.getX() - towerX;
        float dy = enemy.getY() - towerY;
        return dx * dx + dy * dy;
    }

    /**
     * Squared distance from enemy position to its next path waypoint ({@link ActiveEnemy#getTargetWaypointIndex()}).
     */
    public static float distanceSquaredToNextWaypoint(List<Vector2> path, ActiveEnemy enemy) {
        int wi = enemy.getTargetWaypointIndex();
        if (wi < 0 || wi >= path.size()) {
            return 0f;
        }
        Vector2 wp = path.get(wi);
        float dx = wp.x - enemy.getX();
        float dy = wp.y - enemy.getY();
        return dx * dx + dy * dy;
    }

    /**
     * Lexicographic ordering for {@link TowerTargetingPriority#FIRST}: further along path wins;
     * same waypoint index → closer to next waypoint along segment wins; then stable id tie-break.
     */
    public static int compareFirstAlongPath(List<Vector2> path, ActiveEnemy a, ActiveEnemy b) {
        int wiA = a.getTargetWaypointIndex();
        int wiB = b.getTargetWaypointIndex();
        if (wiA != wiB) {
            return Integer.compare(wiA, wiB);
        }
        float da = distanceSquaredToNextWaypoint(path, a);
        float db = distanceSquaredToNextWaypoint(path, b);
        int cmpDist = Float.compare(da, db);
        if (cmpDist != 0) {
            return -cmpDist;
        }
        return a.getRuntimeId().compareTo(b.getRuntimeId());
    }

    public static int compareStrongest(ActiveEnemy a, ActiveEnemy b) {
        int cmpHp = Integer.compare(a.getCurrentHp(), b.getCurrentHp());
        if (cmpHp != 0) {
            return cmpHp;
        }
        return a.getRuntimeId().compareTo(b.getRuntimeId());
    }

    public static int compareNearest(float towerX, float towerY, ActiveEnemy a, ActiveEnemy b) {
        float da = distanceSquaredToTower(towerX, towerY, a);
        float db = distanceSquaredToTower(towerX, towerY, b);
        int cmp = Float.compare(da, db);
        if (cmp != 0) {
            return cmp;
        }
        return a.getRuntimeId().compareTo(b.getRuntimeId());
    }
}
