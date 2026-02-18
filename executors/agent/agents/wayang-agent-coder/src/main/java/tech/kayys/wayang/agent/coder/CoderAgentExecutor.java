package tech.kayys.wayang.agent.coder;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.sdk.executor.core.Executor;
import tech.kayys.wayang.agent.AgentType;
import tech.kayys.wayang.agent.core.inference.AgentInferenceRequest;
import tech.kayys.wayang.agent.core.inference.AgentInferenceResponse;
import tech.kayys.wayang.agent.core.inference.GollekInferenceService;
import tech.kayys.wayang.agent.coder.prompts.CodePrompts;
import tech.kayys.wayang.agent.executor.AbstractAgentExecutor;
import tech.kayys.wayang.error.ErrorCode;
import tech.kayys.wayang.error.WayangException;

import java.util.Map;

/**
 * Executor for Coder Agents.
 * Handles code generation, review, refactoring, debugging, and testing using
 * AI.
 */
@Slf4j
@ApplicationScoped
@Executor(type = "agent-coder", version = "1.0.0")
public class CoderAgentExecutor extends AbstractAgentExecutor {

    @Inject
    GollekInferenceService inferenceService;

    @Inject
    CodePrompts codePrompts;

    @Override
    protected AgentType getAgentType() {
        return AgentType.CODER;
    }

    @Override
    protected Uni<NodeExecutionResult> doExecute(NodeExecutionTask task) {
        logger.info("Coder agent executing task: {}", task.nodeId());

        return Uni.createFrom().item(() -> {
            try {
                Map<String, Object> context = task.context();
                String taskType = (String) context.getOrDefault("taskType", "GENERATE");
                String instruction = (String) context.get("instruction");
                String preferredProvider = (String) context.get("preferredProvider");

                if (instruction == null || instruction.isBlank()) {
                    return createFailureResult(task, new WayangException(
                            ErrorCode.VALIDATION_FAILED,
                            "Instruction required"));
                }

                // Execute code task
                AgentInferenceResponse response = executeCodeTask(taskType, instruction, preferredProvider,
                        context);

                if (response.isError()) {
                    return createFailureResult(task, new WayangException(
                            ErrorCode.INFERENCE_REQUEST_FAILED,
                            response.getError()));
                }

                // Return success
                Map<String, Object> output = Map.of(
                        "status", "COMPLETED",
                        "code", response.getContent(),
                        "taskType", taskType,
                        "provider", response.getProviderUsed(),
                        "model", response.getModelUsed(),
                        "tokens", response.getTotalTokens() != null ? response.getTotalTokens() : 0,
                        "latency_ms", response.getLatency().toMillis());

                return createSuccessResult(task, output);

            } catch (Exception e) {
                logger.error("Coder agent execution failed", e);
                return createFailureResult(task, e);
            }
        });
    }

    private AgentInferenceResponse executeCodeTask(
            String taskType,
            String instruction,
            String preferredProvider,
            Map<String, Object> context) {

        String systemPrompt = codePrompts.getSystemPrompt(taskType);
        String userPrompt = codePrompts.getUserPrompt(taskType, instruction, context);

        String provider = preferredProvider != null ? preferredProvider : getDefaultProviderForTask(taskType);
        double temperature = getTemperatureForTask(taskType);
        int maxTokens = getMaxTokensForTask(taskType);

        AgentInferenceRequest request = AgentInferenceRequest.builder()
                .systemPrompt(systemPrompt)
                .userPrompt(userPrompt)
                .preferredProvider(provider)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();

        String fallbackProvider = determineFallbackProvider(provider);
        return inferenceService.inferWithFallback(request, fallbackProvider);
    }

    private String getDefaultProviderForTask(String taskType) {
        return switch (taskType.toUpperCase()) {
            case "REVIEW", "REFACTOR" -> "tech.kayys/anthropic-provider";
            case "GENERATE", "TEST" -> "tech.kayys/openai-provider";
            default -> "tech.kayys/ollama-provider";
        };
    }

    private double getTemperatureForTask(String taskType) {
        return switch (taskType.toUpperCase()) {
            case "REVIEW", "DEBUG" -> 0.3;
            case "REFACTOR", "TEST" -> 0.5;
            case "GENERATE" -> 0.7;
            default -> 0.5;
        };
    }

    private int getMaxTokensForTask(String taskType) {
        return switch (taskType.toUpperCase()) {
            case "GENERATE", "REFACTOR" -> 4096;
            case "REVIEW", "EXPLAIN" -> 3072;
            case "TEST" -> 2048;
            default -> 2048;
        };
    }

    private String determineFallbackProvider(String primaryProvider) {
        if (primaryProvider == null || primaryProvider.contains("ollama")) {
            return "tech.kayys/openai-provider";
        }
        return "tech.kayys/ollama-provider";
    }
}
