package tech.kayys.wayang.agent.orchestrator;

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
import tech.kayys.wayang.agent.orchestrator.prompts.OrchestrationPrompts;

import java.util.Map;

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
public class OrchestratorAgentExecutor implements WorkflowExecutor {

    @Inject
    GollekInferenceService inferenceService;

    @Inject
    OrchestrationPrompts orchestrationPrompts;

    @Override
    public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
        return Uni.createFrom().item(() -> {
            try {
                log.info("Executing orchestrator agent task: {}", task.getNodeId());

                // Extract task parameters
                Map<String, Object> context = task.getContext();
                String taskType = (String) context.getOrDefault("taskType", "DELEGATE");
                String objective = (String) context.get("objective");
                String preferredProvider = (String) context.get("preferredProvider");

                if (objective == null || objective.isBlank()) {
                    return NodeExecutionResult.failure(
                            task.getNodeId(),
                            "Orchestration objective is required");
                }

                // Execute orchestration task
                AgentInferenceResponse response = executeOrchestrationTask(taskType, objective, preferredProvider,
                        context);

                if (response.isError()) {
                    return NodeExecutionResult.failure(
                            task.getNodeId(),
                            "Orchestration task failed: " + response.getError());
                }

                // Return successful result
                Map<String, Object> result = Map.of(
                        "decision", response.getContent(),
                        "taskType", taskType,
                        "provider", response.getProviderUsed(),
                        "model", response.getModelUsed(),
                        "tokens", response.getTotalTokens() != null ? response.getTotalTokens() : 0,
                        "latency_ms", response.getLatency().toMillis());

                return NodeExecutionResult.success(task.getNodeId(), result);

            } catch (Exception e) {
                log.error("Orchestrator agent execution failed", e);
                return NodeExecutionResult.failure(
                        task.getNodeId(),
                        "Execution error: " + e.getMessage());
            }
        });
    }

    /**
     * Execute an orchestration task using AI inference.
     */
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
    public void onStart() {
        log.info("Orchestrator agent executor started");
    }

    @Override
    public void onStop() {
        log.info("Orchestrator agent executor stopped");
    }

    @Override
    public void onError(Throwable error) {
        log.error("Orchestrator agent executor error", error);
    }
}
