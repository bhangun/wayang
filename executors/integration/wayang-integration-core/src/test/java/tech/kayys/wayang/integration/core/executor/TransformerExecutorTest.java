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
class TransformerExecutorTest {

        @Inject
        TransformerExecutor transformerExecutor;

        @Test
        void testTransformerExecutor_Uppercase() {
                // Given
                NodeExecutionTask task = createTask(Map.of(
                                "message", "hello world",
                                "transformType", "uppercase"));

                // When
                NodeExecutionResult result = transformerExecutor.execute(task)
                                .await().atMost(Duration.ofSeconds(5));

                // Then
                assertThat(result.status()).isEqualTo(NodeExecutionStatus.COMPLETED);
                assertThat(result.output().get("message")).isEqualTo("HELLO WORLD");
        }

        @Test
        void testTransformerExecutor_Base64() {
                // Given
                NodeExecutionTask task = createTask(Map.of(
                                "message", "Test Message",
                                "transformType", "base64-encode"));

                // When
                NodeExecutionResult result = transformerExecutor.execute(task)
                                .await().atMost(Duration.ofSeconds(5));

                // Then
                assertThat(result.status()).isEqualTo(NodeExecutionStatus.COMPLETED);
                String encoded = (String) result.output().get("message");
                assertThat(encoded).isNotEmpty();
                assertThat(encoded).matches("[A-Za-z0-9+/=]+");
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
