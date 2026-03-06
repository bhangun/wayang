package tech.kayys.wayang.agent.coder;

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
import tech.kayys.wayang.agent.coder.prompts.CodePrompts;
import tech.kayys.wayang.agent.core.inference.AgentInferenceRequest;
import tech.kayys.wayang.agent.core.inference.AgentInferenceResponse;
import tech.kayys.wayang.agent.core.inference.GollekInferenceService;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoderAgentExecutorTest {

    @Test
    void executeReturnsCompletedWhenInferenceSucceeds() {
        TestCoderAgentExecutor executor = new TestCoderAgentExecutor();
        executor.setObjectMapper(lenientMapper());
        executor.codePrompts = new CodePrompts();
        FakeGollekInferenceService inferenceService = new FakeGollekInferenceService(
                AgentInferenceResponse.builder()
                        .content("public class Hello {}")
                        .providerUsed("tech.kayys/openai-provider")
                        .modelUsed("gpt-4o-mini")
                        .totalTokens(120)
                        .latency(Duration.ofMillis(150))
                        .build());
        executor.inferenceService = inferenceService;

        Map<String, Object> context = Map.of(
                "taskType", "GENERATE",
                "instruction", "Create hello world class",
                "language", "java");
        NodeExecutionTask task = createTask(context);

        NodeExecutionResult result = executor.execute(task)
                .await().atMost(Duration.ofSeconds(3));

        assertEquals(NodeExecutionStatus.COMPLETED, result.status());
        assertEquals("COMPLETED", result.output().get("status"));
        assertEquals("public class Hello {}", result.output().get("code"));
        assertEquals("GENERATE", result.output().get("taskType"));
        assertEquals("tech.kayys/openai-provider", inferenceService.lastRequest.getPreferredProvider());
        assertEquals(context, inferenceService.lastRequest.getAdditionalParams().get("context"));
    }

    @Test
    void executeFailsWhenInstructionMissing() {
        TestCoderAgentExecutor executor = new TestCoderAgentExecutor();
        executor.setObjectMapper(lenientMapper());
        executor.codePrompts = new CodePrompts();
        executor.inferenceService = new FakeGollekInferenceService(
                AgentInferenceResponse.builder().content("unused").build());

        NodeExecutionTask task = createTask(Map.of(
                "taskType", "GENERATE"));

        NodeExecutionResult result = executor.execute(task)
                .await().atMost(Duration.ofSeconds(3));

        assertEquals(NodeExecutionStatus.FAILED, result.status());
        assertNotNull(result.error());
        assertTrue(result.error().message().contains("Instruction required"));
    }

    private ObjectMapper lenientMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private NodeExecutionTask createTask(Map<String, Object> context) {
        WorkflowRunId runId = new WorkflowRunId(UUID.randomUUID().toString());
        NodeId nodeId = new NodeId("coder-test-node");
        int attempt = 1;
        return new NodeExecutionTask(
                runId,
                nodeId,
                attempt,
                ExecutionToken.create(runId, nodeId, attempt, Duration.ofMinutes(5)),
                context,
                RetryPolicy.none());
    }

    private static final class TestCoderAgentExecutor extends CoderAgentExecutor {
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
