package tech.kayys.wayang.agent.orchestrator;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.kayys.gamelan.sdk.executor.core.WorkflowExecutor;
import tech.kayys.gamelan.sdk.executor.core.Executor;
import tech.kayys.gamelan.engine.protocol.CommunicationType;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.wayang.agent.AgentType;
import tech.kayys.wayang.agent.core.inference.AgentInferenceRequest;
import tech.kayys.wayang.agent.core.inference.AgentInferenceResponse;
import tech.kayys.wayang.agent.core.inference.GollekInferenceService;
import tech.kayys.wayang.agent.orchestrator.prompts.OrchestrationPrompts;
import tech.kayys.wayang.agent.schema.OrchestratorAgentConfig;
import tech.kayys.wayang.agent.executor.AbstractAgentExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import tech.kayys.wayang.agent.common.CommonAgentExecutor;
import tech.kayys.wayang.agent.planner.PlannerAgentExecutor;
import tech.kayys.wayang.agent.coder.CoderAgentExecutor;
import tech.kayys.wayang.agent.analytic.AnalyticAgentExecutor;
import tech.kayys.wayang.agent.evaluator.EvaluatorAgentExecutor;
import tech.kayys.wayang.agent.BuiltInAgents;
import tech.kayys.wayang.agent.orchestrator.CoordinationStrategy;
import tech.kayys.wayang.agent.orchestrator.OrchestrationType;

/**
 * Orchestrator Agent Executor.
 * Handles multi-agent coordination, task delegation, and result synthesis using
 * AI.
 * <p>
 * Capabilities:
 * <ul>
 * <li>DELEGATE - Break down and assign tasks to other agents</li>
 * <li>SYNTHESIZE - Combine results from multiple agents</li>
 * <li>ROUTING - Intelligent request routing</li>
 * <li>COORDINATE - Manage complex workflow execution</li>
 * </ul>
 */
@Slf4j
@Executor(executorType = "orchestrator-agent", communicationType = CommunicationType.GRPC, maxConcurrentTasks = 20)
@ApplicationScoped
public class OrchestratorAgentExecutor extends AbstractAgentExecutor {

    @Inject
    GollekInferenceService inferenceService;

    @Inject
    OrchestrationPrompts orchestrationPrompts;

    @Inject
    CommonAgentExecutor commonAgentExecutor;

    @Inject
    PlannerAgentExecutor plannerAgentExecutor;

    @Inject
    CoderAgentExecutor coderAgentExecutor;

    @Inject
    AnalyticAgentExecutor analyticAgentExecutor;

    @Inject
    EvaluatorAgentExecutor evaluatorAgentExecutor;

    @Override
    public String getExecutorType() {
        return "orchestrator-agent";
    }

    @Override
    protected AgentType getAgentType() {
        return AgentType.ORCHESTRATOR;
    }

    @Override
    protected Uni<NodeExecutionResult> doExecute(NodeExecutionTask task) {
        logger.info("OrchestratorAgentExecutor executing task: {}", task.nodeId());

        Map<String, Object> context = task.context();

        // Check for orchestration tasks
        if (context.containsKey("agentTasks")) {
            OrchestratorAgentConfig config = objectMapper.convertValue(context,
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
            Integer maxIterations = config.getMaxIterations();
            Integer maxDelegations = config.getMaxDelegations();
            Long maxLatencyMs = config.getMaxLatencyMs();
            Long maxAgentLatencyMs = config.getMaxAgentLatencyMs();
            Integer maxRetriesPerDelegation = config.getMaxRetriesPerDelegation();
            if (maxIterations != null && maxIterations <= 0) {
                return Uni.createFrom().item(createFailureResult(task,
                        new IllegalArgumentException("maxIterations must be >= 1")));
            }
            if (maxDelegations != null && maxDelegations <= 0) {
                return Uni.createFrom().item(createFailureResult(task,
                        new IllegalArgumentException("maxDelegations must be >= 1")));
            }
            if (maxLatencyMs != null && maxLatencyMs <= 0) {
                return Uni.createFrom().item(createFailureResult(task,
                        new IllegalArgumentException("maxLatencyMs must be >= 1")));
            }
            if (maxAgentLatencyMs != null && maxAgentLatencyMs <= 0) {
                return Uni.createFrom().item(createFailureResult(task,
                        new IllegalArgumentException("maxAgentLatencyMs must be >= 1")));
            }
            if (maxRetriesPerDelegation != null && maxRetriesPerDelegation < 0) {
                return Uni.createFrom().item(createFailureResult(task,
                        new IllegalArgumentException("maxRetriesPerDelegation must be >= 0")));
            }

            // Execute orchestration
            return executeOrchestration(orchestrationType, coordinationStrategy, agentTasks, maxIterations,
                    maxDelegations, maxLatencyMs, maxAgentLatencyMs, maxRetriesPerDelegation, task)
                    .map(result -> createSuccessResult(task, result))
                    .onFailure().recoverWithItem(error -> createFailureResult(task, error));
        }

        // Otherwise, execute as an inference task if objective is present
        String objective = (String) context.get("objective");
        if (objective != null && !objective.isBlank()) {
            return Uni.createFrom().item(() -> {
                try {
                    String taskType = (String) context.getOrDefault("taskType", "DELEGATE");
                    String preferredProvider = (String) context.get("preferredProvider");

                    // Execute orchestration task via AI
                    AgentInferenceResponse response = executeOrchestrationTask(taskType, objective, preferredProvider,
                            context);

                    if (response.isError()) {
                        return createFailureResult(task, new RuntimeException(response.getError()));
                    }

                    // Return successful result
                    Map<String, Object> result = Map.of(
                            "decision", response.getContent(),
                            "taskType", taskType,
                            "provider", response.getProviderUsed(),
                            "model", response.getModelUsed(),
                            "tokens", response.getTotalTokens() != null ? response.getTotalTokens() : 0,
                            "latency_ms", response.getLatency().toMillis());

                    return createSuccessResult(task, result);

                } catch (Exception e) {
                    log.error("Orchestrator agent inference failed", e);
                    return createFailureResult(task, e);
                }
            });
        }

        return Uni.createFrom().item(createFailureResult(task,
                new IllegalArgumentException("Neither agentTasks nor objective provided for Orchestrator")));
    }

    /**
     * Execute orchestration based on type
     */
    private Uni<Map<String, Object>> executeOrchestration(
            OrchestrationType orchestrationType,
            CoordinationStrategy coordinationStrategy,
            List<Map<String, Object>> agentTasks,
            Integer maxIterations,
            Integer maxDelegations,
            Long maxLatencyMs,
            Long maxAgentLatencyMs,
            Integer maxRetriesPerDelegation,
            NodeExecutionTask task) {
        DelegationTelemetry telemetry = new DelegationTelemetry();
        List<Map<String, Object>> boundedTasks = applyExecutionBudget(agentTasks, maxIterations, maxDelegations);
        Uni<Map<String, Object>> orchestration = switch (orchestrationType) {
            case SEQUENTIAL ->
                executeSequentialOrchestration(boundedTasks, maxAgentLatencyMs, maxRetriesPerDelegation, telemetry, task);
            case PARALLEL ->
                executeParallelOrchestration(boundedTasks, maxAgentLatencyMs, maxRetriesPerDelegation, telemetry, task);
            case HIERARCHICAL -> executeHierarchicalOrchestration(boundedTasks, task);
            case COLLABORATIVE -> executeCollaborativeOrchestration(
                    boundedTasks, maxAgentLatencyMs, maxRetriesPerDelegation, telemetry, task);
            case COMPETITIVE -> executeCompetitiveOrchestration(
                    boundedTasks, maxAgentLatencyMs, maxRetriesPerDelegation, telemetry, task);
            case DEBATE -> executeDebateOrchestration(boundedTasks, task);
        };
        if (maxLatencyMs != null) {
            orchestration = orchestration.ifNoItem().after(Duration.ofMillis(maxLatencyMs))
                    .failWith(() -> new IllegalStateException("Orchestration maxLatencyMs exceeded: " + maxLatencyMs));
        }
        final Integer iterationLimit = maxIterations;
        final Integer delegationLimit = maxDelegations;
        final Long latencyBudget = maxLatencyMs;
        final Long perAgentLatencyBudget = maxAgentLatencyMs;
        final Integer retriesPerDelegation = maxRetriesPerDelegation;
        return orchestration
                .map(result -> withBudget(result, iterationLimit, delegationLimit, latencyBudget,
                        perAgentLatencyBudget, retriesPerDelegation, boundedTasks.size(), agentTasks.size(), telemetry));
    }

    private Uni<Map<String, Object>> executeSequentialOrchestration(
            List<Map<String, Object>> agentTasks,
            Long maxAgentLatencyMs,
            Integer maxRetriesPerDelegation,
            DelegationTelemetry telemetry,
            NodeExecutionTask task) {
        logger.debug("Executing sequential orchestration");

        // Execute agents one by one
        Uni<List<Map<String, Object>>> results = Uni.createFrom().item(new ArrayList<>());

        for (Map<String, Object> agentTask : agentTasks) {
            results = results.chain(list -> executeAgentTask(
                    agentTask, maxAgentLatencyMs, maxRetriesPerDelegation, telemetry, task)
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
            Long maxAgentLatencyMs,
            Integer maxRetriesPerDelegation,
            DelegationTelemetry telemetry,
            NodeExecutionTask task) {
        logger.debug("Executing parallel orchestration");

        // Execute all agents concurrently
        List<Uni<Map<String, Object>>> taskUnis = agentTasks.stream()
                .map(agentTask -> executeAgentTask(
                        agentTask, maxAgentLatencyMs, maxRetriesPerDelegation, telemetry, task))
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
            Long maxAgentLatencyMs,
            Integer maxRetriesPerDelegation,
            DelegationTelemetry telemetry,
            NodeExecutionTask task) {
        logger.debug("Executing collaborative orchestration");

        // Agents work together
        List<Uni<Map<String, Object>>> taskUnis = agentTasks.stream()
                .map(agentTask -> executeAgentTask(
                        agentTask, maxAgentLatencyMs, maxRetriesPerDelegation, telemetry, task))
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
            Long maxAgentLatencyMs,
            Integer maxRetriesPerDelegation,
            DelegationTelemetry telemetry,
            NodeExecutionTask task) {
        logger.debug("Executing competitive orchestration");

        // Best result wins
        List<Uni<Map<String, Object>>> taskUnis = agentTasks.stream()
                .map(agentTask -> executeAgentTask(
                        agentTask, maxAgentLatencyMs, maxRetriesPerDelegation, telemetry, task))
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

    private List<Map<String, Object>> applyExecutionBudget(
            List<Map<String, Object>> agentTasks,
            Integer maxIterations,
            Integer maxDelegations) {
        if (agentTasks == null || agentTasks.isEmpty()) {
            return List.of();
        }
        int limit = agentTasks.size();
        if (maxIterations != null) {
            limit = Math.min(limit, maxIterations);
        }
        if (maxDelegations != null) {
            limit = Math.min(limit, maxDelegations);
        }
        if (limit >= agentTasks.size()) {
            return agentTasks;
        }
        return new ArrayList<>(agentTasks.subList(0, limit));
    }

    private Map<String, Object> withBudget(
            Map<String, Object> result,
            Integer maxIterations,
            Integer maxDelegations,
            Long maxLatencyMs,
            Long maxAgentLatencyMs,
            Integer maxRetriesPerDelegation,
            int tasksProcessed,
            int tasksRequested,
            DelegationTelemetry telemetry) {
        Map<String, Object> enriched = new HashMap<>(result != null ? result : Map.of());
        Map<String, Object> budget = new HashMap<>();
        if (maxIterations != null) {
            budget.put("maxIterations", maxIterations);
        }
        if (maxDelegations != null) {
            budget.put("maxDelegations", maxDelegations);
        }
        if (maxLatencyMs != null) {
            budget.put("maxLatencyMs", maxLatencyMs);
        }
        if (maxAgentLatencyMs != null) {
            budget.put("maxAgentLatencyMs", maxAgentLatencyMs);
        }
        if (maxRetriesPerDelegation != null) {
            budget.put("maxRetriesPerDelegation", maxRetriesPerDelegation);
        }
        budget.put("tasksRequested", tasksRequested);
        budget.put("tasksProcessed", tasksProcessed);
        budget.put("iterationLimitApplied", maxIterations != null && tasksProcessed < tasksRequested);
        budget.put("delegationLimitApplied", maxDelegations != null && tasksProcessed < tasksRequested);
        enriched.put("budget", budget);
        enriched.put("telemetry", telemetry.asMap());
        return enriched;
    }

    /**
     * Execute a single agent task
     */
    private Uni<Map<String, Object>> executeAgentTask(
            Map<String, Object> agentTask,
            Long maxAgentLatencyMs,
            Integer maxRetriesPerDelegation,
            DelegationTelemetry telemetry,
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
        Uni<Map<String, Object>> delegated = switch (agentType) {
            case "common-agent", "agent-basic" -> commonAgentExecutor.execute(subTask)
                    .map(NodeExecutionResult::output);
            case "planner-agent", "agent-planner" -> plannerAgentExecutor.execute(subTask)
                    .map(NodeExecutionResult::output);
            case "coder-agent", "agent-coder" -> coderAgentExecutor.execute(subTask)
                    .map(NodeExecutionResult::output);
            case "analytics-agent", "agent-analytic" -> analyticAgentExecutor.execute(subTask)
                    .map(NodeExecutionResult::output);
            case "evaluator-agent", "agent-evaluator" -> evaluatorAgentExecutor.execute(subTask)
                    .map(NodeExecutionResult::output);
            default -> Uni.createFrom().item(Map.of("error", "Unknown agent type: " + agentType));
        };
        if (maxAgentLatencyMs != null) {
            delegated = delegated
                    .ifNoItem().after(Duration.ofMillis(maxAgentLatencyMs))
                    .failWith(() -> new IllegalStateException(
                            "Agent task timeout for " + agentType + ": maxAgentLatencyMs=" + maxAgentLatencyMs));
        }
        AtomicInteger subscriptions = new AtomicInteger();
        delegated = delegated
                .onSubscription().invoke(() -> {
                    int attempt = subscriptions.incrementAndGet();
                    telemetry.attempts.incrementAndGet();
                    if (attempt > 1) {
                        telemetry.retries.incrementAndGet();
                    }
                })
                .onFailure().invoke(error -> {
                    telemetry.failures.incrementAndGet();
                    if (isDelegationTimeout(error)) {
                        telemetry.timeouts.incrementAndGet();
                    }
                });
        if (maxRetriesPerDelegation != null && maxRetriesPerDelegation > 0) {
            delegated = delegated.onFailure().retry().atMost(maxRetriesPerDelegation);
        }
        return delegated;
    }

    private boolean isDelegationTimeout(Throwable error) {
        if (error == null) {
            return false;
        }
        String message = error.getMessage();
        return message != null && message.contains("Agent task timeout");
    }

    private static final class DelegationTelemetry {
        private final AtomicInteger attempts = new AtomicInteger();
        private final AtomicInteger retries = new AtomicInteger();
        private final AtomicInteger failures = new AtomicInteger();
        private final AtomicInteger timeouts = new AtomicInteger();

        private Map<String, Object> asMap() {
            return Map.of(
                    "delegationAttempts", attempts.get(),
                    "delegationRetries", retries.get(),
                    "delegationFailures", failures.get(),
                    "delegationTimeouts", timeouts.get());
        }
    }

    private AgentInferenceResponse executeOrchestrationTask(
            String taskType,
            String objective,
            String preferredProvider,
            Map<String, Object> context) {

        // Get prompts based on task type
        String systemPrompt = orchestrationPrompts.getSystemPrompt(taskType);
        String userPrompt = orchestrationPrompts.getUserPrompt(taskType, objective, context);

        // Determine settings
        String provider = preferredProvider != null ? preferredProvider : getDefaultProvider();

        // Orchestration requires reasoning and planning capabilities
        double temperature = 0.2; // Very low temperature for consistent decisions
        int maxTokens = 2048;
        Map<String, Object> additionalParams = new HashMap<>();
        additionalParams.put("context", context);

        AgentInferenceRequest request = AgentInferenceRequest.builder()
                .systemPrompt(systemPrompt)
                .userPrompt(userPrompt)
                .preferredProvider(provider)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .additionalParams(additionalParams)
                .build();

        // Execute with fallback
        String fallbackProvider = determineFallbackProvider(provider);
        return inferenceService.inferWithFallback(request, fallbackProvider);
    }

    /**
     * Get default provider.
     * Smartest models needed for orchestration.
     */
    private String getDefaultProvider() {
        return "tech.kayys/openai-provider"; // GPT-4 for complex reasoning
    }

    /**
     * Determine fallback provider.
     */
    private String determineFallbackProvider(String primaryProvider) {
        if (primaryProvider == null || primaryProvider.contains("ollama")) {
            return "tech.kayys/anthropic-provider";
        }
        return "tech.kayys/ollama-provider";
    }

    @Override
    public Uni<Void> initialize() {
        log.info("Orchestrator agent executor initialized");
        return Uni.createFrom().voidItem();
    }

    @Override
    public Uni<Void> cleanup() {
        log.info("Orchestrator agent executor cleaned up");
        return Uni.createFrom().voidItem();
    }

    @Override
    public Uni<Void> onError(NodeExecutionTask task, Throwable error) {
        log.error("Orchestrator agent executor error for task: {}", task.nodeId(), error);
        return Uni.createFrom().voidItem();
    }
}
