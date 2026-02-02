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
class IdempotentReceiverExecutorTest {

        @Inject
        IdempotentReceiverExecutor idempotentReceiverExecutor;

        @Test
        void testIdempotentReceiverExecutor_DetectDuplicate() {
                // Given
                Map<String, Object> message = Map.of("messageId", "unique-123", "data", "test");

                NodeExecutionTask task1 = createTask(Map.of(
                                "message", message,
                                "idempotencyKeyField", "messageId",
                                "windowHours", 24));

                NodeExecutionTask task2 = createTask(Map.of(
                                "message", message,
                                "idempotencyKeyField", "messageId",
                                "windowHours", 24));

                // When
                NodeExecutionResult result1 = idempotentReceiverExecutor.execute(task1)
                                .await().atMost(Duration.ofSeconds(5));

                NodeExecutionResult result2 = idempotentReceiverExecutor.execute(task2)
                                .await().atMost(Duration.ofSeconds(5));

                // Then
                assertThat(result1.status()).isEqualTo(NodeExecutionStatus.COMPLETED);
                assertThat(result1.output().get("duplicate")).isEqualTo(false);

                assertThat(result2.status()).isEqualTo(NodeExecutionStatus.COMPLETED);
                assertThat(result2.output().get("duplicate")).isEqualTo(true);
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
