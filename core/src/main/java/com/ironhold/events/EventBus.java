package com.ironhold.events;

/**
 * Minimal event bus abstraction (Observer foundation).
 */
public interface EventBus {

    <E extends GameEvent> EventSubscription subscribe(Class<E> eventType, GameEventListener<E> listener);

    void publish(GameEvent event);

    void clear();
}
