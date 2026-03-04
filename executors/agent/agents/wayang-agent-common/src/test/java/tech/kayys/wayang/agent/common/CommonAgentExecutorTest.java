package tech.kayys.wayang.agent.common;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.engine.node.NodeId;
import tech.kayys.gamelan.engine.node.NodeExecutionStatus;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;
import tech.kayys.wayang.agent.core.inference.AgentInferenceResponse;
import tech.kayys.wayang.agent.core.inference.GollekInferenceService;

import java.time.Duration;
import java.util.Map;

@QuarkusTest
public class CommonAgentExecutorTest {

    @Inject
    CommonAgentExecutor executor;

    @InjectMock
    GollekInferenceService inferenceService;

    @Test
    public void testExecuteDataProcessor() {
        // Setup task
        NodeExecutionTask task = Mockito.mock(NodeExecutionTask.class);
        Mockito.when(task.nodeId()).thenReturn(NodeId.of("node-1"));
        Mockito.when(task.runId()).thenReturn(WorkflowRunId.of("run-1"));
        Mockito.when(task.attempt()).thenReturn(1);
        Mockito.when(task.context()).thenReturn(Map.of(
                "taskType", "data-processor",
                "instruction", "process some data",
                "agentId", "agent-common-1"));

        // Mock inference response
        AgentInferenceResponse response = Mockito.mock(AgentInferenceResponse.class);
        Mockito.when(response.isError()).thenReturn(false);
        Mockito.when(response.getContent()).thenReturn("Processed content");
        Mockito.when(response.getProviderUsed()).thenReturn("test-provider");
        Mockito.when(response.getModelUsed()).thenReturn("test-model");
        Mockito.when(response.getTotalTokens()).thenReturn(100);
        Mockito.when(response.getLatency()).thenReturn(Duration.ofMillis(500));

        Mockito.when(inferenceService.inferWithFallback(Mockito.any(), Mockito.any()))
                .thenReturn(response);

        // Execute
        NodeExecutionResult result = executor.execute(task)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        // Verify
        Assertions.assertEquals(NodeExecutionStatus.COMPLETED, result.status());
        Map<String, Object> output = result.output();
        Assertions.assertEquals("Processed content", output.get("result"));
        Assertions.assertEquals("data-processor", output.get("taskType"));
        Assertions.assertEquals("test-provider", output.get("provider"));
        Assertions.assertEquals(500L, output.get("latency_ms"));

        // Verify inference was called
        Mockito.verify(inferenceService).inferWithFallback(Mockito.any(), Mockito.any());
    }

    @Test
    public void testFailure() {
        // Setup task
        NodeExecutionTask task = Mockito.mock(NodeExecutionTask.class);
        Mockito.when(task.nodeId()).thenReturn(NodeId.of("node-1"));
        Mockito.when(task.runId()).thenReturn(WorkflowRunId.of("run-1"));
        Mockito.when(task.attempt()).thenReturn(1);
        Mockito.when(task.context()).thenReturn(Map.of(
                "taskType", "data-processor",
                "instruction", "process some data"));

        // Mock inference response with error
        AgentInferenceResponse response = Mockito.mock(AgentInferenceResponse.class);
        Mockito.when(response.isError()).thenReturn(true);
        Mockito.when(response.getError()).thenReturn("Inference failed");

        Mockito.when(inferenceService.inferWithFallback(Mockito.any(), Mockito.any()))
                .thenReturn(response);

        // Execute
        NodeExecutionResult result = executor.execute(task)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        // Verify failure
        Assertions.assertEquals(NodeExecutionStatus.FAILED, result.status());
        Assertions.assertNotNull(result.error());
        Assertions.assertEquals("inference-error", result.error().code());
        Assertions.assertEquals("Inference failed", result.error().message());
    }
}
