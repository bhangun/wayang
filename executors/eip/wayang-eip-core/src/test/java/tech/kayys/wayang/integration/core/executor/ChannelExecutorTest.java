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
class ChannelExecutorTest {

        @Inject
        ChannelExecutor channelExecutor;

        @Test
        void testChannelExecutor_SendAndReceive() {
                // Given - Send
                String channelName = "test-channel-" + UUID.randomUUID();
                Map<String, Object> message = Map.of("data", "test message", "timestamp", System.currentTimeMillis());

                NodeExecutionTask sendTask = createTask(Map.of(
                                "operation", "send",
                                "channelName", channelName,
                                "message", message));

                // When - Send
                NodeExecutionResult sendResult = channelExecutor.execute(sendTask)
                                .await().atMost(Duration.ofSeconds(5));

                // Then - Send
                assertThat(sendResult.status()).isEqualTo(NodeExecutionStatus.COMPLETED);
                String messageId = (String) sendResult.output().get("messageId");
                assertThat(messageId).isNotNull();

                // Given - Receive
                NodeExecutionTask receiveTask = createTask(Map.of(
                                "operation", "receive",
                                "channelName", channelName));

                // When - Receive
                NodeExecutionResult receiveResult = channelExecutor.execute(receiveTask)
                                .await().atMost(Duration.ofSeconds(10));

                // Then - Receive
                assertThat(receiveResult.status()).isEqualTo(NodeExecutionStatus.COMPLETED);
                Map<String, Object> received = (Map<String, Object>) receiveResult.output().get("message");
                assertThat(received).isNotNull();
                assertThat(received).containsKey("data");
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
