package tech.kayys.wayang.a2a.durable;

import tech.kayys.wayang.a2a.model.A2ATaskStatus;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Application-scoped registry of durable A2A tasks (Phase 8 §40-42).
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Create and track {@link DurableA2ATask} instances keyed by taskId.</li>
 *   <li>Maintain {@link RemoteAgentWaitState} instances so the local execution kernel
 *       can park while the remote agent works.</li>
 *   <li>Provide status-update entry points called by the transport layer
 *       ({@link #update}, {@link #complete}, {@link #fail}, {@link #cancel}).</li>
 *   <li>Resume suspended wait states when a terminal status arrives.</li>
 * </ul>
 *
 * <p>This class does NOT depend on CDI so it can be used standalone or in tests.
 * In a CDI deployment, wrap it with {@code @ApplicationScoped} in a producer.</p>
 */
public class DurableA2ATaskLedger {

    private static final Logger LOG = Logger.getLogger(DurableA2ATaskLedger.class.getName());

    /** taskId → task */
    private final Map<String, DurableA2ATask> tasks = new ConcurrentHashMap<>();

    /** taskId → wait state (only present while task is in-flight) */
    private final Map<String, RemoteAgentWaitState> waitStates = new ConcurrentHashMap<>();

    // ── Task lifecycle ────────────────────────────────────────────────────────

    /**
     * Creates a new durable task and registers it.
     *
     * @param callerExecutionId  local execution ID that initiated the call
     * @param remoteEndpoint     URI of the remote agent
     * @param checkpointId       checkpoint saved before the call (resume anchor)
     * @return the newly created task
     */
    public DurableA2ATask create(String callerExecutionId, String remoteEndpoint, String checkpointId) {
        String taskId = UUID.randomUUID().toString();
        DurableA2ATask task = DurableA2ATask.create(taskId, callerExecutionId, remoteEndpoint, checkpointId);
        tasks.put(taskId, task);
        LOG.info(() -> "DurableA2A task created: " + taskId + " for exec=" + callerExecutionId);
        return task;
    }

    /**
     * Creates a wait state for the given task and returns it.
     * The caller blocks on {@link RemoteAgentWaitState#await()} until the remote resolves.
     */
    public RemoteAgentWaitState createWaitState(String taskId, Duration timeout) {
        DurableA2ATask task = tasks.get(taskId);
        if (task == null) throw new IllegalArgumentException("Unknown taskId: " + taskId);

        RemoteAgentWaitState ws = RemoteAgentWaitState.of(taskId, task.callerExecutionId(), timeout);
        waitStates.put(taskId, ws);
        LOG.fine(() -> "Wait state created for task " + taskId);
        return ws;
    }

    /** Convenience overload with default 10-minute timeout. */
    public RemoteAgentWaitState createWaitState(String taskId) {
        return createWaitState(taskId, Duration.ofMinutes(10));
    }

    // ── Status updates (called by transport / polling) ────────────────────────

    /** Transitions a task to a new status. Fires resolution if terminal. */
    public DurableA2ATask update(String taskId, A2ATaskStatus newStatus) {
        DurableA2ATask updated = tasks.compute(taskId, (k, existing) -> {
            if (existing == null) {
                LOG.warning("update called for unknown task: " + taskId);
                return null;
            }
            return existing.withStatus(newStatus);
        });

        if (updated == null) return null;

        LOG.fine(() -> "Task " + taskId + " → " + newStatus);

        if (updated.isTerminal()) {
            resolveWaitState(taskId, updated);
        }

        return updated;
    }

    /** Marks a task COMPLETED and fires the wait state. */
    public DurableA2ATask complete(String taskId) {
        return update(taskId, A2ATaskStatus.COMPLETED);
    }

    /** Marks a task FAILED and fires the wait state with a reason. */
    public DurableA2ATask fail(String taskId, String reason) {
        DurableA2ATask updated = update(taskId, A2ATaskStatus.FAILED);
        RemoteAgentWaitState ws = waitStates.get(taskId);
        if (ws != null && updated != null) {
            ws.fail(updated, reason);
            waitStates.remove(taskId);
        }
        return updated;
    }

    /** Cancels a task and its wait state. */
    public DurableA2ATask cancel(String taskId) {
        DurableA2ATask updated = update(taskId, A2ATaskStatus.CANCELLED);
        RemoteAgentWaitState ws = waitStates.remove(taskId);
        if (ws != null) ws.cancel();
        return updated;
    }

    /** Increments the retry counter and resets to PENDING. */
    public DurableA2ATask retry(String taskId) {
        return tasks.compute(taskId, (k, existing) ->
            existing == null ? null
                : existing.incrementRetry().withStatus(A2ATaskStatus.PENDING));
    }

    // ── Checkpoint / resume (§42) ─────────────────────────────────────────────

    /** Updates the checkpoint anchor for a task. */
    public void updateCheckpoint(String taskId, String checkpointId) {
        tasks.computeIfPresent(taskId, (k, t) -> t.withCheckpointId(checkpointId));
    }

    /** Updates the last ledger sequence number for distributed observability. */
    public void updateEventSeq(String taskId, long seq) {
        tasks.computeIfPresent(taskId, (k, t) -> t.withLastEventSeq(seq));
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public Optional<DurableA2ATask> find(String taskId) {
        return Optional.ofNullable(tasks.get(taskId));
    }

    public List<DurableA2ATask> allPending() {
        List<DurableA2ATask> result = new ArrayList<>();
        tasks.values().stream().filter(DurableA2ATask::isPending).forEach(result::add);
        return result;
    }

    public List<DurableA2ATask> byCallerExecution(String callerExecutionId) {
        List<DurableA2ATask> result = new ArrayList<>();
        tasks.values().stream()
            .filter(t -> callerExecutionId.equals(t.callerExecutionId()))
            .forEach(result::add);
        return result;
    }

    public int taskCount()     { return tasks.size(); }
    public int waitingCount()  { return waitStates.size(); }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void resolveWaitState(String taskId, DurableA2ATask task) {
        RemoteAgentWaitState ws = waitStates.remove(taskId);
        if (ws == null) return;
        switch (task.status()) {
            case COMPLETED -> ws.complete(task);
            case FAILED    -> ws.fail(task, "Remote agent reported FAILED");
            case CANCELLED -> ws.cancel();
            default        -> {} // no-op for non-terminal
        }
    }
}
