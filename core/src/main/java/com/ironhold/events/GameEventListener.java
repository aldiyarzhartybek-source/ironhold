package com.ironhold.events;

/**
 * Observer callback for a concrete {@link GameEvent} type.
 */
@FunctionalInterface
public interface GameEventListener<E extends GameEvent> {

    void onEvent(E event);
}
