package tech.kayys.wayang.agent.analytic;

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
import tech.kayys.wayang.agent.analytic.prompts.AnalyticsPrompts;
import tech.kayys.wayang.agent.core.inference.AgentInferenceRequest;
import tech.kayys.wayang.agent.core.inference.AgentInferenceResponse;
import tech.kayys.wayang.agent.core.inference.GollekInferenceService;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalyticAgentExecutorTest {

    @Test
    void executeReturnsCompletedWhenInferenceSucceeds() {
        TestAnalyticAgentExecutor executor = new TestAnalyticAgentExecutor();
        executor.setObjectMapper(lenientMapper());
        executor.analyticsPrompts = new AnalyticsPrompts();
        FakeGollekInferenceService inferenceService = new FakeGollekInferenceService(
                AgentInferenceResponse.builder()
                        .content("Revenue trend is increasing 12% QoQ.")
                        .providerUsed("tech.kayys/anthropic-provider")
                        .modelUsed("claude-3.5-sonnet")
                        .totalTokens(210)
                        .latency(Duration.ofMillis(120))
                        .build());
        executor.inferenceService = inferenceService;

        Map<String, Object> context = Map.of(
                "taskType", "DESCRIPTIVE",
                "question", "What is the revenue trend?");
        NodeExecutionTask task = createTask(context);

        NodeExecutionResult result = executor.execute(task)
                .await().atMost(Duration.ofSeconds(3));

        assertEquals(NodeExecutionStatus.COMPLETED, result.status());
        assertEquals("COMPLETED", result.output().get("status"));
        assertEquals("Revenue trend is increasing 12% QoQ.", result.output().get("analysis"));
        assertEquals("DESCRIPTIVE", result.output().get("taskType"));
        assertEquals("tech.kayys/anthropic-provider", inferenceService.lastRequest.getPreferredProvider());
        assertEquals(context, inferenceService.lastRequest.getAdditionalParams().get("context"));
    }

    @Test
    void executeFailsWhenQuestionMissing() {
        TestAnalyticAgentExecutor executor = new TestAnalyticAgentExecutor();
        executor.setObjectMapper(lenientMapper());
        executor.analyticsPrompts = new AnalyticsPrompts();
        executor.inferenceService = new FakeGollekInferenceService(
                AgentInferenceResponse.builder().content("unused").build());

        NodeExecutionTask task = createTask(Map.of("taskType", "DESCRIPTIVE"));

        NodeExecutionResult result = executor.execute(task)
                .await().atMost(Duration.ofSeconds(3));

        assertEquals(NodeExecutionStatus.FAILED, result.status());
        assertNotNull(result.error());
        assertTrue(result.error().message().contains("Analysis question is required"));
    }

    @Test
    void canHandleMatchesAgentType() {
        AnalyticAgentExecutor executor = new TestAnalyticAgentExecutor();

        NodeExecutionTask matching = createTask(Map.of("agentType", "agent-analytic"));
        NodeExecutionTask different = createTask(Map.of("agentType", "agent-coder"));
        NodeExecutionTask missing = createTask(Map.of("question", "x"));

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
        NodeId nodeId = new NodeId("analytic-test-node");
        int attempt = 1;
        return new NodeExecutionTask(
                runId,
                nodeId,
                attempt,
                ExecutionToken.create(runId, nodeId, attempt, Duration.ofMinutes(5)),
                context,
                RetryPolicy.none());
    }

    private static final class TestAnalyticAgentExecutor extends AnalyticAgentExecutor {
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
