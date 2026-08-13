package tech.kayys.wayang.execution.event;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Mutable, thread-safe telemetry accumulator for a single execution.
 *
 * <p>Answers questions like:
 * <ul>
 *   <li>"Why did this agent take 45 seconds?"</li>
 *   <li>"Why did this execution cost so much?"</li>
 * </ul>
 *
 * <p>Populated by the {@link EventLedger} subscriber inside the Execution Kernel;
 * accessible after (or during) execution via {@link EventLedger#metrics(String)}.
 */
public final class ExecutionMetrics {

    private final String executionId;
    private volatile long startedAt   = 0;
    private volatile long completedAt = 0;

    private final AtomicLong modelCalls        = new AtomicLong();
    private final AtomicLong inputTokens       = new AtomicLong();
    private final AtomicLong outputTokens      = new AtomicLong();
    private final AtomicLong toolCalls         = new AtomicLong();
    private final AtomicLong toolCacheHits     = new AtomicLong();
    private final AtomicLong toolCacheMisses   = new AtomicLong();
    private final AtomicLong retries           = new AtomicLong();
    private final AtomicLong memoryRetrievals  = new AtomicLong();
    private final AtomicLong checkpointCount   = new AtomicLong();
    private final AtomicLong errors            = new AtomicLong();
    private final AtomicLong a2aRequests       = new AtomicLong();

    public ExecutionMetrics(String executionId) {
        this.executionId = executionId;
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────────
    public void markStarted()    { this.startedAt   = System.currentTimeMillis(); }
    public void markCompleted()  { this.completedAt = System.currentTimeMillis(); }

    // ── Counters ───────────────────────────────────────────────────────────────
    public void recordModelCall()                         { modelCalls.incrementAndGet(); }
    public void recordTokens(long input, long output)    { inputTokens.addAndGet(input); outputTokens.addAndGet(output); }
    public void recordToolCall()                          { toolCalls.incrementAndGet(); }
    public void recordCacheHit()                          { toolCacheHits.incrementAndGet(); }
    public void recordCacheMiss()                         { toolCacheMisses.incrementAndGet(); }
    public void recordRetry()                             { retries.incrementAndGet(); }
    public void recordMemoryRetrieval()                   { memoryRetrievals.incrementAndGet(); }
    public void recordCheckpoint()                        { checkpointCount.incrementAndGet(); }
    public void recordError()                             { errors.incrementAndGet(); }
    public void recordA2ARequest()                        { a2aRequests.incrementAndGet(); }

    // ── Reads ──────────────────────────────────────────────────────────────────
    public String executionId()        { return executionId; }
    public Duration latency()          { return (completedAt > startedAt) ? Duration.ofMillis(completedAt - startedAt) : Duration.ZERO; }
    public long modelCalls()           { return modelCalls.get(); }
    public long inputTokens()          { return inputTokens.get(); }
    public long outputTokens()         { return outputTokens.get(); }
    public long toolCalls()            { return toolCalls.get(); }
    public long cacheHits()            { return toolCacheHits.get(); }
    public long cacheMisses()          { return toolCacheMisses.get(); }
    public long retries()              { return retries.get(); }
    public long memoryRetrievals()     { return memoryRetrievals.get(); }
    public long checkpointCount()      { return checkpointCount.get(); }
    public long errors()               { return errors.get(); }
    public long a2aRequests()          { return a2aRequests.get(); }

    @Override
    public String toString() {
        return String.format(
            "ExecutionMetrics{id=%s, latency=%s, modelCalls=%d, tokens=[in=%d,out=%d], " +
            "toolCalls=%d, cacheHits=%d, cacheMisses=%d, retries=%d, errors=%d}",
            executionId, latency(), modelCalls.get(), inputTokens.get(), outputTokens.get(),
            toolCalls.get(), toolCacheHits.get(), toolCacheMisses.get(), retries.get(), errors.get()
        );
    }
}
