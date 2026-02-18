package tech.kayys.wayang.agent.common;

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

import java.util.Map;

/**
 * Common Agent Executor.
 * Handles general-purpose task execution using AI inference.
 * <p>
 * Suitable for:
 * <ul>
 * <li>Data processing and transformation</li>
 * <li>API calling and integration</li>
 * <li>Data validation</li>
 * <li>Generic task execution</li>
 * <li>Question answering</li>
 * </ul>
 */
@Slf4j
@Executor(executorType = "common-agent", communicationType = CommunicationType.GRPC, maxConcurrentTasks = 10)
@ApplicationScoped
public class CommonAgentExecutor implements WorkflowExecutor {

    @Inject
    GollekInferenceService inferenceService;

    @Inject
    tech.kayys.wayang.agent.core.tool.ToolRegistry toolRegistry;

    @Override
    public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
        return Uni.createFrom().item(() -> {
            try {
                log.info("Executing common agent task: {}", task.getNodeId());

                // Extract task parameters
                Map<String, Object> context = task.getContext();
                String taskType = (String) context.getOrDefault("taskType", "general");
                String instruction = (String) context.get("instruction");
                String preferredProvider = (String) context.get("preferredProvider");
                List<String> allowedTools = (List<String>) context.get("allowedTools");

                // Get tools if requested
                List<tech.kayys.wayang.tool.spi.Tool> tools = new java.util.ArrayList<>();
                if (allowedTools != null) {
                    for (String toolId : allowedTools) {
                        toolRegistry.getTool(toolId).ifPresent(tools::add);
                    }
                }

                if (instruction == null || instruction.isBlank()) {
                    return NodeExecutionResult.failure(
                            task.getNodeId(),
                            "Instruction is required for common agent task");
                }

                String agentId = (String) context.getOrDefault("agentId", "default-agent");

                // Execute task using inference
                AgentInferenceResponse response = executeTask(taskType, instruction, preferredProvider, context, tools,
                        agentId);

                if (response.isError()) {
                    return NodeExecutionResult.failure(
                            task.getNodeId(),
                            "Task execution failed: " + response.getError());
                }

                // Return successful result
                Map<String, Object> result = Map.of(
                        "result", response.getContent(),
                        "taskType", taskType,
                        "provider", response.getProviderUsed(),
                        "model", response.getModelUsed(),
                        "tokens", response.getTotalTokens() != null ? response.getTotalTokens() : 0,
                        "latency_ms", response.getLatency().toMillis());

                return NodeExecutionResult.success(task.getNodeId(), result);

            } catch (Exception e) {
                log.error("Common agent execution failed", e);
                return NodeExecutionResult.failure(
                        task.getNodeId(),
                        "Execution error: " + e.getMessage());
            }
        });
    }

    /**
     * Execute a task using AI inference.
     */
    private AgentInferenceResponse executeTask(
            String taskType,
            String instruction,
            String preferredProvider,
            Map<String, Object> context,
            List<tech.kayys.wayang.tool.spi.Tool> tools,
            String agentId) {

        // Build system prompt based on task type
        String systemPrompt = getSystemPromptForTaskType(taskType);

        // Build user prompt with instruction and context
        String userPrompt = buildUserPrompt(instruction, context);

        // Build inference request
        AgentInferenceRequest request = AgentInferenceRequest.builder()
                .systemPrompt(systemPrompt)
                .userPrompt(userPrompt)
                .preferredProvider(preferredProvider != null ? preferredProvider : "tech.kayys/ollama-provider")
                .temperature(0.5) // Lower temperature for more deterministic results
                .maxTokens(2048)
                .tools(tools) // Pass allowed tools
                .useMemory(true) // Enable memory by default for common agent
                .agentId(agentId) // Needs to be passed or derived
                .build();

        // Execute with fallback
        String fallbackProvider = determineFallbackProvider(preferredProvider);
        return inferenceService.inferWithFallback(request, fallbackProvider);
    }

    /**
     * Get system prompt based on task type.
     */
    private String getSystemPromptForTaskType(String taskType) {
        return switch (taskType.toLowerCase()) {
            case "data-processor" -> """
                    You are a data processing assistant. Your role is to transform, clean, and manipulate data
                    according to the given instructions. Be precise and follow the specifications exactly.
                    """;
            case "api-caller" -> """
                    You are an API integration assistant. Your role is to help construct API requests,
                    parse responses, and handle data transformations for API integrations.
                    """;
            case "validator" -> """
                    You are a data validation assistant. Your role is to validate data against rules,
                    identify errors, and suggest corrections. Be thorough and specific in your validation.
                    """;
            case "question-answering" -> """
                    You are a helpful assistant that answers questions accurately and concisely.
                    Provide clear, well-structured answers based on the information given.
                    """;
            default -> """
                    You are a helpful AI assistant. Your role is to complete tasks according to the
                    given instructions. Be clear, accurate, and helpful in your responses.
                    """;
        };
    }

    /**
     * Build user prompt with instruction and context.
     */
    private String buildUserPrompt(String instruction, Map<String, Object> context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Instruction: ").append(instruction).append("\n\n");

        // Add context if provided
        if (context != null && !context.isEmpty()) {
            prompt.append("Context:\n");
            context.forEach((key, value) -> {
                if (!key.equals("taskType") && !key.equals("instruction") && !key.equals("preferredProvider")) {
                    prompt.append("- ").append(key).append(": ").append(value).append("\n");
                }
            });
        }

        return prompt.toString();
    }

    /**
     * Determine fallback provider.
     */
    private String determineFallbackProvider(String primaryProvider) {
        if (primaryProvider == null) {
            return "tech.kayys/openai-provider";
        }

        // If primary is cloud, fallback to Ollama (local)
        if (primaryProvider.contains("openai") ||
                primaryProvider.contains("anthropic") ||
                primaryProvider.contains("gemini")) {
            return "tech.kayys/ollama-provider";
        }

        // If primary is local, fallback to OpenAI
        return "tech.kayys/openai-provider";
    }

    @Override
    public void onStart() {
        log.info("Common agent executor started");
        log.info("Available providers: {}", inferenceService.listAvailableProviders());
    }

    @Override
    public void onStop() {
        log.info("Common agent executor stopped");
    }

    @Override
    public void onError(Throwable error) {
        log.error("Common agent executor error", error);
    }
}
