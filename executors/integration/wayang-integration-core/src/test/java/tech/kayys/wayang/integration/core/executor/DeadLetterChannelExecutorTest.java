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
class DeadLetterChannelExecutorTest {

    @Inject
    DeadLetterChannelExecutor deadLetterChannelExecutor;

    @Test
    void testDeadLetterChannelExecutor_StoreFailure() {
        // Given
        NodeExecutionTask task = createTask(Map.of(
                "message", Map.of("orderId", "failed-order-123"),
                "error", new RuntimeException("Processing failed"),
                "channelName", "critical-failures",
                "logErrors", true,
                "retentionDays", 7));

        // When
        NodeExecutionResult result = deadLetterChannelExecutor.execute(task)
                .await().atMost(Duration.ofSeconds(5));

        // Then
        assertThat(result.status()).isEqualTo(NodeExecutionStatus.COMPLETED);
        assertThat(result.output().get("sentToDLC")).isEqualTo(true);
        assertThat(result.output().get("originalMessage")).isNotNull();
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
