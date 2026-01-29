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
class RouterExecutorTest {

        @Inject
        RouterExecutor routerExecutor;

        @Test
        void testRouterExecutor_SimpleCondition() {
                // Given
                NodeExecutionTask task = createTask(Map.of(
                                "message", Map.of("priority", "high", "amount", 1500),
                                "rules", List.of(
                                                Map.of("condition", "jsonpath:$.priority", "targetNode",
                                                                "high-priority-handler", "priority",
                                                                10),
                                                Map.of("condition", "jsonpath:$.amount", "targetNode",
                                                                "large-amount-handler", "priority", 5)),
                                "defaultRoute", "standard-handler"));

                // When
                NodeExecutionResult result = routerExecutor.execute(task)
                                .await().atMost(Duration.ofSeconds(5));

                // Then
                assertThat(result.status()).isEqualTo(NodeExecutionStatus.COMPLETED);
                Map<String, Object> output = result.output();
                assertThat(output.get("route")).isEqualTo("high-priority-handler");
        }

        @Test
        void testRouterExecutor_HeaderCondition() {
                // Given
                NodeExecutionTask task = createTask(Map.of(
                                "message", Map.of("data", "test"),
                                "headers", Map.of("X-Priority", "urgent"),
                                "rules", List.of(
                                                Map.of("condition", "header.X-Priority==urgent", "targetNode",
                                                                "urgent-queue", "priority",
                                                                10))));

                // When
                NodeExecutionResult result = routerExecutor.execute(task)
                                .await().atMost(Duration.ofSeconds(5));

                // Then
                assertThat(result.status()).isEqualTo(NodeExecutionStatus.COMPLETED);
                assertThat(result.output().get("route")).isEqualTo("urgent-queue");
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
