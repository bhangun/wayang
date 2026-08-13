package tech.kayys.wayang.execution.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * A durable, immutable record of something that happened during an agent execution.
 *
 * <p>Unlike ephemeral listener callbacks (onToolCallStart, onDone, etc.), events
 * are persisted in the {@link EventLedger} and can be replayed or audited.</p>
 *
 * @param eventId       Unique ID for this event.
 * @param executionId   The execution this event belongs to.
 * @param sequence      Monotonic sequence number within the execution.
 * @param timestamp     When the event occurred.
 * @param type          The canonical event type.
 * @param actor         Who/what emitted the event (e.g. "executor", "model", tool name).
 * @param payload       Structured event payload (type-specific key/value pairs).
 * @param correlationId Optional correlation to a parent event or external request.
 */
public record ExecutionEvent(
        String eventId,
        String executionId,
        long   sequence,
        Instant timestamp,
        ExecutionEventType type,
        String actor,
        Map<String, Object> payload,
        String correlationId
) {

    /** Convenience factory for a typical in-process event. */
    public static ExecutionEvent of(
            String executionId,
            long sequence,
            ExecutionEventType type,
            String actor,
            Map<String, Object> payload
    ) {
        return new ExecutionEvent(
                UUID.randomUUID().toString(),
                executionId,
                sequence,
                Instant.now(),
                type,
                actor,
                payload != null ? Map.copyOf(payload) : Map.of(),
                null
        );
    }

    /** Factory including a correlation ID (e.g. for A2A or chained executions). */
    public static ExecutionEvent ofCorrelated(
            String executionId,
            long sequence,
            ExecutionEventType type,
            String actor,
            Map<String, Object> payload,
            String correlationId
    ) {
        return new ExecutionEvent(
                UUID.randomUUID().toString(),
                executionId,
                sequence,
                Instant.now(),
                type,
                actor,
                payload != null ? Map.copyOf(payload) : Map.of(),
                correlationId
        );
    }
}
