package tech.kayys.wayang.agent.backend.gamelan;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import tech.kayys.gamelan.engine.binding.WorkflowBindingContext;
import tech.kayys.gamelan.engine.binding.WorkflowBindingResolver;
import tech.kayys.gamelan.engine.error.ErrorInfo;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.sdk.executor.core.SimpleNodeExecutionResult;
import tech.kayys.gamelan.sdk.executor.core.WorkflowExecutor;
import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.agent.AgentResponse;
import tech.kayys.wayang.core.AgentDefinition;
import tech.kayys.wayang.core.DefaultWayangRuntime;
import tech.kayys.wayang.extension.Metadata;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * WorkflowExecutor implementation allowing Gamelan to invoke Wayang agents as workflow steps.
 */
@ApplicationScoped
public class WayangAgentNodeExecutor implements WorkflowExecutor {

    private static final Logger LOG = Logger.getLogger(WayangAgentNodeExecutor.class);
    public static final String EXECUTOR_TYPE = "wayang-agent";

    @Inject
    Instance<DefaultWayangRuntime> runtimeInstances;

    @Inject
    Instance<WorkflowBindingResolver> bindingResolverInstances;

    private DefaultWayangRuntime wayangRuntime;
    private WorkflowBindingResolver bindingResolver;

    public WayangAgentNodeExecutor() {
    }

    public WayangAgentNodeExecutor(DefaultWayangRuntime wayangRuntime, WorkflowBindingResolver bindingResolver) {
        this.wayangRuntime = wayangRuntime;
        this.bindingResolver = bindingResolver;
    }

    @Override
    public String getExecutorType() {
        return EXECUTOR_TYPE;
    }

    @Override
    public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
        Objects.requireNonNull(task, "task must not be null");

        String agentId = (String) task.context().get("agentId");
        if (agentId == null && task.nodeConfiguration() != null) {
            agentId = (String) task.nodeConfiguration().get("agentId");
        }
        if (agentId == null) {
            agentId = task.nodeId().value();
        }

        LOG.debugf("Executing Wayang agent step %s with agent %s", task.nodeId().value(), agentId);

        @SuppressWarnings("unchecked")
        Map<String, Object> rawParameters = (Map<String, Object>) task.context().getOrDefault("parameters", Map.of());

        WorkflowBindingResolver resolver = resolveBindingResolver();
        Map<String, Object> resolvedInputs = rawParameters;
        if (resolver != null) {
            WorkflowBindingContext bindingContext = new WorkflowBindingContext(
                    task.workflowVariables(),
                    task.workflowVariables(),
                    Map.of(),
                    task.context()
            );
            resolvedInputs = resolver.resolveMap(rawParameters, bindingContext);
        }

        String content = resolvedInputs.getOrDefault("content", "").toString();
        AgentRequest agentReq = AgentRequest.builder()
                .content(content)
                .parameters(resolvedInputs)
                .build();

        AgentDefinition agent = (AgentDefinition) task.context().get("agentDefinition");
        if (agent == null) {
            agent = AgentDefinition.builder()
                    .metadata(Metadata.builder().name(agentId).build())
                    .build();
        }

        DefaultWayangRuntime runtime = resolveRuntime();
        if (runtime == null) {
            return Uni.createFrom().item(SimpleNodeExecutionResult.failure(
                    task.runId(),
                    task.nodeId(),
                    task.attempt(),
                    new ErrorInfo("RUNTIME_UNAVAILABLE", "No WayangRuntime configured", null, Map.of()),
                    task.token()
            ));
        }

        Instant start = Instant.now();
        return Uni.createFrom().completionStage(runtime.executeAsync(agent, agentReq))
                .map(response -> {
                    Map<String, Object> output = Map.of(
                            "output", response.content() != null ? response.content() : "",
                            "success", response.success()
                    );
                    return SimpleNodeExecutionResult.success(
                            task.runId(),
                            task.nodeId(),
                            task.attempt(),
                            output,
                            task.token(),
                            Duration.between(start, Instant.now())
                    );
                })
                .onFailure().recoverWithItem(err -> {
                    LOG.errorf(err, "Failed to execute Wayang agent step %s", task.nodeId().value());
                    return SimpleNodeExecutionResult.failure(
                            task.runId(),
                            task.nodeId(),
                            task.attempt(),
                            ErrorInfo.of(err),
                            task.token()
                    );
                });
    }

    private DefaultWayangRuntime resolveRuntime() {
        if (wayangRuntime != null) {
            return wayangRuntime;
        }
        if (runtimeInstances != null && runtimeInstances.isResolvable()) {
            return runtimeInstances.get();
        }
        return null;
    }

    private WorkflowBindingResolver resolveBindingResolver() {
        if (bindingResolver != null) {
            return bindingResolver;
        }
        if (bindingResolverInstances != null && bindingResolverInstances.isResolvable()) {
            return bindingResolverInstances.get();
        }
        return null;
    }
}
