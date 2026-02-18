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
class MessageStoreExecutorTest {

        @Inject
        MessageStoreExecutor messageStoreExecutor;

        @Test
        void testMessageStoreExecutor_StoreAndRetrieve() {
                // Given - Store
                NodeExecutionTask storeTask = createTask(Map.of(
                                "operation", "store",
                                "message", Map.of("data", "important payload", "timestamp", System.currentTimeMillis()),
                                "storeType", "in-memory",
                                "retentionDays", 7));

                // When - Store
                NodeExecutionResult storeResult = messageStoreExecutor.execute(storeTask)
                                .await().atMost(Duration.ofSeconds(5));

                // Then - Store
                assertThat(storeResult.status()).isEqualTo(NodeExecutionStatus.COMPLETED);
                String messageId = (String) storeResult.output().get("messageId");
                assertThat(messageId).isNotNull();
                // ...

                // Given - Retrieve
                NodeExecutionTask retrieveTask = createTask(Map.of(
                                "operation", "retrieve",
                                "messageId", messageId));

                // When - Retrieve
                NodeExecutionResult retrieveResult = messageStoreExecutor.execute(retrieveTask)
                                .await().atMost(Duration.ofSeconds(5));

                // Then - Retrieve
                assertThat(retrieveResult.status()).isEqualTo(NodeExecutionStatus.COMPLETED);
                Map<String, Object> retrieved = (Map<String, Object>) retrieveResult.output().get("message");
                assertThat(retrieved).containsKey("data");
                assertThat(retrieved.get("data")).isEqualTo("important payload");
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
