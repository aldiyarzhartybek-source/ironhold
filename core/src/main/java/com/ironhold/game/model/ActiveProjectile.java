package com.ironhold.game.model;

/**
 * Runtime projectile emitted by a tower.
 */
public final class ActiveProjectile {

    private final String runtimeId;
    private final String targetEnemyRuntimeId;
    private final int damage;
    private float x;
    private float y;
    private final float speed;
    private final ProjectileKind kind;
    private final float landingX;
    private final float landingY;
    private final float splashRadius;

    private float prevX;
    private float prevY;

    public static ActiveProjectile beam(
        String runtimeId,
        String targetEnemyRuntimeId,
        int damage,
        float x,
        float y,
        float speed
    ) {
        return new ActiveProjectile(
            runtimeId,
            targetEnemyRuntimeId,
            damage,
            x,
            y,
            speed,
            ProjectileKind.BEAM,
            0f,
            0f,
            0f
        );
    }

    public static ActiveProjectile mortarShell(
        String runtimeId,
        int damage,
        float x,
        float y,
        float landingX,
        float landingY,
        float splashRadius,
        float speed
    ) {
        return new ActiveProjectile(
            runtimeId,
            "",
            damage,
            x,
            y,
            speed,
            ProjectileKind.MORTAR_SHELL,
            landingX,
            landingY,
            splashRadius
        );
    }

    private ActiveProjectile(
        String runtimeId,
        String targetEnemyRuntimeId,
        int damage,
        float x,
        float y,
        float speed,
        ProjectileKind kind,
        float landingX,
        float landingY,
        float splashRadius
    ) {
        this.runtimeId = runtimeId;
        this.targetEnemyRuntimeId = targetEnemyRuntimeId;
        this.damage = damage;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.kind = kind;
        this.landingX = landingX;
        this.landingY = landingY;
        this.splashRadius = splashRadius;
        this.prevX = x;
        this.prevY = y;
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public String getTargetEnemyRuntimeId() {
        return targetEnemyRuntimeId;
    }

    public int getDamage() {
        return damage;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getSpeed() {
        return speed;
    }

    public ProjectileKind getKind() {
        return kind;
    }

    public float getLandingX() {
        return landingX;
    }

    public float getLandingY() {
        return landingY;
    }

    public float getSplashRadius() {
        return splashRadius;
    }

    public float getPrevX() {
        return prevX;
    }

    public float getPrevY() {
        return prevY;
    }

    public void setPosition(float x, float y) {
        this.prevX = this.x;
        this.prevY = this.y;
        this.x = x;
        this.y = y;
    }
}
