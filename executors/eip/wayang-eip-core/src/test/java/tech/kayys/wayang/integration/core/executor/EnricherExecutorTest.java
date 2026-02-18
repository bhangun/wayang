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
class EnricherExecutorTest {

        @Inject
        EnricherExecutor enricherExecutor;

        @Test
        void testEnricherExecutor_StaticEnrichment() {
                // Given
                NodeExecutionTask task = createTask(Map.of(
                                "message", Map.of("orderId", "12345"),
                                "sources", List.of(
                                                Map.of(
                                                                "type", "static",
                                                                "uri", "json:{\"region\":\"US\",\"currency\":\"USD\"}",
                                                                "mapping", Map.of())),
                                "mergeStrategy", "merge"));

                // When
                NodeExecutionResult result = enricherExecutor.execute(task)
                                .await().atMost(Duration.ofSeconds(5));

                // Then
                assertThat(result.status()).isEqualTo(NodeExecutionStatus.COMPLETED);
                Map<String, Object> enriched = (Map<String, Object>) result.output().get("message");
                assertThat(enriched).containsKeys("orderId", "region", "currency");
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
