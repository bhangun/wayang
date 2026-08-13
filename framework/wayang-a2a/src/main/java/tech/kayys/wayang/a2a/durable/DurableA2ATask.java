package tech.kayys.wayang.a2a.durable;

import tech.kayys.wayang.a2a.model.A2AArtifact;
import tech.kayys.wayang.a2a.model.A2ATask;
import tech.kayys.wayang.a2a.model.A2ATaskStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Durable extension of {@link A2ATask} that ties the remote task to the local
 * Execution Kernel's checkpoint and event ledger (Phase 8 §40).
 *
 * <p>Fields added over the base record:
 * <ul>
 *   <li>{@code callerExecutionId} — the local execution that initiated the A2A call.</li>
 *   <li>{@code checkpointId}      — the checkpoint saved when A2A call was made (resume anchor).</li>
 *   <li>{@code lastEventSeq}      — last ledger sequence number written for this task.</li>
 *   <li>{@code remoteEndpoint}    — URI of the remote agent (for retry/resume).</li>
 *   <li>{@code createdAt}         — wall-clock timestamp of task creation.</li>
 *   <li>{@code completedAt}       — wall-clock timestamp of task completion (null if still running).</li>
 *   <li>{@code retryCount}        — number of times this task has been retried after failure.</li>
 * </ul>
 *
 * <p>Immutable record — mutations produce a new instance via {@link #with*} builder methods.</p>
 */
public record DurableA2ATask(
        // ── Base fields (mirrors A2ATask) ─────────────────────────────────────
        String         taskId,
        String         executionId,
        A2ATaskStatus  status,
        List<A2AArtifact> artifacts,
        Map<String, Object> metadata,

        // ── Durable fields ────────────────────────────────────────────────────
        String  callerExecutionId,
        String  checkpointId,
        long    lastEventSeq,
        String  remoteEndpoint,
        Instant createdAt,
        Instant completedAt,
        int     retryCount
) {
    public DurableA2ATask {
        artifacts = artifacts != null ? List.copyOf(artifacts) : List.of();
        metadata  = metadata  != null ? Map.copyOf(metadata)   : Map.of();
        if (createdAt == null) createdAt = Instant.now();
    }

    /** Converts to the base protocol type for transport. */
    public A2ATask toA2ATask() {
        return new A2ATask(taskId, executionId, status, artifacts, metadata);
    }

    // ── Builder-style with* methods (returns new record) ─────────────────────

    public DurableA2ATask withStatus(A2ATaskStatus newStatus) {
        Instant completed = newStatus == A2ATaskStatus.COMPLETED
                         || newStatus == A2ATaskStatus.FAILED
                         || newStatus == A2ATaskStatus.CANCELLED
                ? Instant.now() : completedAt;
        return new DurableA2ATask(taskId, executionId, newStatus, artifacts, metadata,
            callerExecutionId, checkpointId, lastEventSeq, remoteEndpoint,
            createdAt, completed, retryCount);
    }

    public DurableA2ATask withCheckpointId(String id) {
        return new DurableA2ATask(taskId, executionId, status, artifacts, metadata,
            callerExecutionId, id, lastEventSeq, remoteEndpoint,
            createdAt, completedAt, retryCount);
    }

    public DurableA2ATask withLastEventSeq(long seq) {
        return new DurableA2ATask(taskId, executionId, status, artifacts, metadata,
            callerExecutionId, checkpointId, seq, remoteEndpoint,
            createdAt, completedAt, retryCount);
    }

    public DurableA2ATask withArtifacts(List<A2AArtifact> newArtifacts) {
        return new DurableA2ATask(taskId, executionId, status, newArtifacts, metadata,
            callerExecutionId, checkpointId, lastEventSeq, remoteEndpoint,
            createdAt, completedAt, retryCount);
    }

    public DurableA2ATask incrementRetry() {
        return new DurableA2ATask(taskId, executionId, status, artifacts, metadata,
            callerExecutionId, checkpointId, lastEventSeq, remoteEndpoint,
            createdAt, completedAt, retryCount + 1);
    }

    // ── Predicates ────────────────────────────────────────────────────────────

    public boolean isTerminal() {
        return status == A2ATaskStatus.COMPLETED
            || status == A2ATaskStatus.FAILED
            || status == A2ATaskStatus.CANCELLED;
    }

    public boolean isPending() {
        return status == A2ATaskStatus.PENDING || status == A2ATaskStatus.RUNNING
            || status == A2ATaskStatus.WAITING_FOR_TOOL
            || status == A2ATaskStatus.WAITING_FOR_APPROVAL;
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    public static DurableA2ATask create(String taskId, String callerExecutionId,
                                        String remoteEndpoint, String checkpointId) {
        return new DurableA2ATask(
            taskId, callerExecutionId, A2ATaskStatus.PENDING, List.of(), Map.of(),
            callerExecutionId, checkpointId, 0L, remoteEndpoint,
            Instant.now(), null, 0
        );
    }
}
