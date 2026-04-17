package com.ironhold.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * In-memory implementation of {@link EventBus}.
 */
public final class SimpleEventBus implements EventBus {

    private final Map<Class<? extends GameEvent>, List<GameEventListener<? extends GameEvent>>> listenersByType =
        new HashMap<>();

    @Override
    public <E extends GameEvent> EventSubscription subscribe(Class<E> eventType, GameEventListener<E> listener) {
        Objects.requireNonNull(eventType, "eventType");
        Objects.requireNonNull(listener, "listener");

        List<GameEventListener<? extends GameEvent>> listeners =
            listenersByType.computeIfAbsent(eventType, ignored -> new ArrayList<>());
        listeners.add(listener);

        return () -> unsubscribe(eventType, listener);
    }

    @Override
    public void publish(GameEvent event) {
        Objects.requireNonNull(event, "event");
        List<GameEventListener<? extends GameEvent>> listeners = listenersByType.get(event.getClass());
        if (listeners == null || listeners.isEmpty()) {
            return;
        }

        List<GameEventListener<? extends GameEvent>> snapshot = new ArrayList<>(listeners);
        for (GameEventListener<? extends GameEvent> listener : snapshot) {
            notifyListener(listener, event);
        }
    }

    @Override
    public void clear() {
        listenersByType.clear();
    }

    private <E extends GameEvent> void unsubscribe(Class<E> eventType, GameEventListener<E> listener) {
        List<GameEventListener<? extends GameEvent>> listeners = listenersByType.get(eventType);
        if (listeners == null) {
            return;
        }
        listeners.remove(listener);
        if (listeners.isEmpty()) {
            listenersByType.remove(eventType);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends GameEvent> void notifyListener(
        GameEventListener<? extends GameEvent> listener,
        E event
    ) {
        ((GameEventListener<E>) listener).onEvent(event);
    }
}
