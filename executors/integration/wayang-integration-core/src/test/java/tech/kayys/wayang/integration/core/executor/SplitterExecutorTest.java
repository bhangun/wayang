package tech.kayys.wayang.integration.core.executor;

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
class SplitterExecutorTest {

        @Inject
        SplitterExecutor splitterExecutor;

        @Test
        void testSplitterExecutor_FixedSize() {
                // Given
                List<Integer> items = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
                NodeExecutionTask task = createTask(Map.of(
                                "message", items,
                                "strategy", "fixed",
                                "batchSize", 3));

                // When
                NodeExecutionResult result = splitterExecutor.execute(task)
                                .await().atMost(Duration.ofSeconds(5));

                // Then
                assertThat(result.status()).isEqualTo(NodeExecutionStatus.COMPLETED);
                List<Object> parts = (List<Object>) result.output().get("parts");
                assertThat(parts).hasSize(4); // 3+3+3+1
                assertThat(result.output().get("partCount")).isEqualTo(4);
        }

        @Test
        void testSplitterExecutor_Delimiter() {
                // Given
                NodeExecutionTask task = createTask(Map.of(
                                "message", "apple,banana,cherry,date,elderberry",
                                "strategy", "delimiter",
                                "delimiter", ","));

                // When
                NodeExecutionResult result = splitterExecutor.execute(task)
                                .await().atMost(Duration.ofSeconds(5));

                // Then
                assertThat(result.status()).isEqualTo(NodeExecutionStatus.COMPLETED);
                List<Object> parts = (List<Object>) result.output().get("parts");
                assertThat(parts).hasSize(5);
                assertThat(parts).contains("apple", "banana", "cherry");
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
