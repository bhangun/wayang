package tech.kayys.wayang.integration.core.executor;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import tech.kayys.gamelan.engine.node.*;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import tech.kayys.gamelan.engine.execution.ExecutionToken;
import tech.kayys.gamelan.engine.node.NodeExecutionStatus;
import tech.kayys.gamelan.engine.node.NodeId;
import tech.kayys.gamelan.engine.run.RetryPolicy;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class RetryExecutorTest {

    @Inject
    RetryExecutor retryExecutor;

    @Test
    void testRetryExecutor_Success() {
        // Given
        NodeExecutionTask task = createTask(Map.of(
                "operation", Map.of("action", "test"),
                "maxAttempts", 3,
                "initialDelayMs", 100,
                "maxDelayMs", 1000,
                "backoffMultiplier", 2.0));

        // When
        NodeExecutionResult result = retryExecutor.execute(task)
                .await().atMost(Duration.ofSeconds(10));

        // Then
        assertThat(result.status()).isEqualTo(NodeExecutionStatus.COMPLETED);
        assertThat(result.output()).containsKey("attempts");
        Integer attempts = (Integer) result.output().get("attempts");
        assertThat(attempts).isGreaterThan(0).isLessThanOrEqualTo(3);
    }

    private NodeExecutionTask createTask(Map<String, Object> context) {
        WorkflowRunId runId = new WorkflowRunId(UUID.randomUUID().toString());
        NodeId nodeId = new NodeId("test-node");
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
