package tech.kayys.wayang.agent.analytic;

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
import tech.kayys.wayang.agent.analytic.prompts.AnalyticsPrompts;
import tech.kayys.wayang.agent.executor.AbstractAgentExecutor;
import tech.kayys.wayang.error.ErrorCode;
import tech.kayys.wayang.error.WayangException;
import tech.kayys.wayang.agent.schema.AnalyticAgentConfig;

import java.util.Map;

/**
 * Executor for Analytic Agents.
 * Responsible for data analysis and insights using AI.
 */
@Slf4j
@ApplicationScoped
@Executor(executorType = "agent-analytic", version = "1.0.0")
public class AnalyticAgentExecutor extends AbstractAgentExecutor {

    @Override
    public String getExecutorType() {
        return "agent-analytic";
    }

    @Inject
    GollekInferenceService inferenceService;

    @Inject
    AnalyticsPrompts analyticsPrompts;

    @Override
    protected AgentType getAgentType() {
        return AgentType.ANALYTICS;
    }

    @Override
    protected Uni<NodeExecutionResult> doExecute(NodeExecutionTask task) {
        logger.info("Analytic agent executing task: {}", task.nodeId());

        return Uni.createFrom().item(() -> {
            try {
                // Extract task parameters
                AnalyticAgentConfig config = objectMapper.convertValue(task.context(), AnalyticAgentConfig.class);
                Map<String, Object> context = task.context();
                String taskType = (String) context.getOrDefault("taskType", "DESCRIPTIVE");
                String question = (String) context.get("question");
                String preferredProvider = (String) context.get("preferredProvider");

                if (question == null || question.isBlank()) {
                    return createFailureResult(task, new WayangException(
                            ErrorCode.VALIDATION_FAILED,
                            "Analysis question is required"));
                }

                // Execute analytics task
                AgentInferenceResponse response = executeAnalyticsTask(taskType, question, preferredProvider, context);

                if (response.isError()) {
                    return createFailureResult(task,
                            new WayangException(
                                    ErrorCode.INFERENCE_REQUEST_FAILED,
                                    "Analytics task failed: " + response.getError()));
                }

                // Return successful result
                Map<String, Object> output = Map.of(
                        "status", "COMPLETED",
                        "analysis", response.getContent(),
                        "taskType", taskType,
                        "provider", response.getProviderUsed(),
                        "model", response.getModelUsed(),
                        "tokens", response.getTotalTokens() != null ? response.getTotalTokens() : 0,
                        "latency_ms", response.getLatency().toMillis());

                return createSuccessResult(task, output);

            } catch (Exception e) {
                logger.error("Analytic agent execution failed", e);
                return createFailureResult(task, e);
            }
        });
    }

    /**
     * Execute an analytics task using AI inference.
     */
    private AgentInferenceResponse executeAnalyticsTask(
            String taskType,
            String question,
            String preferredProvider,
            Map<String, Object> context) {

        // Get prompts based on task type
        String systemPrompt = analyticsPrompts.getSystemPrompt(taskType);
        String userPrompt = analyticsPrompts.getUserPrompt(taskType, question, context);

        // Determine settings
        String provider = preferredProvider != null ? preferredProvider : getDefaultProvider();

        // Analytics needs high precision but also reasoning capability
        double temperature = 0.3;
        int maxTokens = 4096; // Analysis can be verbose

        AgentInferenceRequest request = AgentInferenceRequest.builder()
                .systemPrompt(systemPrompt)
                .userPrompt(userPrompt)
                .preferredProvider(provider)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();

        // Execute with fallback
        String fallbackProvider = determineFallbackProvider(provider);
        return inferenceService.inferWithFallback(request, fallbackProvider);
    }

    /**
     * Get default provider.
     * Cloud providers (Claude/GPT-4) often better for complex analysis.
     */
    private String getDefaultProvider() {
        return "tech.kayys/anthropic-provider"; // Claude 3 is excellent for analysis
    }

    /**
     * Determine fallback provider.
     */
    private String determineFallbackProvider(String primaryProvider) {
        if (primaryProvider == null || primaryProvider.contains("ollama")) {
            return "tech.kayys/openai-provider";
        }
        return "tech.kayys/ollama-provider";
    }
}
