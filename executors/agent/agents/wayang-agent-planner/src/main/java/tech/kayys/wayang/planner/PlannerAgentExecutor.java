package tech.kayys.wayang.agent.executor;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.engine.protocol.CommunicationType;
import tech.kayys.gamelan.sdk.executor.core.Executor;
import tech.kayys.wayang.agent.core.inference.AgentInferenceRequest;
import tech.kayys.wayang.agent.core.inference.AgentInferenceResponse;
import tech.kayys.wayang.agent.core.inference.GollekInferenceService;
import tech.kayys.wayang.agent.planner.PlanningStrategy;
import tech.kayys.wayang.agent.planner.prompts.PlanningPrompts;
import tech.kayys.wayang.agent.type.AgentType;
import tech.kayys.wayang.agent.type.PlannerAgent;
import tech.kayys.wayang.error.ErrorCode;
import tech.kayys.wayang.error.WayangException;

import java.util.*;

/**
 * Executor for PlannerAgent - handles strategic planning and task decomposition
 */
@Executor(executorType = "planner-agent", communicationType = CommunicationType.GRPC, maxConcurrentTasks = 5, supportedNodeTypes = {
                "agent-task", "planner-agent-task", "planning-task" })
@ApplicationScoped
public class PlannerAgentExecutor extends AbstractAgentExecutor {

        @Inject
        GollekInferenceService inferenceService;

        @Inject
        PlanningPrompts planningPrompts;

        @Override
        public String getExecutorType() {
                return "planner-agent";
        }

        @Override
        protected AgentType getAgentType() {
                return new PlannerAgent(
                                PlanningStrategy.PLAN_AND_EXECUTE,
                                5,
                                true);
        }

        @Override
        protected Uni<NodeExecutionResult> doExecute(NodeExecutionTask task) {
                logger.info("PlannerAgentExecutor executing planning task: {}", task.nodeId());

                Map<String, Object> context = task.context();

                // Extract planning configuration
                String strategyName = (String) context.getOrDefault("strategy", "PLAN_AND_EXECUTE");
                PlanningStrategy strategy = PlanningStrategy.valueOf(strategyName);
                String goal = (String) context.getOrDefault("goal", "");
                Map<String, Object> parameters = (Map<String, Object>) context.getOrDefault("parameters", Map.of());

                // Execute planning based on strategy
                return executePlanning(strategy, goal, parameters, task)
                                .map(plan -> createSuccessResult(task, Map.of(
                                                "plan", plan,
                                                "strategy", strategy.name(),
                                                "goal", goal)))
                                .onFailure().recoverWithItem(error -> createFailureResult(task, error));
        }

        /**
         * Execute planning based on the selected strategy using AI inference
         */
        private Uni<Map<String, Object>> executePlanning(
                        PlanningStrategy strategy,
                        String goal,
                        Map<String, Object> parameters,
                        NodeExecutionTask task) {

                return Uni.createFrom().item(() -> {
                        // Get preferred provider from parameters
                        String preferredProvider = (String) parameters.get("preferredProvider");

                        // Get prompts for this strategy
                        String systemPrompt = planningPrompts.getSystemPrompt(strategy.name());
                        String userPrompt = planningPrompts.getUserPrompt(strategy.name(), goal, parameters);

                        // Build inference request
                        AgentInferenceRequest request = AgentInferenceRequest.builder()
                                        .systemPrompt(systemPrompt)
                                        .userPrompt(userPrompt)
                                        .preferredProvider(preferredProvider)
                                        .temperature(0.7) // Balanced creativity for planning
                                        .maxTokens(4096) // Allow longer plans
                                        .build();

                        // Execute inference with fallback
                        String fallbackProvider = determineFallbackProvider(preferredProvider);
                        AgentInferenceResponse response = inferenceService.inferWithFallback(request, fallbackProvider);

                        if (response.isError()) {
                                throw new WayangException(
                                                ErrorCode.INFERENCE_REQUEST_FAILED,
                                                "Planning inference failed: " + response.getError());
                        }

                        // Return plan with metadata
                        return Map.of(
                                        "strategy", strategy.name(),
                                        "plan", response.getContent(),
                                        "provider", response.getProviderUsed(),
                                        "model", response.getModelUsed(),
                                        "tokens", response.getTotalTokens() != null ? response.getTotalTokens() : 0,
                                        "latency_ms", response.getLatency().toMillis());
                });
        }

        /**
         * Determine fallback provider based on primary provider
         */
        private String determineFallbackProvider(String primaryProvider) {
                if (primaryProvider == null) {
                        return "tech.kayys/ollama-provider"; // Default fallback
                }

                // If primary is cloud, fallback to Ollama (local)
                if (primaryProvider.contains("openai") ||
                                primaryProvider.contains("anthropic") ||
                                primaryProvider.contains("gemini")) {
                        return "tech.kayys/ollama-provider";
                }

                // If primary is local, fallback to OpenAI
                if (primaryProvider.contains("ollama") ||
                                primaryProvider.contains("local")) {
                        return "tech.kayys/openai-provider";
                }

                return "tech.kayys/ollama-provider";
        }

        @Override
        public int getMaxConcurrentTasks() {
                return 5; // Planning tasks are more resource-intensive
        }

        @Override
        public boolean canHandle(NodeExecutionTask task) {
                Map<String, Object> context = task.context();
                String agentType = (String) context.get("agentType");
                return "planner-agent".equals(agentType) || "PLANNER_AGENT".equals(agentType);
        }
}
