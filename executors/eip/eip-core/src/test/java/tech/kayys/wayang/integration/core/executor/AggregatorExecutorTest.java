package tech.kayys.wayang.eip.executor;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import tech.kayys.gamelan.engine.node.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import tech.kayys.gamelan.engine.execution.ExecutionToken;
import tech.kayys.gamelan.engine.node.NodeExecutionStatus;
import tech.kayys.gamelan.engine.node.NodeId;
import tech.kayys.gamelan.engine.run.RetryPolicy;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class AggregatorExecutorTest {

        @Inject
        AggregatorExecutor aggregatorExecutor;

        @Test
        void testAggregatorExecutor_CountBased() {
                // Given - Send 3 messages with same correlation ID
                String correlationId = UUID.randomUUID().toString();

                NodeExecutionTask task1 = createTask(Map.of(
                                "message", Map.of("part", 1, "data", "first"),
                                "correlationKey", "orderId",
                                "orderId", correlationId,
                                "expectedCount", 3,
                                "timeoutSeconds", 60));

                NodeExecutionTask task2 = createTask(Map.of(
                                "message", Map.of("part", 2, "data", "second"),
                                "correlationKey", "orderId",
                                "orderId", correlationId,
                                "expectedCount", 3,
                                "timeoutSeconds", 60));

                NodeExecutionTask task3 = createTask(Map.of(
                                "message", Map.of("part", 3, "data", "third"),
                                "correlationKey", "orderId",
                                "orderId", correlationId,
                                "expectedCount", 3,
                                "timeoutSeconds", 60));

                // When
                NodeExecutionResult result1 = aggregatorExecutor.execute(task1)
                                .await().atMost(Duration.ofSeconds(5));

                NodeExecutionResult result2 = aggregatorExecutor.execute(task2)
                                .await().atMost(Duration.ofSeconds(5));

                NodeExecutionResult result3 = aggregatorExecutor.execute(task3)
                                .await().atMost(Duration.ofSeconds(5));

                // Then - Complete batch
                assertThat(result1.status()).isEqualTo(NodeExecutionStatus.PENDING);
                assertThat(result2.status()).isEqualTo(NodeExecutionStatus.PENDING);
                assertThat(result3.status()).isEqualTo(NodeExecutionStatus.COMPLETED);
                List<Object> batch = (List<Object>) result3.output().get("aggregatedMessages");
                assertThat(batch).hasSize(3);
                assertThat(result3.output().get("messageCount")).isEqualTo(3);
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
