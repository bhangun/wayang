package tech.kayys.wayang.engine.gamelan;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.gamelan.sdk.client.GamelanClient;
import tech.kayys.wayang.workflow.kernel.WorkflowRunManager;
import tech.kayys.wayang.workflow.kernel.WorkflowRunId;
import tech.kayys.wayang.workflow.kernel.NodeExecutionResult;
import tech.kayys.wayang.workflow.kernel.Signal;
import tech.kayys.wayang.workflow.kernel.WorkflowRunSnapshot;
import tech.kayys.wayang.workflow.kernel.ExecutionHistory;
import tech.kayys.wayang.workflow.kernel.WorkflowRunState;
import tech.kayys.wayang.workflow.kernel.ExecutionToken;
import tech.kayys.wayang.workflow.kernel.ExternalSignal;
import tech.kayys.wayang.workflow.kernel.CallbackRegistration;
import tech.kayys.wayang.workflow.kernel.CallbackConfig;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Gamelan-backed implementation of Wayang WorkflowRunManager
 */
@ApplicationScoped
public class GamelanWorkflowRunManager implements WorkflowRunManager {

    private final GamelanWorkflowEngine engine;

    @Inject
    public GamelanWorkflowRunManager(GamelanWorkflowEngine engine) {
        this.engine = engine;
    }

    @Override
    public Uni<tech.kayys.wayang.workflow.domain.WorkflowRun> createRun(
            tech.kayys.wayang.workflow.api.dto.CreateRunRequest request, String tenantId) {

        return engine.startRun(request.getWorkflowId())
                .map(response -> {
                    // Map Gamelan RunResponse to Wayang WorkflowRun
                    // This is a placeholder as the domain models might differ
                    return null;
                });
    }

    @Override
    public Uni<tech.kayys.wayang.workflow.domain.WorkflowRun> getRun(String runId) {
        // Delegate to Gamelan via engine/client
        return Uni.createFrom()
                .failure(new UnsupportedOperationException("Gamelan getRun not implemented in Wayang yet"));
    }

    @Override
    public Uni<tech.kayys.wayang.workflow.domain.WorkflowRun> startRun(String runId, String tenantId) {
        return engine.startRun(runId)
                .map(response -> null);
    }

    @Override
    public Uni<tech.kayys.wayang.workflow.domain.WorkflowRun> suspendRun(String runId, String tenantId, String reason,
            String humanTaskId) {
        // Gamelan doesn't have a direct "suspend" in SDK yet, but we can model it
        return Uni.createFrom().failure(new UnsupportedOperationException("Gamelan suspendRun not implemented yet"));
    }

    @Override
    public Uni<tech.kayys.wayang.workflow.domain.WorkflowRun> resumeRun(String runId, String tenantId,
            String humanTaskId, Map<String, Object> resumeData) {
        // Use Gamelan SDK resume (which we updated earlier to accept humanTaskId)
        // return
        // engine.client().runs().resume(runId).humanTaskId(humanTaskId).data(resumeData).execute()
        return Uni.createFrom()
                .failure(new UnsupportedOperationException("Gamelan resumeRun needs direct client access"));
    }

    @Override
    public Uni<Void> cancelRun(String runId, String tenantId, String reason) {
        // return engine.client().runs().cancel(runId).execute();
        return Uni.createFrom().voidItem();
    }

    @Override
    public Uni<tech.kayys.wayang.workflow.domain.WorkflowRun> completeRun(String runId, String tenantId,
            Map<String, Object> outputs) {
        return Uni.createFrom().failure(new UnsupportedOperationException());
    }

    @Override
    public Uni<tech.kayys.wayang.workflow.domain.WorkflowRun> failRun(String runId, String tenantId,
            tech.kayys.wayang.workflow.api.dto.ErrorResponse error) {
        return Uni.createFrom().failure(new UnsupportedOperationException());
    }

    @Override
    public Uni<java.util.List<tech.kayys.wayang.workflow.api.dto.RunResponse>> queryRuns(String tenantId,
            String workflowId, tech.kayys.wayang.workflow.api.model.RunStatus status, int page, int size) {
        return Uni.createFrom().item(java.util.List.of());
    }

    @Override
    public Uni<Long> getActiveRunsCount(String tenantId) {
        return Uni.createFrom().item(0L);
    }

    @Override
    public Uni<Void> handleNodeResult(WorkflowRunId runId, NodeExecutionResult result) {
        return Uni.createFrom().voidItem();
    }

    @Override
    public Uni<Void> signal(WorkflowRunId runId, Signal signal) {
        return Uni.createFrom().voidItem();
    }

    @Override
    public Uni<WorkflowRunSnapshot> getSnapshot(WorkflowRunId runId) {
        return Uni.createFrom().failure(new UnsupportedOperationException());
    }

    @Override
    public Uni<ExecutionHistory> getExecutionHistory(WorkflowRunId runId) {
        return Uni.createFrom().failure(new UnsupportedOperationException());
    }

    @Override
    public Uni<tech.kayys.wayang.sdk.util.WorkflowValidator.ValidationResult> validateTransition(WorkflowRunId runId,
            WorkflowRunState targetState) {
        return Uni.createFrom().item(tech.kayys.wayang.sdk.util.WorkflowValidator.ValidationResult.success());
    }

    @Override
    public Uni<Void> onNodeExecutionCompleted(NodeExecutionResult result, String executorSignature) {
        return Uni.createFrom().voidItem();
    }

    @Override
    public Uni<Void> onExternalSignal(WorkflowRunId runId, ExternalSignal signal, String callbackToken) {
        return Uni.createFrom().voidItem();
    }

    @Override
    public Uni<CallbackRegistration> registerCallback(WorkflowRunId runId, String nodeId, CallbackConfig config) {
        return Uni.createFrom().failure(new UnsupportedOperationException());
    }

    @Override
    public Uni<ExecutionToken> createExecutionToken(WorkflowRunId runId, String nodeId, int attempt) {
        return Uni.createFrom().failure(new UnsupportedOperationException());
    }
}
