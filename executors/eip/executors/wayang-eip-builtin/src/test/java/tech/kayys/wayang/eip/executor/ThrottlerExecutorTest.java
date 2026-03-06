package tech.kayys.wayang.eip.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import tech.kayys.gamelan.engine.execution.ExecutionToken;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.engine.node.NodeExecutionStatus;
import tech.kayys.gamelan.engine.node.NodeId;
import tech.kayys.gamelan.engine.run.RetryPolicy;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ThrottlerExecutorTest {

    @Test
    void executeReturnsCompletedAndThrottlePayload() {
        ThrottlerExecutor executor = new ThrottlerExecutor();
        executor.objectMapper = new ObjectMapper();

        NodeExecutionTask task = createTask(Map.of(
                "rate", 25,
                "nodeType", "throttler"));

        NodeExecutionResult result = executor.execute(task)
                .await().atMost(Duration.ofSeconds(3));

        assertEquals(NodeExecutionStatus.COMPLETED, result.status());
        assertFalse((Boolean) result.output().get("throttled"));
        assertEquals("throttler", result.output().get("nodeType"));
    }

    @Test
    void executeSupportsAlternativeNodeType() {
        ThrottlerExecutor executor = new ThrottlerExecutor();
        executor.objectMapper = new ObjectMapper();

        NodeExecutionTask task = createTask(Map.of(
                "rate", 10,
                "nodeType", "rate-limiter"));

        NodeExecutionResult result = executor.execute(task)
                .await().atMost(Duration.ofSeconds(3));

        assertFalse((Boolean) result.output().get("throttled"));
        assertEquals("rate-limiter", result.output().get("nodeType"));
    }

    private NodeExecutionTask createTask(Map<String, Object> context) {
        WorkflowRunId runId = new WorkflowRunId(UUID.randomUUID().toString());
        NodeId nodeId = new NodeId("throttler-test-node");
        int attempt = 1;
        return new NodeExecutionTask(
                runId,
                nodeId,
                attempt,
                ExecutionToken.create(runId, nodeId, attempt, Duration.ofMinutes(5)),
                context,
                RetryPolicy.none());
    }
}
