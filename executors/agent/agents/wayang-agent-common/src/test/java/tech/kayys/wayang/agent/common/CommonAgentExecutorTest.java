package tech.kayys.wayang.agent.common;

import org.junit.jupiter.api.Assertions;
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

public class CommonAgentExecutorTest {

    @Test
    public void testExecuteDataProcessor() {
        CommonAgentExecutor executor = new CommonAgentExecutor();
        RecordingInferenceService inferenceService = new RecordingInferenceService(
                AgentInferenceResponse.builder()
                        .content("Processed content")
                        .providerUsed("test-provider")
                        .modelUsed("test-model")
                        .totalTokens(100)
                        .latency(Duration.ofMillis(500))
                        .build());
        executor.inferenceService = inferenceService;

        Map<String, Object> context = Map.of(
                "taskType", "data-processor",
                "instruction", "process some data",
                "agentId", "agent-common-1");
        NodeExecutionTask task = createTask(context);

        // Execute
        NodeExecutionResult result = executor.execute(task)
                .await().atMost(Duration.ofSeconds(3));

        // Verify
        Assertions.assertEquals(NodeExecutionStatus.COMPLETED, result.status());
        Map<String, Object> output = result.output();
        Assertions.assertEquals("Processed content", output.get("result"));
        Assertions.assertEquals("data-processor", output.get("taskType"));
        Assertions.assertEquals("test-provider", output.get("provider"));
        Assertions.assertEquals(500L, output.get("latency_ms"));

        // Verify inference payload includes full task context for downstream provider/vault
        AgentInferenceRequest request = inferenceService.lastRequest;
        Assertions.assertEquals("agent-common-1", request.getAgentId());
        Assertions.assertTrue(request.getUseMemory());
        Assertions.assertNotNull(request.getAdditionalParams());
        Assertions.assertEquals(context, request.getAdditionalParams().get("context"));
    }

    @Test
    public void testFailure() {
        CommonAgentExecutor executor = new CommonAgentExecutor();
        executor.inferenceService = new RecordingInferenceService(
                AgentInferenceResponse.builder()
                        .error("Inference failed")
                        .build());

        NodeExecutionTask task = createTask(Map.of(
                "taskType", "data-processor",
                "instruction", "process some data"));

        // Execute
        NodeExecutionResult result = executor.execute(task)
                .await().atMost(Duration.ofSeconds(3));

        // Verify failure
        Assertions.assertEquals(NodeExecutionStatus.FAILED, result.status());
        Assertions.assertNotNull(result.error());
        Assertions.assertEquals("inference-error", result.error().code());
        Assertions.assertEquals("Inference failed", result.error().message());
    }

    private NodeExecutionTask createTask(Map<String, Object> context) {
        WorkflowRunId runId = new WorkflowRunId(UUID.randomUUID().toString());
        NodeId nodeId = new NodeId("common-test-node");
        int attempt = 1;
        return new NodeExecutionTask(
                runId,
                nodeId,
                attempt,
                ExecutionToken.create(runId, nodeId, attempt, Duration.ofMinutes(5)),
                context,
                RetryPolicy.none());
    }

    private static final class RecordingInferenceService extends GollekInferenceService {
        private final AgentInferenceResponse response;
        private AgentInferenceRequest lastRequest;

        private RecordingInferenceService(AgentInferenceResponse response) {
            this.response = response;
        }

        @Override
        public AgentInferenceResponse inferWithFallback(AgentInferenceRequest request, String fallbackProvider) {
            this.lastRequest = request;
            return response;
        }
    }
}
