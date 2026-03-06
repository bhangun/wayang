package tech.kayys.wayang.runtime.standalone.executor;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.engine.protocol.CommunicationType;
import tech.kayys.gamelan.sdk.executor.core.AbstractWorkflowExecutor;
import tech.kayys.gamelan.sdk.executor.core.Executor;
import tech.kayys.gamelan.sdk.executor.core.SimpleNodeExecutionResult;
import tech.kayys.wayang.runtime.standalone.service.TriggerIntegrationService;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Local trigger source executor for standalone runtime.
 * This allows UI trigger source nodes to execute and emit a deterministic payload
 * even when no external provider (email, kafka, webhook listener) is wired yet.
 */
@ApplicationScoped
@Executor(
        executorType = "trigger-source-executor",
        communicationType = CommunicationType.LOCAL,
        maxConcurrentTasks = 200,
        supportedNodeTypes = {
                "start",
                "trigger-manual",
                "trigger-schedule",
                "trigger-email",
                "trigger-telegram",
                "trigger-websocket",
                "trigger-webhook",
                "trigger-event",
                "trigger-kafka",
                "trigger-file"
        },
        version = "1.0.0",
        description = "Standalone source trigger executor for UI trigger nodes")
public class TriggerSourceExecutor extends AbstractWorkflowExecutor {
    private final TriggerIntegrationService integrationService;

    public TriggerSourceExecutor() {
        this.integrationService = new TriggerIntegrationService();
    }

    @Inject
    public TriggerSourceExecutor(Instance<TriggerIntegrationService> integrationServiceInstance) {
        this.integrationService = integrationServiceInstance != null && integrationServiceInstance.isResolvable()
                ? integrationServiceInstance.get()
                : new TriggerIntegrationService();
    }

    @Override
    public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
        Map<String, Object> context = task.context() != null
                ? new LinkedHashMap<>(task.context())
                : new LinkedHashMap<>();

        String triggerType = String.valueOf(context.getOrDefault("__node_type__", task.nodeId().value()));
        context.putIfAbsent("triggerType", triggerType);
        context.put("triggeredAt", Instant.now().toString());
        context.putIfAbsent("triggerNodeId", task.nodeId().value());
        context.putIfAbsent("triggerStatus", "fired");
        context.put("triggerIntegration",
                integrationService.enrich(triggerType, task.nodeId().value(), context));

        return Uni.createFrom().item(SimpleNodeExecutionResult.success(
                task.runId(),
                task.nodeId(),
                task.attempt(),
                context,
                task.token(),
                Duration.ZERO));
    }
}
