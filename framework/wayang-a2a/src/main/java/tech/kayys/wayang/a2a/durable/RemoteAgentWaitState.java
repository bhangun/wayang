package tech.kayys.wayang.a2a.durable;

import tech.kayys.wayang.a2a.model.A2ATaskStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Represents the local execution kernel's "waiting" state while a remote A2A call
 * is in-flight (Phase 8 §41 — Remote-agent waiting state).
 *
 * <p>The local execution thread parks here; it is resumed when:
 * <ol>
 *   <li>The remote task completes (COMPLETED / FAILED / CANCELLED).</li>
 *   <li>The timeout expires — triggers a configurable timeout handler.</li>
 *   <li>{@link #cancel()} is called explicitly.</li>
 * </ol>
 *
 * <p>This is intentionally decoupled from the transport layer: the
 * {@link DurableA2ATaskLedger} calls {@link #complete} / {@link #fail} /
 * {@link #cancel} when it receives status updates.</p>
 */
public final class RemoteAgentWaitState {

    private static final Logger LOG = Logger.getLogger(RemoteAgentWaitState.class.getName());

    private final String taskId;
    private final String callerExecutionId;
    private final Duration timeout;
    private final Instant createdAt;

    private final CompletableFuture<DurableA2ATask> resultFuture = new CompletableFuture<>();

    /** Optional callback fired when the wait resolves (any outcome). */
    private Consumer<DurableA2ATask> onResolved;

    public RemoteAgentWaitState(String taskId, String callerExecutionId, Duration timeout) {
        this.taskId            = taskId;
        this.callerExecutionId = callerExecutionId;
        this.timeout           = timeout;
        this.createdAt         = Instant.now();
    }

    public static RemoteAgentWaitState of(String taskId, String callerExecutionId) {
        return new RemoteAgentWaitState(taskId, callerExecutionId, Duration.ofMinutes(10));
    }

    public static RemoteAgentWaitState of(String taskId, String callerExecutionId, Duration timeout) {
        return new RemoteAgentWaitState(taskId, callerExecutionId, timeout);
    }

    // ── Resolution triggers ───────────────────────────────────────────────────

    /** Called when the remote task finishes successfully. */
    public void complete(DurableA2ATask task) {
        LOG.fine(() -> "A2A task " + taskId + " completed for execution " + callerExecutionId);
        resolve(task.withStatus(A2ATaskStatus.COMPLETED));
    }

    /** Called when the remote task fails. */
    public void fail(DurableA2ATask task, String reason) {
        LOG.warning(() -> "A2A task " + taskId + " failed: " + reason);
        resolve(task.withStatus(A2ATaskStatus.FAILED));
    }

    /** Called to cancel the wait (and signal the remote if possible). */
    public void cancel() {
        LOG.info(() -> "A2A task " + taskId + " cancelled by local execution " + callerExecutionId);
        resultFuture.cancel(true);
    }

    // ── Blocking await ────────────────────────────────────────────────────────

    /**
     * Blocks the calling thread until the remote task resolves or the timeout expires.
     *
     * @return the resolved {@link DurableA2ATask}
     * @throws TimeoutException if the remote does not respond within {@link #timeout}
     * @throws InterruptedException if the thread is interrupted
     */
    public DurableA2ATask await() throws TimeoutException, InterruptedException {
        try {
            return resultFuture.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.ExecutionException e) {
            throw new RuntimeException("A2A task " + taskId + " failed: " + e.getCause().getMessage(), e.getCause());
        }
    }

    // ── Async callback ────────────────────────────────────────────────────────

    /** Non-blocking: registers a callback fired when the wait resolves. */
    public RemoteAgentWaitState onResolved(Consumer<DurableA2ATask> callback) {
        this.onResolved = callback;
        // Fire immediately if already resolved
        if (resultFuture.isDone() && !resultFuture.isCancelled()) {
            try { callback.accept(resultFuture.get()); }
            catch (Exception ignored) {}
        }
        return this;
    }

    // ── Introspection ─────────────────────────────────────────────────────────

    public String taskId()            { return taskId; }
    public String callerExecutionId() { return callerExecutionId; }
    public Duration timeout()         { return timeout; }
    public Instant createdAt()        { return createdAt; }
    public boolean isResolved()       { return resultFuture.isDone(); }
    public boolean isCancelled()      { return resultFuture.isCancelled(); }

    public Duration waitingFor() {
        return Duration.between(createdAt, Instant.now());
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void resolve(DurableA2ATask task) {
        resultFuture.complete(task);
        if (onResolved != null) {
            try { onResolved.accept(task); }
            catch (Exception e) {
                LOG.warning("onResolved callback threw: " + e.getMessage());
            }
        }
    }
}
