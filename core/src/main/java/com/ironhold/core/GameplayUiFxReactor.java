package com.ironhold.core;

import com.ironhold.events.BuildPlacementFailedEvent;
import com.ironhold.events.EnemyKilledEvent;
import com.ironhold.events.EventBus;
import com.ironhold.events.EventSubscription;
import com.ironhold.events.TowerBuiltEvent;
import com.ironhold.events.WaveCompletedEvent;
import com.ironhold.events.WaveStartedEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Event-driven UI/FX bridge for gameplay screen.
 */
public final class GameplayUiFxReactor {
    private static final float FLOATING_TEXT_TTL_SEC = 0.9f;
    private static final float FLOATING_TEXT_RISE_PER_SEC = 28f;
    private static final float BANNER_TTL_SEC = 1.8f;
    private static final float TOAST_TTL_SEC = 1.5f;

    private final List<EventSubscription> subscriptions = new ArrayList<>();
    private final List<FloatingTextFx> floatingTexts = new ArrayList<>();
    private final Deque<ToastUi> pendingToasts = new ArrayDeque<>();
    private BannerUi activeBanner;
    private ToastUi activeToast;

    public GameplayUiFxReactor(EventBus eventBus) {
        Objects.requireNonNull(eventBus, "eventBus");
        subscriptions.add(eventBus.subscribe(EnemyKilledEvent.class, this::onEnemyKilled));
        subscriptions.add(eventBus.subscribe(WaveStartedEvent.class, this::onWaveStarted));
        subscriptions.add(eventBus.subscribe(WaveCompletedEvent.class, this::onWaveCompleted));
        subscriptions.add(eventBus.subscribe(TowerBuiltEvent.class, this::onTowerBuilt));
        subscriptions.add(eventBus.subscribe(BuildPlacementFailedEvent.class, this::onBuildFailed));
    }

    public void update(float deltaSec) {
        float dt = Math.max(0f, deltaSec);
        updateFloatingTexts(dt);
        updateBanner(dt);
        updateToast(dt);
    }

    public List<FloatingTextView> getFloatingTextViews() {
        List<FloatingTextView> views = new ArrayList<>(floatingTexts.size());
        for (FloatingTextFx fx : floatingTexts) {
            float alpha = Math.max(0f, Math.min(1f, fx.ttlSec / FLOATING_TEXT_TTL_SEC));
            views.add(new FloatingTextView(fx.text, fx.x, fx.y, alpha));
        }
        return List.copyOf(views);
    }

    public BannerView getBannerView() {
        if (activeBanner == null) {
            return null;
        }
        float alpha = Math.max(0f, Math.min(1f, activeBanner.ttlSec / BANNER_TTL_SEC));
        return new BannerView(activeBanner.text, alpha);
    }

    public ToastView getToastView() {
        if (activeToast == null) {
            return null;
        }
        float alpha = Math.max(0f, Math.min(1f, activeToast.ttlSec / TOAST_TTL_SEC));
        return new ToastView(activeToast.text, activeToast.error, alpha);
    }

    public void clearTransientState() {
        floatingTexts.clear();
        pendingToasts.clear();
        activeToast = null;
        activeBanner = null;
    }

    public void dispose() {
        for (EventSubscription subscription : subscriptions) {
            subscription.unsubscribe();
        }
        subscriptions.clear();
        clearTransientState();
    }

    private void onEnemyKilled(EnemyKilledEvent event) {
        floatingTexts.add(new FloatingTextFx(
            "+" + event.getReward(),
            event.getWorldX() + 6f,
            event.getWorldY() + 26f,
            FLOATING_TEXT_TTL_SEC
        ));
    }

    private void onWaveStarted(WaveStartedEvent event) {
        activeBanner = new BannerUi(
            "Wave " + event.getWaveNumber() + "/" + event.getTotalWaves(),
            BANNER_TTL_SEC
        );
    }

    private void onWaveCompleted(WaveCompletedEvent event) {
        activeBanner = new BannerUi(
            "Wave " + event.getWaveNumber() + " cleared",
            BANNER_TTL_SEC
        );
    }

    private void onTowerBuilt(TowerBuiltEvent event) {
        pendingToasts.addLast(new ToastUi(
            "Built " + event.getTowerId() + " (-" + event.getCost() + "g)",
            false,
            TOAST_TTL_SEC
        ));
    }

    private void onBuildFailed(BuildPlacementFailedEvent event) {
        String message = "Build failed: " + formatReason(event.getReasonCode());
        pendingToasts.addLast(new ToastUi(message, true, TOAST_TTL_SEC));
    }

    private static String formatReason(String reasonCode) {
        if (reasonCode == null || reasonCode.isBlank()) {
            return "unknown";
        }
        return reasonCode
            .toLowerCase(Locale.ROOT)
            .replace('_', ' ');
    }

    private void updateFloatingTexts(float dt) {
        if (floatingTexts.isEmpty() || dt <= 0f) {
            return;
        }
        List<FloatingTextFx> expired = new ArrayList<>();
        for (FloatingTextFx fx : floatingTexts) {
            fx.ttlSec -= dt;
            fx.y += FLOATING_TEXT_RISE_PER_SEC * dt;
            if (fx.ttlSec <= 0f) {
                expired.add(fx);
            }
        }
        if (!expired.isEmpty()) {
            floatingTexts.removeAll(expired);
        }
    }

    private void updateBanner(float dt) {
        if (activeBanner == null || dt <= 0f) {
            return;
        }
        activeBanner.ttlSec -= dt;
        if (activeBanner.ttlSec <= 0f) {
            activeBanner = null;
        }
    }

    private void updateToast(float dt) {
        if (activeToast == null && !pendingToasts.isEmpty()) {
            activeToast = pendingToasts.removeFirst();
        }
        if (activeToast == null || dt <= 0f) {
            return;
        }
        activeToast.ttlSec -= dt;
        if (activeToast.ttlSec <= 0f) {
            activeToast = null;
            if (!pendingToasts.isEmpty()) {
                activeToast = pendingToasts.removeFirst();
            }
        }
    }

    private static final class FloatingTextFx {
        private final String text;
        private float x;
        private float y;
        private float ttlSec;

        private FloatingTextFx(String text, float x, float y, float ttlSec) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.ttlSec = ttlSec;
        }
    }

    private static final class BannerUi {
        private final String text;
        private float ttlSec;

        private BannerUi(String text, float ttlSec) {
            this.text = text;
            this.ttlSec = ttlSec;
        }
    }

    private static final class ToastUi {
        private final String text;
        private final boolean error;
        private float ttlSec;

        private ToastUi(String text, boolean error, float ttlSec) {
            this.text = text;
            this.error = error;
            this.ttlSec = ttlSec;
        }
    }

    public static final class FloatingTextView {
        private final String text;
        private final float x;
        private final float y;
        private final float alpha;

        private FloatingTextView(String text, float x, float y, float alpha) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.alpha = alpha;
        }

        public String getText() {
            return text;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getAlpha() {
            return alpha;
        }
    }

    public static final class BannerView {
        private final String text;
        private final float alpha;

        private BannerView(String text, float alpha) {
            this.text = text;
            this.alpha = alpha;
        }

        public String getText() {
            return text;
        }

        public float getAlpha() {
            return alpha;
        }
    }

    public static final class ToastView {
        private final String text;
        private final boolean error;
        private final float alpha;

        private ToastView(String text, boolean error, float alpha) {
            this.text = text;
            this.error = error;
            this.alpha = alpha;
        }

        public String getText() {
            return text;
        }

        public boolean isError() {
            return error;
        }

        public float getAlpha() {
            return alpha;
        }
    }
}
