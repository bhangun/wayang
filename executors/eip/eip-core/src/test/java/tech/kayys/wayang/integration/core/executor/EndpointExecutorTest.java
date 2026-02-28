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

import io.quarkus.test.InjectMock;
import static org.assertj.core.api.Assertions.assertThat;
import jakarta.inject.Inject;

@QuarkusTest
class EndpointExecutorTest {

        @Inject
        EndpointExecutor endpointExecutor;

        @InjectMock
        tech.kayys.wayang.eip.client.EndpointClientRegistry clientRegistry;

        @Test
        void testHttpEndpointExecutor_Success() {
                // Mock client behavior
                tech.kayys.wayang.eip.client.EndpointClient mockClient = org.mockito.Mockito
                                .mock(tech.kayys.wayang.eip.client.EndpointClient.class);
                org.mockito.Mockito.when(clientRegistry.getClient(org.mockito.Mockito.anyString()))
                                .thenReturn(mockClient);

                Map<String, Object> mockResponse = Map.of(
                                "statusCode", 200,
                                "statusMessage", "OK",
                                "headers", Map.of("Content-Type", "application/json"),
                                "body", Map.of("id", "quarkus", "name", "Quarkus"));
                org.mockito.Mockito.when(mockClient.send(org.mockito.Mockito.any(), org.mockito.Mockito.any()))
                                .thenReturn(io.smallrye.mutiny.Uni.createFrom().item(mockResponse));

                // Given
                NodeExecutionTask task = createTask(Map.of(
                                "uri", "https://api.github.com/repos/quarkusio/quarkus",
                                "protocol", "https",
                                "payload", Map.of(),
                                "headers", Map.of("Accept", "application/json"),
                                "timeoutMs", 10000));

                // When
                NodeExecutionResult result = endpointExecutor.execute(task)
                                .await().atMost(Duration.ofSeconds(15));

                // Then
                assertThat(result.status()).isEqualTo(NodeExecutionStatus.COMPLETED);
                Map<String, Object> output = result.output();
                assertThat(result.output().get("status")).isEqualTo(200);
                assertThat(output).containsKey("response");
                assertThat(output).containsKey("endpoint");

                Map<String, Object> response = (Map<String, Object>) output.get("response");
                assertThat(response).containsKey("statusCode");
                assertThat(response.get("statusCode")).isEqualTo(200);
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
