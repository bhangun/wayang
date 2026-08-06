package tech.kayys.wayang.execution;

/**
 * Event bus for publishing and subscribing to events.
 */
public interface EventBus {

    /**
     * Publishes an event.
     */
    void publish(Event event);

    /**
     * Subscribes to all events.
     */
    void subscribe(EventListener listener);

    /**
     * Subscribes to specific event types.
     */
    void subscribe(EventListener listener, Class<?>... eventTypes);

    /**
     * Subscribes with a filter.
     */
    void subscribe(EventListener listener, java.util.function.Predicate<Event> filter);

    /**
     * Unsubscribes a listener.
     */
    void unsubscribe(EventListener listener);

    /**
     * Returns event bus statistics.
     */
    EventBusStatistics getStatistics();

    /**
     * Clears all subscribers.
     */
    void clear();
}
