package tech.kayys.wayang.agent.analytic;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.kayys.gamelan.sdk.executor.core.WorkflowExecutor;
import tech.kayys.gamelan.sdk.executor.core.annotation.Executor;
import tech.kayys.gamelan.sdk.executor.core.model.CommunicationType;
import tech.kayys.gamelan.sdk.executor.core.model.NodeExecutionResult;
import tech.kayys.gamelan.sdk.executor.core.model.NodeExecutionTask;
import tech.kayys.wayang.agent.core.inference.AgentInferenceRequest;
import tech.kayys.wayang.agent.core.inference.AgentInferenceResponse;
import tech.kayys.wayang.agent.core.inference.GollekInferenceService;
import tech.kayys.wayang.agent.analytic.prompts.AnalyticsPrompts;

import java.util.Map;

/**
 * Analytics Agent Executor.
 * Handles data analysis and insight generation using AI inference.
 * <p>
 * Supports:
 * <ul>
 * <li>DESCRIPTIVE - What happened?</li>
 * <li>DIAGNOSTIC - Why did it happen?</li>
 * <li>PREDICTIVE - What will happen?</li>
 * <li>PRESCRIPTIVE - What should we do?</li>
 * <li>EXPLORATORY - Open-ended exploration</li>
 * </ul>
 */
@Slf4j
@Executor(executorType = "analytics-agent", communicationType = CommunicationType.GRPC, maxConcurrentTasks = 5)
@ApplicationScoped
public class AnalyticsAgentExecutor implements WorkflowExecutor {

    @Inject
    GollekInferenceService inferenceService;

    @Inject
    AnalyticsPrompts analyticsPrompts;

    @Override
    public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
        return Uni.createFrom().item(() -> {
            try {
                log.info("Executing analytics agent task: {}", task.getNodeId());

                // Extract task parameters
                Map<String, Object> context = task.getContext();
                String taskType = (String) context.getOrDefault("taskType", "DESCRIPTIVE");
                String question = (String) context.get("question");
                String preferredProvider = (String) context.get("preferredProvider");

                if (question == null || question.isBlank()) {
                    return NodeExecutionResult.failure(
                            task.getNodeId(),
                            "Analysis question is required");
                }

                // Execute analytics task
                AgentInferenceResponse response = executeAnalyticsTask(taskType, question, preferredProvider, context);

                if (response.isError()) {
                    return NodeExecutionResult.failure(
                            task.getNodeId(),
                            "Analytics task failed: " + response.getError());
                }

                // Return successful result
                Map<String, Object> result = Map.of(
                        "analysis", response.getContent(),
                        "taskType", taskType,
                        "provider", response.getProviderUsed(),
                        "model", response.getModelUsed(),
                        "tokens", response.getTotalTokens() != null ? response.getTotalTokens() : 0,
                        "latency_ms", response.getLatency().toMillis());

                return NodeExecutionResult.success(task.getNodeId(), result);

            } catch (Exception e) {
                log.error("Analytics agent execution failed", e);
                return NodeExecutionResult.failure(
                        task.getNodeId(),
                        "Execution error: " + e.getMessage());
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

    @Override
    public void onStart() {
        log.info("Analytics agent executor started");
    }

    @Override
    public void onStop() {
        log.info("Analytics agent executor stopped");
    }

    @Override
    public void onError(Throwable error) {
        log.error("Analytics agent executor error", error);
    }
}
