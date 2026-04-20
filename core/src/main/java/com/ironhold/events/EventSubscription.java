package com.ironhold.events;

/**
 * Handle that allows unsubscribing a listener.
 */
@FunctionalInterface
public interface EventSubscription {

    void unsubscribe();
}
