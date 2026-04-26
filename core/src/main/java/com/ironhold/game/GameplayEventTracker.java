package com.ironhold.game;

import com.ironhold.events.EnemyKilledEvent;
import com.ironhold.events.EnemySpawnedEvent;
import com.ironhold.events.EventBus;
import com.ironhold.events.EventSubscription;
import com.ironhold.events.TowerBuiltEvent;
import com.ironhold.events.WaveCompletedEvent;
import com.ironhold.events.WaveStartedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Runtime event subscriber used by gameplay systems and HUD diagnostics.
 */
public final class GameplayEventTracker {

    private final List<EventSubscription> subscriptions;
    private int enemySpawnedEvents;
    private int enemyKilledEvents;
    private int towerBuiltEvents;
    private int waveStartedEvents;
    private int waveCompletedEvents;

    public GameplayEventTracker(EventBus eventBus) {
        Objects.requireNonNull(eventBus, "eventBus");
        this.subscriptions = new ArrayList<>();
        this.subscriptions.add(eventBus.subscribe(EnemySpawnedEvent.class, this::onEnemySpawned));
        this.subscriptions.add(eventBus.subscribe(EnemyKilledEvent.class, this::onEnemyKilled));
        this.subscriptions.add(eventBus.subscribe(TowerBuiltEvent.class, this::onTowerBuilt));
        this.subscriptions.add(eventBus.subscribe(WaveStartedEvent.class, this::onWaveStarted));
        this.subscriptions.add(eventBus.subscribe(WaveCompletedEvent.class, this::onWaveCompleted));
    }

    public void dispose() {
        for (EventSubscription subscription : subscriptions) {
            subscription.unsubscribe();
        }
        subscriptions.clear();
    }

    public int getEnemySpawnedEvents() {
        return enemySpawnedEvents;
    }

    public int getEnemyKilledEvents() {
        return enemyKilledEvents;
    }

    public int getTowerBuiltEvents() {
        return towerBuiltEvents;
    }

    public int getWaveStartedEvents() {
        return waveStartedEvents;
    }

    public int getWaveCompletedEvents() {
        return waveCompletedEvents;
    }

    private void onEnemySpawned(EnemySpawnedEvent event) {
        enemySpawnedEvents++;
    }

    private void onEnemyKilled(EnemyKilledEvent event) {
        enemyKilledEvents++;
    }

    private void onTowerBuilt(TowerBuiltEvent event) {
        towerBuiltEvents++;
    }

    private void onWaveStarted(WaveStartedEvent event) {
        waveStartedEvents++;
    }

    private void onWaveCompleted(WaveCompletedEvent event) {
        waveCompletedEvents++;
    }
}
