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
class FilterExecutorTest {

        @Inject
        FilterExecutor filterExecutor;

        @Test
        void testFilterExecutor_SizeComparison() {
                // Given
                NodeExecutionTask task = createTask(Map.of(
                                "message", List.of(1, 2, 3, 4, 5),
                                "expression", "size > 3"));

                // When
                NodeExecutionResult result = filterExecutor.execute(task)
                                .await().atMost(Duration.ofSeconds(5));

                // Then
                assertThat(result.status()).isEqualTo(NodeExecutionStatus.COMPLETED);
                assertThat(result.output().get("reason")).isEqualTo("Message too large");
                assertThat(result.output().get("filtered")).isEqualTo(false);
        }

        @Test
        void testFilterExecutor_NotNull() {
                // Given
                NodeExecutionTask task = createTask(Map.of(
                                "message", "valid data",
                                "expression", "not-null"));

                // When
                NodeExecutionResult result = filterExecutor.execute(task)
                                .await().atMost(Duration.ofSeconds(5));

                // Then
                assertThat(result.output().get("passed")).isEqualTo(true);
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
