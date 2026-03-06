package tech.kayys.wayang.agent.evaluator;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import tech.kayys.gamelan.engine.execution.ExecutionToken;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionStatus;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.engine.node.NodeId;
import tech.kayys.gamelan.engine.run.RetryPolicy;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;
import tech.kayys.wayang.agent.core.inference.AgentInferenceRequest;
import tech.kayys.wayang.agent.core.inference.AgentInferenceResponse;
import tech.kayys.wayang.agent.core.inference.GollekInferenceService;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EvaluatorAgentExecutorTest {

    @Test
    void executeReturnsInferenceEvaluation() {
        TestEvaluatorAgentExecutor executor = new TestEvaluatorAgentExecutor();
        executor.setObjectMapper(lenientMapper());
        FakeGollekInferenceService inferenceService = new FakeGollekInferenceService(
                AgentInferenceResponse.builder()
                        .content("Good quality. Minor gaps in error handling.")
                        .providerUsed("tech.kayys/anthropic-provider")
                        .modelUsed("claude-3.5-sonnet")
                        .totalTokens(88)
                        .latency(Duration.ofMillis(70))
                        .build());
        executor.inferenceService = inferenceService;

        Map<String, Object> context = Map.of(
                "candidateOutput", "Implemented API endpoints for projects",
                "criteria", "correctness, quality",
                "model", "gpt-4o-mini",
                "temperature", 0.1);
        NodeExecutionTask task = createTask(context);

        NodeExecutionResult result = executor.execute(task)
                .await().atMost(Duration.ofSeconds(3));

        assertEquals(NodeExecutionStatus.COMPLETED, result.status());
        assertEquals("COMPLETED", result.output().get("status"));
        assertEquals("Good quality. Minor gaps in error handling.", result.output().get("evaluation"));
        assertNotNull(inferenceService.lastRequest);
        assertEquals("tech.kayys/anthropic-provider", inferenceService.lastRequest.getPreferredProvider());
        assertEquals(context, inferenceService.lastRequest.getAdditionalParams().get("context"));
    }

    @Test
    void canHandleMatchesAgentType() {
        EvaluatorAgentExecutor executor = new TestEvaluatorAgentExecutor();

        NodeExecutionTask matching = createTask(Map.of("agentType", "agent-evaluator"));
        NodeExecutionTask different = createTask(Map.of("agentType", "agent-analytic"));
        NodeExecutionTask missing = createTask(Map.of("model", "x"));

        assertTrue(executor.canHandle(matching));
        assertFalse(executor.canHandle(different));
        assertFalse(executor.canHandle(missing));
    }

    private ObjectMapper lenientMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private NodeExecutionTask createTask(Map<String, Object> context) {
        WorkflowRunId runId = new WorkflowRunId(UUID.randomUUID().toString());
        NodeId nodeId = new NodeId("evaluator-test-node");
        int attempt = 1;
        return new NodeExecutionTask(
                runId,
                nodeId,
                attempt,
                ExecutionToken.create(runId, nodeId, attempt, Duration.ofMinutes(5)),
                context,
                RetryPolicy.none());
    }

    private static final class TestEvaluatorAgentExecutor extends EvaluatorAgentExecutor {
        private void setObjectMapper(ObjectMapper mapper) {
            this.objectMapper = mapper;
        }
    }

    private static final class FakeGollekInferenceService extends GollekInferenceService {
        private final AgentInferenceResponse response;
        private AgentInferenceRequest lastRequest;

        private FakeGollekInferenceService(AgentInferenceResponse response) {
            this.response = response;
        }

        @Override
        public AgentInferenceResponse inferWithFallback(AgentInferenceRequest request, String fallbackProvider) {
            this.lastRequest = request;
            return response;
        }
    }
}
