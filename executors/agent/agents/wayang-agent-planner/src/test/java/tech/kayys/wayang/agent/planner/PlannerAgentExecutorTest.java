package tech.kayys.wayang.agent.planner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
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
import tech.kayys.wayang.agent.planner.prompts.PlanningPrompts;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PlannerAgentExecutorTest {

    @Test
    void executeReturnsInferencePlanOutput() {
        TestPlannerAgentExecutor executor = new TestPlannerAgentExecutor();
        executor.setObjectMapper(lenientMapper());
        executor.planningPrompts = new PlanningPrompts();
        FakeGollekInferenceService inferenceService = new FakeGollekInferenceService(
                AgentInferenceResponse.builder()
                        .content("1) Gather requirements 2) Design 3) Implement")
                        .providerUsed("tech.kayys/anthropic-provider")
                        .modelUsed("claude-3.5-sonnet")
                        .totalTokens(111)
                        .latency(Duration.ofMillis(80))
                        .build());
        executor.inferenceService = inferenceService;

        Map<String, Object> context = Map.of(
                "goal", "Build release pipeline",
                "model", "gpt-4o-mini",
                "temperature", 0.2);
        NodeExecutionTask task = createTask(context);

        NodeExecutionResult result = executor.execute(task)
                .await().atMost(Duration.ofSeconds(3));

        assertEquals(NodeExecutionStatus.COMPLETED, result.status());
        assertEquals("COMPLETED", result.output().get("status"));
        assertEquals("1) Gather requirements 2) Design 3) Implement", result.output().get("plan"));
        assertEquals("Plan generated successfully", result.output().get("result"));
        assertNotNull(inferenceService.lastRequest);
        assertEquals("tech.kayys/anthropic-provider", inferenceService.lastRequest.getPreferredProvider());
        assertEquals(context, inferenceService.lastRequest.getAdditionalParams().get("context"));
    }

    @Test
    void canHandleMatchesAgentType() {
        PlannerAgentExecutor executor = new TestPlannerAgentExecutor();

        NodeExecutionTask matchingTask = createTask(Map.of("agentType", "agent-planner"));
        NodeExecutionTask differentTask = createTask(Map.of("agentType", "agent-coder"));
        NodeExecutionTask missingTypeTask = createTask(Map.of("model", "gpt"));

        assertTrue(executor.canHandle(matchingTask));
        assertFalse(executor.canHandle(differentTask));
        assertFalse(executor.canHandle(missingTypeTask));
    }

    private NodeExecutionTask createTask(Map<String, Object> context) {
        WorkflowRunId runId = new WorkflowRunId(UUID.randomUUID().toString());
        NodeId nodeId = new NodeId("planner-test-node");
        int attempt = 1;
        return new NodeExecutionTask(
                runId,
                nodeId,
                attempt,
                ExecutionToken.create(runId, nodeId, attempt, Duration.ofMinutes(5)),
                context,
                RetryPolicy.none());
    }

    private ObjectMapper lenientMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static final class TestPlannerAgentExecutor extends PlannerAgentExecutor {
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
