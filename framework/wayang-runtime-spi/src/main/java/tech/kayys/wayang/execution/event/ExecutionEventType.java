package tech.kayys.wayang.execution.event;

/**
 * Canonical event types emitted by the Execution Kernel.
 * Grouped into lifecycle, context, model, tool, memory, and system events.
 *
 * <p>The distinction per the architecture proposal:
 * <ul>
 *   <li><b>Event</b> — "What happened?" (durable, append-only ledger)</li>
 *   <li><b>Checkpoint</b> — "Where can I resume?"</li>
 *   <li><b>Cache</b> — "What work can I reuse?"</li>
 *   <li><b>Artifact</b> — "What did execution produce?"</li>
 * </ul>
 */
public enum ExecutionEventType {

    // ── Lifecycle ──────────────────────────────────────────────────────────────
    EXECUTION_STARTED,
    EXECUTION_PAUSED,
    EXECUTION_RESUMED,
    EXECUTION_COMPLETED,
    EXECUTION_FAILED,
    EXECUTION_CANCELLED,

    // ── Context & Model ────────────────────────────────────────────────────────
    CONTEXT_COMPILED,
    MODEL_REQUESTED,
    MODEL_RESPONSE_RECEIVED,
    MODEL_ROUTING_RESOLVED,

    // ── Tool ───────────────────────────────────────────────────────────────────
    TOOL_REQUESTED,
    TOOL_APPROVAL_REQUIRED,
    TOOL_APPROVED,
    TOOL_DENIED,
    TOOL_EXECUTED,
    TOOL_CACHE_HIT,
    TOOL_FAILED,
    TOOL_TIMEOUT,
    TOOL_RETRY,

    // ── Memory ─────────────────────────────────────────────────────────────────
    MEMORY_RETRIEVED,
    MEMORY_STORED,

    // ── Checkpoint / Artifact ──────────────────────────────────────────────────
    CHECKPOINT_CREATED,
    ARTIFACT_CREATED,

    // ── Multi-Agent (A2A) ──────────────────────────────────────────────────────
    A2A_REQUESTED,
    A2A_COMPLETED,
    A2A_FAILED,

    // ── Cache ──────────────────────────────────────────────────────────────────
    CACHE_HIT,
    CACHE_MISS,
    CACHE_EVICTED
}
