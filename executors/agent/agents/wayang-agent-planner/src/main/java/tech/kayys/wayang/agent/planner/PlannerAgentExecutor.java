package tech.kayys.wayang.agent.planner;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.sdk.executor.core.Executor;
import tech.kayys.wayang.agent.AgentType;
import tech.kayys.wayang.agent.core.inference.AgentInferenceRequest;
import tech.kayys.wayang.agent.core.inference.AgentInferenceResponse;
import tech.kayys.wayang.agent.core.inference.GollekInferenceService;
import tech.kayys.wayang.agent.executor.AbstractAgentExecutor;
import tech.kayys.wayang.agent.planner.prompts.PlanningPrompts;
import tech.kayys.wayang.error.ErrorCode;
import tech.kayys.wayang.error.WayangException;

import java.util.LinkedHashMap;
import java.util.Map;
import tech.kayys.wayang.agent.schema.PlannerAgentConfig;

/**
 * Executor for Planner Agents.
 * Responsible for breaking down goals into tasks.
 */
@ApplicationScoped
@Executor(executorType = "agent-planner", version = "1.0.0")
public class PlannerAgentExecutor extends AbstractAgentExecutor {
    @Inject
    GollekInferenceService inferenceService;

    @Inject
    PlanningPrompts planningPrompts;

    @Override
    public String getExecutorType() {
        return "agent-planner";
    }

    @Override
    protected AgentType getAgentType() {
        return AgentType.PLANNER;
    }

    @Override
    protected Uni<NodeExecutionResult> doExecute(NodeExecutionTask task) {
        logger.info("Planner agent executing task: {}", task.nodeId());

        return Uni.createFrom().item(() -> {
            try {
                PlannerAgentConfig config = objectMapper.convertValue(task.context(), PlannerAgentConfig.class);
                Map<String, Object> context = task.context();
                String strategy = firstNonBlank(config.getStrategy(), (String) context.get("strategy"));
                if (strategy == null || strategy.isBlank()) {
                    strategy = "DEFAULT";
                }
                String goal = firstNonBlank(
                        config.getGoal(),
                        config.getObjective(),
                        config.getInstruction(),
                        (String) context.get("goal"),
                        (String) context.get("objective"),
                        (String) context.get("instruction"));
                String preferredProvider = firstNonBlank(config.getPreferredProvider(),
                        (String) context.get("preferredProvider"));

                if (goal == null || goal.isBlank()) {
                    return createFailureResult(task, new WayangException(
                            ErrorCode.VALIDATION_FAILED,
                            "Planning goal is required"));
                }

                AgentInferenceResponse response = executePlanningTask(strategy, goal, preferredProvider, context, config);
                if (response.isError()) {
                    return createFailureResult(task, new WayangException(
                            ErrorCode.INFERENCE_REQUEST_FAILED,
                            response.getError()));
                }

                Map<String, Object> output = Map.of(
                        "status", "COMPLETED",
                        "plan", response.getContent(),
                        "result", "Plan generated successfully",
                        "strategy", strategy,
                        "provider", response.getProviderUsed(),
                        "model", response.getModelUsed(),
                        "tokens", response.getTotalTokens() != null ? response.getTotalTokens() : 0,
                        "latency_ms", response.getLatency().toMillis());
                return createSuccessResult(task, output);
            } catch (Exception e) {
                logger.error("Planner agent execution failed", e);
                return createFailureResult(task, e);
            }
        });
    }

    private AgentInferenceResponse executePlanningTask(
            String strategy,
            String goal,
            String preferredProvider,
            Map<String, Object> context,
            PlannerAgentConfig config) {
        String systemPrompt = planningPrompts.getSystemPrompt(strategy);
        String userPrompt = planningPrompts.getUserPrompt(strategy, goal, context);

        Map<String, Object> additionalParams = new LinkedHashMap<>();
        additionalParams.put("context", context);

        AgentInferenceRequest request = AgentInferenceRequest.builder()
                .systemPrompt(systemPrompt)
                .userPrompt(userPrompt)
                .preferredProvider(preferredProvider != null ? preferredProvider : "tech.kayys/anthropic-provider")
                .temperature(config.getTemperature() != null ? config.getTemperature() : 0.2)
                .maxTokens(config.getMaxTokens() != null ? config.getMaxTokens() : 3072)
                .additionalParams(additionalParams)
                .build();

        return inferenceService.inferWithFallback(request, "tech.kayys/ollama-provider");
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
