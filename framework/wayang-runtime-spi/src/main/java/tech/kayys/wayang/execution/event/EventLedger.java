package tech.kayys.wayang.execution.event;

import java.util.List;
import java.util.Optional;

/**
 * Durable, append-only event ledger for the Execution Kernel.
 *
 * <p>Distinct from callbacks (ephemeral), checkpoints (resume points),
 * cache (reusable work), and artifacts (produced outputs). The ledger answers
 * the single question: <b>"What happened during this execution?"</b></p>
 *
 * <p>Implementations may store events in-memory (for tests/dev), to a
 * relational or time-series DB, or to a distributed stream (Kafka, etc.)
 * without changing the SPI contract.</p>
 */
public interface EventLedger {

    /**
     * Appends a new event to the ledger and updates the associated metrics.
     *
     * @param event The event to record. Must not be null.
     */
    void record(ExecutionEvent event);

    /**
     * Returns all events recorded for the given execution, ordered by sequence.
     *
     * @param executionId The execution to query.
     * @return Ordered, immutable list of events; empty if none found.
     */
    List<ExecutionEvent> events(String executionId);

    /**
     * Returns all events of a specific type for the given execution.
     *
     * @param executionId The execution to query.
     * @param type        The event type to filter on.
     */
    List<ExecutionEvent> events(String executionId, ExecutionEventType type);

    /**
     * Returns the live metrics snapshot for the given execution.
     * Metrics are updated atomically as events are recorded.
     */
    Optional<ExecutionMetrics> metrics(String executionId);

    /**
     * Purges all events and metrics for the given execution.
     * Useful for test isolation or after long-term archival.
     *
     * @param executionId The execution to purge.
     */
    void purge(String executionId);

    /**
     * Returns the total number of events stored across all executions.
     */
    long totalEventCount();
}
