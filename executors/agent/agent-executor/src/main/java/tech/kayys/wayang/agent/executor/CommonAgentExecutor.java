package tech.kayys.wayang.agent.executor;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.engine.protocol.CommunicationType;
import tech.kayys.gamelan.sdk.executor.core.Executor;
import tech.kayys.wayang.agent.type.AgentType;
import tech.kayys.wayang.agent.type.CommonAgent;

import java.util.Map;
import java.util.Set;

/**
 * Executor for CommonAgent - handles general-purpose task execution
 */
@Executor(executorType = "common-agent", communicationType = CommunicationType.GRPC, maxConcurrentTasks = 10, supportedNodeTypes = {
        "agent-task", "common-agent-task" })
@ApplicationScoped
public class CommonAgentExecutor extends AbstractAgentExecutor {

    @Override
    public String getExecutorType() {
        return "common-agent";
    }

    @Override
    protected AgentType getAgentType() {
        // Extract from task context or use default
        return new CommonAgent(
                "general-purpose",
                Set.of("generic-task"));
    }

    @Override
    protected Uni<NodeExecutionResult> doExecute(NodeExecutionTask task) {
        logger.info("CommonAgentExecutor executing task: {}", task.nodeId());

        Map<String, Object> context = task.context();

        // Extract agent configuration from context
        String specialization = (String) context.getOrDefault("specialization", "general-purpose");
        String taskType = (String) context.getOrDefault("taskType", "generic");
        Map<String, Object> parameters = (Map<String, Object>) context.getOrDefault("parameters", Map.of());

        // Execute agent logic based on specialization and task type
        return executeAgentTask(specialization, taskType, parameters, task)
                .map(result -> createSuccessResult(task, result))
                .onFailure().recoverWithItem(error -> createFailureResult(task, error));
    }

    /**
     * Execute the actual agent task based on specialization
     */
    private Uni<Map<String, Object>> executeAgentTask(
            String specialization,
            String taskType,
            Map<String, Object> parameters,
            NodeExecutionTask task) {

        return Uni.createFrom().item(() -> {
            logger.debug("Executing {} task with specialization: {}", taskType, specialization);

            // Agent execution logic here
            // This is where you would implement the actual agent behavior
            // For now, returning a simple result

            return Map.of(
                    "status", "completed",
                    "specialization", specialization,
                    "taskType", taskType,
                    "result", executeSpecializedLogic(specialization, taskType, parameters),
                    "executedAt", java.time.Instant.now().toString());
        });
    }

    /**
     * Execute specialized logic based on the agent's specialization
     */
    private Object executeSpecializedLogic(
            String specialization,
            String taskType,
            Map<String, Object> parameters) {

        // Implement specialized logic based on agent configuration
        // This could call external services, process data, etc.

        switch (specialization) {
            case "data-processor":
                return processData(parameters);
            case "api-caller":
                return callApi(parameters);
            case "validator":
                return validateData(parameters);
            default:
                return Map.of("message", "Task executed successfully");
        }
    }

    private Map<String, Object> processData(Map<String, Object> parameters) {
        // Data processing logic
        return Map.of("processed", true, "data", parameters);
    }

    private Map<String, Object> callApi(Map<String, Object> parameters) {
        // API calling logic
        String url = (String) parameters.getOrDefault("url", "");
        return Map.of("apiCalled", true, "url", url);
    }

    private Map<String, Object> validateData(Map<String, Object> parameters) {
        // Validation logic
        return Map.of("valid", true, "validation", "passed");
    }

    @Override
    public int getMaxConcurrentTasks() {
        return 10;
    }

    @Override
    public boolean canHandle(NodeExecutionTask task) {
        Map<String, Object> context = task.context();
        String agentType = (String) context.get("agentType");
        return "common-agent".equals(agentType) || "COMMON_AGENT".equals(agentType);
    }
}
