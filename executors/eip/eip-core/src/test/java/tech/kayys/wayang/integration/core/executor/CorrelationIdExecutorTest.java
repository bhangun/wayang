package tech.kayys.wayang.eip.executor;

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
class CorrelationIdExecutorTest {

        @Inject
        CorrelationIdExecutor correlationIdExecutor;

        @Test
        void testCorrelationIdExecutor_Generate() {
                // Given
                NodeExecutionTask task = createTask(Map.of(
                                "message", Map.of("data", "test"),
                                "strategy", "generate",
                                "headerName", "X-Correlation-ID"));

                // When
                NodeExecutionResult result = correlationIdExecutor.execute(task)
                                .await().atMost(Duration.ofSeconds(5));

                // Then
                assertThat(result.status()).isEqualTo(NodeExecutionStatus.COMPLETED);
                String correlationId = (String) result.output().get("correlationId");
                assertThat(correlationId).isNotEmpty();
                assertThat(correlationId).matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");

                Map<String, Object> headers = (Map<String, Object>) result.output().get("headers");
                assertThat(headers.get("X-Correlation-ID")).isEqualTo(correlationId);
        }

        @Test
        void testCorrelationIdExecutor_Propagate() {
                // Given
                String existingCorrelationId = "existing-correlation-123";
                NodeExecutionTask task = createTask(Map.of(
                                "message", Map.of("data", "test"),
                                "strategy", "propagate",
                                "headerName", "X-Correlation-ID",
                                "headers", Map.of("X-Correlation-ID", existingCorrelationId)));

                // When
                NodeExecutionResult result = correlationIdExecutor.execute(task)
                                .await().atMost(Duration.ofSeconds(5));

                // Then
                assertThat(result.output().get("correlationId")).isEqualTo(existingCorrelationId);
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
