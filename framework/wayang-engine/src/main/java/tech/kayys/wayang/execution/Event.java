package tech.kayys.wayang.execution;

/**
 * Base event class.
 */
public interface Event {

    /**
     * Returns the event type.
     */
    String getType();

    /**
     * Returns the event timestamp.
     */
    long getTimestamp();

    /**
     * Returns the event source.
     */
    Object getSource();

    /**
     * Returns the correlation ID.
     */
    String getCorrelationId();
}