package tech.kayys.wayang.a2a.workflow;

import tech.kayys.wayang.a2a.durable.DurableA2ATask;
import tech.kayys.wayang.a2a.durable.DurableA2ATaskLedger;
import tech.kayys.wayang.a2a.durable.RemoteAgentWaitState;
import tech.kayys.wayang.a2a.model.A2ATaskStatus;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * Connects workflow execution lifecycle to A2A task semantics (Phase 8 §43).
 *
 * <p>Decoupled from any specific {@code WorkflowEngine} implementation via three
 * functional callbacks ({@link WorkflowLifecycle}), so {@code wayang-a2a} does
 * not need a compile-time dependency on {@code wayang-workflow}.</p>
 *
 * <p>Usage:
 * <pre>{@code
 * A2AWorkflowBridge bridge = new A2AWorkflowBridge(ledger,
 *     new WorkflowLifecycle() {
 *         public void pause(String id)  { myEngine.pause(id); }
 *         public void resume(String id) { myEngine.resume(id); }
 *         public void cancel(String id) { myEngine.cancel(id); }
 *     });
 * DurableA2ATask result = bridge.executeStep("wf-123", "http://remote/agent", "do X");
 * }</pre>
 */
public class A2AWorkflowBridge {

    private static final Logger LOG = Logger.getLogger(A2AWorkflowBridge.class.getName());

    /** Decoupled workflow lifecycle callbacks. */
    public interface WorkflowLifecycle {
        void pause(String workflowExecutionId)  throws Exception;
        void resume(String workflowExecutionId) throws Exception;
        void cancel(String workflowExecutionId) throws Exception;
    }

    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(15);

    private final DurableA2ATaskLedger ledger;
    private final WorkflowLifecycle    lifecycle;

    public A2AWorkflowBridge(DurableA2ATaskLedger ledger, WorkflowLifecycle lifecycle) {
        this.ledger    = ledger;
        this.lifecycle = lifecycle;
    }

    // ── Core bridge operation ─────────────────────────────────────────────────

    /**
     * Executes one workflow step via a remote A2A call, then pauses/resumes the workflow.
     *
     * @param workflowExecutionId the running workflow instance ID
     * @param remoteEndpoint      URL of the remote agent
     * @param stepInput           message payload for the step
     * @param timeout             how long to wait for the remote agent
     * @return the resolved {@link DurableA2ATask}
     */
    public DurableA2ATask executeStep(String workflowExecutionId,
                                       String remoteEndpoint,
                                       String stepInput,
                                       Duration timeout) throws Exception {

        // 1. Create durable task anchored to this workflow execution
        DurableA2ATask task = ledger.create(workflowExecutionId, remoteEndpoint, null);
        String taskId = task.taskId();

        LOG.info(() -> "A2AWorkflowBridge: step workflow=" + workflowExecutionId
                      + " task=" + taskId + " remote=" + remoteEndpoint);

        // 2. Pause the workflow
        try { lifecycle.pause(workflowExecutionId); }
        catch (Exception e) {
            LOG.warning("Could not pause workflow " + workflowExecutionId + ": " + e.getMessage());
        }

        // 3. Transition to RUNNING and park on wait state
        ledger.update(taskId, A2ATaskStatus.RUNNING);
        RemoteAgentWaitState ws = ledger.createWaitState(taskId, timeout);

        DurableA2ATask resolved;
        try {
            resolved = ws.await();
        } catch (TimeoutException te) {
            LOG.warning("A2A step timed out: workflow=" + workflowExecutionId + " task=" + taskId);
            ledger.fail(taskId, "timeout");
            safeCancelWorkflow(workflowExecutionId);
            throw te;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            ledger.cancel(taskId);
            safeCancelWorkflow(workflowExecutionId);
            throw ie;
        }

        // 4. Resume or cancel based on outcome
        switch (resolved.status()) {
            case COMPLETED -> {
                LOG.info(() -> "A2A step complete — resuming workflow " + workflowExecutionId);
                safeResumeWorkflow(workflowExecutionId);
            }
            case FAILED, CANCELLED -> {
                LOG.warning("A2A step " + resolved.status() + " — cancelling workflow " + workflowExecutionId);
                safeCancelWorkflow(workflowExecutionId);
            }
            default -> LOG.warning("Unexpected terminal status: " + resolved.status());
        }

        return resolved;
    }

    /** Convenience overload using the default 15-minute timeout. */
    public DurableA2ATask executeStep(String workflowExecutionId,
                                       String remoteEndpoint,
                                       String stepInput) throws Exception {
        return executeStep(workflowExecutionId, remoteEndpoint, stepInput, DEFAULT_TIMEOUT);
    }

    // ── Cancellation propagation ──────────────────────────────────────────────

    /** Cancels all pending A2A tasks belonging to this workflow execution. */
    public void cancelAllForWorkflow(String workflowExecutionId) {
        ledger.byCallerExecution(workflowExecutionId)
            .stream()
            .filter(DurableA2ATask::isPending)
            .forEach(t -> {
                LOG.info(() -> "Cascade cancel to A2A task " + t.taskId());
                ledger.cancel(t.taskId());
            });
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void safeResumeWorkflow(String id) {
        try { lifecycle.resume(id); }
        catch (Exception e) { LOG.warning("Failed to resume workflow " + id + ": " + e.getMessage()); }
    }

    private void safeCancelWorkflow(String id) {
        try { lifecycle.cancel(id); }
        catch (Exception e) { LOG.warning("Failed to cancel workflow " + id + ": " + e.getMessage()); }
    }
}
