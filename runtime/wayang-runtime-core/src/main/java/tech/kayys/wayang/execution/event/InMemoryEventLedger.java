package tech.kayys.wayang.execution.event;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Application-scoped, thread-safe in-memory implementation of {@link EventLedger}.
 *
 * <p>Events are stored per-execution in a {@link CopyOnWriteArrayList} for
 * lock-free reads. Metrics are maintained as a live {@link ExecutionMetrics} object
 * updated on every {@link #record(ExecutionEvent)} call.</p>
 *
 * <p>Suitable for development, testing, and single-node production deployments.
 * For distributed/durable event ledgers, replace with a Kafka, NATS, or DB-backed
 * implementation without changing the SPI contract.</p>
 */
@ApplicationScoped
public class InMemoryEventLedger implements EventLedger {

    private static final Logger LOGGER = Logger.getLogger(InMemoryEventLedger.class.getName());

    /** executionId → ordered list of events */
    private final Map<String, CopyOnWriteArrayList<ExecutionEvent>> store = new ConcurrentHashMap<>();

    /** executionId → live metrics */
    private final Map<String, ExecutionMetrics> metricsStore = new ConcurrentHashMap<>();

    /** Global event counter (sequence guarantee within a single JVM). */
    private final AtomicLong globalSeq = new AtomicLong();

    // -------------------------------------------------------------------------

    @Override
    public void record(ExecutionEvent event) {
        if (event == null) return;

        store.computeIfAbsent(event.executionId(), k -> new CopyOnWriteArrayList<>())
             .add(event);

        updateMetrics(event);

        LOGGER.fine(() -> String.format("[%s] EVENT %-35s actor=%-20s seq=%d",
                event.executionId(), event.type(), event.actor(), event.sequence()));
    }

    @Override
    public List<ExecutionEvent> events(String executionId) {
        List<ExecutionEvent> raw = store.get(executionId);
        if (raw == null) return List.of();
        // Return a snapshot sorted by sequence number
        return raw.stream()
                  .sorted((a, b) -> Long.compare(a.sequence(), b.sequence()))
                  .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<ExecutionEvent> events(String executionId, ExecutionEventType type) {
        return events(executionId).stream()
                                  .filter(e -> e.type() == type)
                                  .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Optional<ExecutionMetrics> metrics(String executionId) {
        return Optional.ofNullable(metricsStore.get(executionId));
    }

    @Override
    public void purge(String executionId) {
        store.remove(executionId);
        metricsStore.remove(executionId);
    }

    @Override
    public long totalEventCount() {
        return store.values().stream().mapToLong(List::size).sum();
    }

    // -------------------------------------------------------------------------
    // Metrics update

    private void updateMetrics(ExecutionEvent event) {
        ExecutionMetrics m = metricsStore.computeIfAbsent(
                event.executionId(), ExecutionMetrics::new);

        switch (event.type()) {
            case EXECUTION_STARTED       -> m.markStarted();
            case EXECUTION_COMPLETED,
                 EXECUTION_CANCELLED     -> m.markCompleted();
            case EXECUTION_FAILED        -> { m.markCompleted(); m.recordError(); }
            case MODEL_REQUESTED         -> m.recordModelCall();
            case MODEL_RESPONSE_RECEIVED -> {
                Object in  = event.payload().get("inputTokens");
                Object out = event.payload().get("outputTokens");
                m.recordTokens(
                    in  instanceof Number n ? n.longValue() : 0L,
                    out instanceof Number n ? n.longValue() : 0L
                );
            }
            case TOOL_EXECUTED           -> m.recordToolCall();
            case TOOL_CACHE_HIT,
                 CACHE_HIT               -> m.recordCacheHit();
            case CACHE_MISS              -> m.recordCacheMiss();
            case TOOL_RETRY              -> m.recordRetry();
            case TOOL_FAILED             -> m.recordError();
            case MEMORY_RETRIEVED        -> m.recordMemoryRetrieval();
            case CHECKPOINT_CREATED      -> m.recordCheckpoint();
            case A2A_REQUESTED           -> m.recordA2ARequest();
            default                      -> { /* no counter for this type */ }
        }
    }
}
