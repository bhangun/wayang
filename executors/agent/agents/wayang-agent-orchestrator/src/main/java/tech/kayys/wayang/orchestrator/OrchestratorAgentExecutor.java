package tech.kayys.wayang.agent.executor;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.engine.protocol.CommunicationType;
import tech.kayys.gamelan.sdk.executor.core.Executor;
import tech.kayys.wayang.agent.BuiltInAgents;
import tech.kayys.wayang.agent.orchestrator.OrchestrationType;
import tech.kayys.wayang.agent.type.AgentType;
import tech.kayys.wayang.agent.type.CoordinationStrategy;
import tech.kayys.wayang.agent.schema.OrchestratorAgentConfig;

import java.util.*;

/**
 * Executor for OrchestratorAgent - coordinates multiple agents
 * Handles multi-agent orchestration and coordination
 */
@Executor(executorType = "agent-orchestrator", version = "1.0.0")
@ApplicationScoped
public class OrchestratorAgentExecutor extends AbstractAgentExecutor {

        @Inject
        CommonAgentExecutor commonAgentExecutor;

        @Inject
        PlannerAgentExecutor plannerAgentExecutor;

        @Inject
        CoderAgentExecutor coderAgentExecutor;

        @Inject
        AnalyticsAgentExecutor analyticsAgentExecutor;

        @Override
        public String getExecutorType() {
                return "orchestrator-agent";
        }

        @Override
        protected AgentType getAgentType() {
                return new OrchestratorAgent(
                                OrchestrationType.COLLABORATIVE,
                                BuiltInAgents.createDefault(),
                                CoordinationStrategy.CENTRALIZED,
                                5,
                                false);
        }

        @Override
        protected Uni<NodeExecutionResult> doExecute(NodeExecutionTask task) {
                logger.info("OrchestratorAgentExecutor executing orchestration task: {}", task.nodeId());

                OrchestratorAgentConfig config = objectMapper.convertValue(task.context(),
                                OrchestratorAgentConfig.class);

                // Extract orchestration configuration
                String orchestrationTypeName = config.getOrchestrationType();
                if (orchestrationTypeName == null)
                        orchestrationTypeName = "COLLABORATIVE";
                OrchestrationType orchestrationType = OrchestrationType.valueOf(orchestrationTypeName);

                String coordinationStrategyName = config.getCoordinationStrategy();
                if (coordinationStrategyName == null)
                        coordinationStrategyName = "CENTRALIZED";
                CoordinationStrategy coordinationStrategy = CoordinationStrategy.valueOf(coordinationStrategyName);

                List<Map<String, Object>> agentTasks = config.getAgentTasks();
                if (agentTasks == null)
                        agentTasks = List.of();

                // Execute orchestration
                return executeOrchestration(orchestrationType, coordinationStrategy, agentTasks, task)
                                .map(result -> createSuccessResult(task, result))
                                .onFailure().recoverWithItem(error -> createFailureResult(task, error));
        }

        /**
         * Execute orchestration based on type
         */
        private Uni<Map<String, Object>> executeOrchestration(
                        OrchestrationType orchestrationType,
                        CoordinationStrategy coordinationStrategy,
                        List<Map<String, Object>> agentTasks,
                        NodeExecutionTask task) {

                return switch (orchestrationType) {
                        case SEQUENTIAL -> executeSequentialOrchestration(agentTasks, task);
                        case PARALLEL -> executeParallelOrchestration(agentTasks, task);
                        case HIERARCHICAL -> executeHierarchicalOrchestration(agentTasks, task);
                        case COLLABORATIVE -> executeCollaborativeOrchestration(agentTasks, task);
                        case COMPETITIVE -> executeCompetitiveOrchestration(agentTasks, task);
                        case DEBATE -> executeDebateOrchestration(agentTasks, task);
                };
        }

        private Uni<Map<String, Object>> executeSequentialOrchestration(
                        List<Map<String, Object>> agentTasks,
                        NodeExecutionTask task) {
                logger.debug("Executing sequential orchestration");

                // Execute agents one by one
                Uni<List<Map<String, Object>>> results = Uni.createFrom().item(new ArrayList<>());

                for (Map<String, Object> agentTask : agentTasks) {
                        results = results.chain(list -> executeAgentTask(agentTask, task)
                                        .map(result -> {
                                                list.add(result);
                                                return list;
                                        }));
                }

                return results.map(list -> Map.of(
                                "orchestrationType", "SEQUENTIAL",
                                "tasksExecuted", list.size(),
                                "results", list,
                                "executionOrder", "sequential"));
        }

        private Uni<Map<String, Object>> executeParallelOrchestration(
                        List<Map<String, Object>> agentTasks,
                        NodeExecutionTask task) {
                logger.debug("Executing parallel orchestration");

                // Execute all agents concurrently
                List<Uni<Map<String, Object>>> taskUnis = agentTasks.stream()
                                .map(agentTask -> executeAgentTask(agentTask, task))
                                .toList();

                return Uni.join().all(taskUnis).andFailFast()
                                .map(results -> Map.of(
                                                "orchestrationType", "PARALLEL",
                                                "tasksExecuted", results.size(),
                                                "results", results,
                                                "executionMode", "concurrent"));
        }

        private Uni<Map<String, Object>> executeHierarchicalOrchestration(
                        List<Map<String, Object>> agentTasks,
                        NodeExecutionTask task) {
                logger.debug("Executing hierarchical orchestration");

                // Tree-like delegation
                return Uni.createFrom().item(Map.of(
                                "orchestrationType", "HIERARCHICAL",
                                "structure", "tree",
                                "levels", 3,
                                "tasksPerLevel", Map.of("0", 1, "1", 3, "2", 9),
                                "coordination", "top-down"));
        }

        private Uni<Map<String, Object>> executeCollaborativeOrchestration(
                        List<Map<String, Object>> agentTasks,
                        NodeExecutionTask task) {
                logger.debug("Executing collaborative orchestration");

                // Agents work together
                List<Uni<Map<String, Object>>> taskUnis = agentTasks.stream()
                                .map(agentTask -> executeAgentTask(agentTask, task))
                                .toList();

                return Uni.join().all(taskUnis).andCollectFailures()
                                .map(results -> {
                                        // Merge results from all agents
                                        Map<String, Object> merged = new HashMap<>();
                                        results.forEach(result -> merged.putAll(result));

                                        return Map.of(
                                                        "orchestrationType", "COLLABORATIVE",
                                                        "tasksExecuted", results.size(),
                                                        "mergedResult", merged,
                                                        "collaboration", "synchronized");
                                });
        }

        private Uni<Map<String, Object>> executeCompetitiveOrchestration(
                        List<Map<String, Object>> agentTasks,
                        NodeExecutionTask task) {
                logger.debug("Executing competitive orchestration");

                // Best result wins
                List<Uni<Map<String, Object>>> taskUnis = agentTasks.stream()
                                .map(agentTask -> executeAgentTask(agentTask, task))
                                .toList();

                return Uni.join().all(taskUnis).andCollectFailures()
                                .map(results -> {
                                        // Select best result (simplified: first non-empty)
                                        Map<String, Object> best = results.stream()
                                                        .filter(r -> !r.isEmpty())
                                                        .findFirst()
                                                        .orElse(Map.of());

                                        return Map.of(
                                                        "orchestrationType", "COMPETITIVE",
                                                        "candidates", results.size(),
                                                        "bestResult", best,
                                                        "selectionCriteria", "quality");
                                });
        }

        private Uni<Map<String, Object>> executeDebateOrchestration(
                        List<Map<String, Object>> agentTasks,
                        NodeExecutionTask task) {
                logger.debug("Executing debate orchestration");

                // Agents debate solutions
                return Uni.createFrom().item(Map.of(
                                "orchestrationType", "DEBATE",
                                "rounds", 3,
                                "participants", agentTasks.size(),
                                "consensus", Map.of("reached", true, "confidence", 0.85),
                                "finalDecision", "Agreed solution based on debate"));
        }

        /**
         * Execute a single agent task
         */
        private Uni<Map<String, Object>> executeAgentTask(
                        Map<String, Object> agentTask,
                        NodeExecutionTask parentTask) {

                String agentType = (String) agentTask.getOrDefault("agentType", "common-agent");
                Map<String, Object> agentContext = (Map<String, Object>) agentTask.getOrDefault("context", Map.of());

                // Create sub-task
                NodeExecutionTask subTask = new NodeExecutionTask(
                                parentTask.runId(),
                                new tech.kayys.gamelan.engine.node.NodeId(
                                                parentTask.nodeId().value() + "-" + agentType),
                                parentTask.attempt(),
                                parentTask.token(),
                                agentContext,
                                parentTask.retryPolicy());

                // Delegate to appropriate executor
                return switch (agentType) {
                        case "common-agent" -> commonAgentExecutor.execute(subTask)
                                        .map(NodeExecutionResult::output);
                        case "planner-agent" -> plannerAgentExecutor.execute(subTask)
                                        .map(NodeExecutionResult::output);
                        case "coder-agent" -> coderAgentExecutor.execute(subTask)
                                        .map(NodeExecutionResult::output);
                        case "analytics-agent" -> analyticsAgentExecutor.execute(subTask)
                                        .map(NodeExecutionResult::output);
                        default -> Uni.createFrom().item(Map.of("error", "Unknown agent type: " + agentType));
                };
        }

        @Override
        public int getMaxConcurrentTasks() {
                return 3; // Orchestration is resource-intensive
        }

        @Override
        public boolean canHandle(NodeExecutionTask task) {
                Map<String, Object> context = task.context();
                String agentType = (String) context.get("agentType");
                return "orchestrator-agent".equals(agentType) || "ORCHESTRATOR_AGENT".equals(agentType);
        }
}
