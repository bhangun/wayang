package tech.kayys.wayang.eip.executor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import tech.kayys.gamelan.engine.execution.ExecutionToken;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.engine.node.NodeExecutionStatus;
import tech.kayys.gamelan.engine.node.NodeId;
import tech.kayys.gamelan.engine.run.RetryPolicy;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;
import tech.kayys.wayang.eip.service.TransformerRegistry;
import tech.kayys.wayang.eip.strategy.MessageTransformer;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransformerExecutorTest {

    @Test
    void executeAppliesConfiguredTransformer() {
        TransformerExecutor executor = new TransformerExecutor();
        executor.objectMapper = lenientMapper();
        executor.transformerRegistry = new FakeTransformerRegistry("uppercase");

        NodeExecutionTask task = createTask(Map.of(
                "transformType", "uppercase",
                "parameters", Map.of(),
                "message", "hello world"));

        NodeExecutionResult result = executor.execute(task)
                .await().atMost(Duration.ofSeconds(3));

        assertEquals(NodeExecutionStatus.COMPLETED, result.status());
        assertEquals("HELLO WORLD", result.output().get("message"));
        assertTrue(result.output().containsKey("transformedAt"));
    }

    @Test
    void executeFallsBackToIdentityTransformer() {
        TransformerExecutor executor = new TransformerExecutor();
        executor.objectMapper = lenientMapper();
        executor.transformerRegistry = new FakeTransformerRegistry("identity");

        NodeExecutionTask task = createTask(Map.of(
                "transformType", "unknown-type",
                "parameters", Map.of(),
                "message", "keep-me"));

        NodeExecutionResult result = executor.execute(task)
                .await().atMost(Duration.ofSeconds(3));

        assertEquals(NodeExecutionStatus.COMPLETED, result.status());
        assertEquals("keep-me", result.output().get("message"));
    }

    private ObjectMapper lenientMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private NodeExecutionTask createTask(Map<String, Object> context) {
        WorkflowRunId runId = new WorkflowRunId(UUID.randomUUID().toString());
        NodeId nodeId = new NodeId("transformer-test-node");
        int attempt = 1;
        return new NodeExecutionTask(
                runId,
                nodeId,
                attempt,
                ExecutionToken.create(runId, nodeId, attempt, Duration.ofMinutes(5)),
                context,
                RetryPolicy.none());
    }

    private static final class FakeTransformerRegistry extends TransformerRegistry {
        private final String supportedType;

        private FakeTransformerRegistry(String supportedType) {
            this.supportedType = supportedType;
        }

        @Override
        public MessageTransformer getTransformer(String type) {
            if (supportedType.equals(type)) {
                return (message, parameters) -> Uni.createFrom().item(
                        message == null ? null : message.toString().toUpperCase());
            }
            return (message, parameters) -> Uni.createFrom().item(message);
        }
    }
}
