package tech.kayys.wayang.agent.evaluator;

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
import tech.kayys.wayang.agent.schema.EvaluatorAgentConfig;
import tech.kayys.wayang.error.ErrorCode;
import tech.kayys.wayang.error.WayangException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Executor for Evaluator Agents.
 * Responsible for evaluating outputs and providing feedback.
 */
@ApplicationScoped
@Executor(executorType = "agent-evaluator", version = "1.0.0")
public class EvaluatorAgentExecutor extends AbstractAgentExecutor {

    @Inject
    GollekInferenceService inferenceService;

    @Override
    public String getExecutorType() {
        return "agent-evaluator";
    }

    @Override
    protected AgentType getAgentType() {
        return AgentType.EVALUATOR;
    }

    @Override
    protected Uni<NodeExecutionResult> doExecute(NodeExecutionTask task) {
        logger.info("Evaluator agent executing task: {}", task.nodeId());

        return Uni.createFrom().item(() -> {
            try {
                EvaluatorAgentConfig config = objectMapper.convertValue(task.context(), EvaluatorAgentConfig.class);
                Map<String, Object> context = task.context();
                String candidateOutput = firstNonBlank(
                        config.getCandidateOutput(),
                        config.getOutput(),
                        config.getResult(),
                        config.getContent(),
                        (String) context.get("candidateOutput"),
                        (String) context.get("output"),
                        (String) context.get("result"),
                        (String) context.get("content"));
                String criteria = firstNonBlank(config.getCriteria(), (String) context.get("criteria"));
                if (criteria == null || criteria.isBlank()) {
                    criteria = "quality, correctness, completeness";
                }
                String preferredProvider = firstNonBlank(config.getPreferredProvider(),
                        (String) context.get("preferredProvider"));

                if (candidateOutput == null || candidateOutput.isBlank()) {
                    return createFailureResult(task, new WayangException(
                            ErrorCode.VALIDATION_FAILED,
                            "Candidate output is required"));
                }

                AgentInferenceResponse response = executeEvaluationTask(candidateOutput, criteria, preferredProvider,
                        context, config);
                if (response.isError()) {
                    return createFailureResult(task, new WayangException(
                            ErrorCode.INFERENCE_REQUEST_FAILED,
                            response.getError()));
                }

                Map<String, Object> output = Map.of(
                        "status", "COMPLETED",
                        "evaluation", response.getContent(),
                        "result", "Evaluation completed successfully",
                        "criteria", criteria,
                        "provider", response.getProviderUsed(),
                        "model", response.getModelUsed(),
                        "tokens", response.getTotalTokens() != null ? response.getTotalTokens() : 0,
                        "latency_ms", response.getLatency().toMillis());
                return createSuccessResult(task, output);
            } catch (Exception e) {
                logger.error("Evaluator agent execution failed", e);
                return createFailureResult(task, e);
            }
        });
    }

    private AgentInferenceResponse executeEvaluationTask(
            String candidateOutput,
            String criteria,
            String preferredProvider,
            Map<String, Object> context,
            EvaluatorAgentConfig config) {
        String systemPrompt = """
                You are an evaluator agent. Assess outputs critically and provide concise, actionable evaluation.
                Include strengths, weaknesses, and a final verdict.
                """;
        String userPrompt = """
                Evaluate the following output against these criteria: %s

                Output:
                %s
                """.formatted(criteria, candidateOutput);

        Map<String, Object> additionalParams = new LinkedHashMap<>();
        additionalParams.put("context", context);

        AgentInferenceRequest request = AgentInferenceRequest.builder()
                .systemPrompt(systemPrompt)
                .userPrompt(userPrompt)
                .preferredProvider(preferredProvider != null ? preferredProvider : "tech.kayys/anthropic-provider")
                .temperature(config.getTemperature() != null ? config.getTemperature() : 0.1)
                .maxTokens(config.getMaxTokens() != null ? config.getMaxTokens() : 2048)
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
