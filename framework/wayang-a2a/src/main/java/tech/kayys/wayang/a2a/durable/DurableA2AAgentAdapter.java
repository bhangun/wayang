package tech.kayys.wayang.a2a.durable;

import tech.kayys.wayang.a2a.adapter.A2AMessageMapper;
import tech.kayys.wayang.a2a.api.A2AServer;
import tech.kayys.wayang.a2a.model.A2AMessage;
import tech.kayys.wayang.a2a.model.A2ATask;
import tech.kayys.wayang.a2a.model.A2ATaskStatus;
import tech.kayys.wayang.a2a.model.AgentCard;
import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.agent.AgentResponse;
import tech.kayys.wayang.core.AgentDefinition;
import tech.kayys.wayang.core.runtime.WayangRuntime;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Durable replacement for {@link tech.kayys.wayang.a2a.adapter.A2AAgentAdapter}
 * (Phase 8 §40 — Durable A2A executions).
 *
 * <p>Differences from the non-durable adapter:
 * <ol>
 *   <li>Each task is stored in the {@link DurableA2ATaskLedger} — survives JVM restart
 *       when the ledger is backed by a persistent store.</li>
 *   <li>{@link #sendMessage} returns immediately with a PENDING task; execution runs async.</li>
 *   <li>When execution completes/fails, the ledger task is updated — resuming any parked
 *       {@link RemoteAgentWaitState}.</li>
 *   <li>{@link #getTask} reads directly from the ledger — no in-memory map needed.</li>
 *   <li>{@link #cancelTask} transitions the task to CANCELLED and fires the wait state.</li>
 * </ol>
 */
public class DurableA2AAgentAdapter implements A2AServer {

    private static final Logger LOG = Logger.getLogger(DurableA2AAgentAdapter.class.getName());

    private final WayangRuntime        runtime;
    private final AgentDefinition      agentDefinition;
    private final DurableA2ATaskLedger ledger;

    /** Remote endpoint URI of this adapter (used as the "remoteEndpoint" in the ledger). */
    private final String localEndpoint;

    public DurableA2AAgentAdapter(WayangRuntime runtime,
                                   AgentDefinition agentDefinition,
                                   DurableA2ATaskLedger ledger,
                                   String localEndpoint) {
        this.runtime         = runtime;
        this.agentDefinition = agentDefinition;
        this.ledger          = ledger;
        this.localEndpoint   = localEndpoint;
    }

    // ── A2AServer ─────────────────────────────────────────────────────────────

    @Override
    public AgentCard getAgentCard() {
        java.net.URI endpoint = null;
        try { if (localEndpoint != null) endpoint = java.net.URI.create(localEndpoint); }
        catch (IllegalArgumentException ignored) {}
        return new AgentCard(
            agentDefinition.id().asString(),
            agentDefinition.metadata().name(),
            agentDefinition.metadata().description(),
            java.util.List.of(), java.util.List.of(), java.util.List.of(),
            java.util.Map.of(), endpoint, java.util.Map.of()
        );
    }

    /**
     * Accepts the message, creates a durable task, starts async execution, returns PENDING.
     */
    @Override
    public CompletionStage<A2ATask> sendMessage(A2AMessage message) {
        AgentRequest request = A2AMessageMapper.toAgentRequest(message);

        // Create durable task — no checkpoint yet (caller may save one separately)
        DurableA2ATask durable = ledger.create(
            agentDefinition.id().asString(),
            localEndpoint,
            null // checkpointId will be set by the runtime on first checkpoint
        );

        String taskId = durable.taskId();

        // Transition to RUNNING
        ledger.update(taskId, A2ATaskStatus.RUNNING);

        // Execute asynchronously — result drives ledger updates
        CompletableFuture<AgentResponse> execution = runtime.executeAsync(agentDefinition, request);

        execution.whenComplete((response, error) -> {
            if (error != null) {
                LOG.log(Level.WARNING, "Durable A2A task " + taskId + " failed", error);
                ledger.fail(taskId, error.getMessage());
            } else {
                LOG.fine(() -> "Durable A2A task " + taskId + " completed");
                ledger.complete(taskId);
            }
        });

        return CompletableFuture.completedFuture(durable.withStatus(A2ATaskStatus.RUNNING).toA2ATask());
    }

    /**
     * Reads status from the durable ledger rather than an in-memory map.
     */
    @Override
    public CompletionStage<A2ATask> getTask(String taskId) {
        return ledger.find(taskId)
            .map(t -> CompletableFuture.<A2ATask>completedFuture(t.toA2ATask()))
            .orElseGet(() -> CompletableFuture.failedFuture(
                new IllegalArgumentException("Task not found: " + taskId)));
    }

    /**
     * Cancels a task — stops the wait state and transitions status to CANCELLED.
     */
    @Override
    public CompletionStage<Void> cancelTask(String taskId) {
        ledger.cancel(taskId);
        return CompletableFuture.completedFuture(null);
    }

    // ── Resume support (§42) ──────────────────────────────────────────────────

    /**
     * Parks the calling thread until the remote task resolves.
     * Used by an orchestrating execution that fired the A2A call and wants to wait.
     *
     * @param taskId  the task to wait on
     * @param timeout how long to wait before raising TimeoutException
     * @return the resolved durable task
     */
    public DurableA2ATask awaitCompletion(String taskId, Duration timeout)
            throws java.util.concurrent.TimeoutException, InterruptedException {
        RemoteAgentWaitState ws = ledger.createWaitState(taskId, timeout);
        return ws.await();
    }
}
