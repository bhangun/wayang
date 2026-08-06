package tech.kayys.wayang.execution;

/**
 * Event listener.
 */
@FunctionalInterface
public interface EventListener {

    /**
     * Handles an event.
     */
    void onEvent(Event event);
}