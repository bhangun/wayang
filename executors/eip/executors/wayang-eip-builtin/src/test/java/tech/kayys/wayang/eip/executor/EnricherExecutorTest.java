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
import tech.kayys.wayang.eip.dto.EnrichmentSourceDto;
import tech.kayys.wayang.eip.service.EnrichmentService;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnricherExecutorTest {

    @Test
    void executeMergesEnrichmentIntoMessageMap() {
        EnricherExecutor executor = new EnricherExecutor();
        executor.objectMapper = lenientMapper();
        executor.enrichmentService = new FakeEnrichmentService(Map.of("customerTier", "gold"));

        NodeExecutionTask task = createTask(Map.of(
                "mergeStrategy", "merge",
                "sources", List.of(Map.of("type", "static", "uri", "ignored", "mapping", Map.of())),
                "message", Map.of("orderId", "ORD-001")));

        NodeExecutionResult result = executor.execute(task)
                .await().atMost(Duration.ofSeconds(3));

        assertEquals(NodeExecutionStatus.COMPLETED, result.status());
        assertInstanceOf(Map.class, result.output().get("message"));
        @SuppressWarnings("unchecked")
        Map<String, Object> mergedMessage = (Map<String, Object>) result.output().get("message");
        assertEquals("ORD-001", mergedMessage.get("orderId"));
        assertEquals("gold", mergedMessage.get("customerTier"));
        assertTrue(result.output().containsKey("enrichedAt"));
    }

    @Test
    void executeWrapsMessageAndEnrichment() {
        EnricherExecutor executor = new EnricherExecutor();
        executor.objectMapper = lenientMapper();
        executor.enrichmentService = new FakeEnrichmentService(Map.of("region", "apac"));

        NodeExecutionTask task = createTask(Map.of(
                "mergeStrategy", "wrap",
                "sources", List.of(Map.of("type", "context", "uri", "ignored", "mapping", Map.of())),
                "message", "payload"));

        NodeExecutionResult result = executor.execute(task)
                .await().atMost(Duration.ofSeconds(3));

        assertEquals(NodeExecutionStatus.COMPLETED, result.status());
        assertInstanceOf(Map.class, result.output().get("message"));
        @SuppressWarnings("unchecked")
        Map<String, Object> wrapped = (Map<String, Object>) result.output().get("message");
        assertEquals("payload", wrapped.get("original"));
        assertEquals(Map.of("region", "apac"), wrapped.get("enrichment"));
    }

    private ObjectMapper lenientMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private NodeExecutionTask createTask(Map<String, Object> context) {
        WorkflowRunId runId = new WorkflowRunId(UUID.randomUUID().toString());
        NodeId nodeId = new NodeId("enricher-test-node");
        int attempt = 1;
        return new NodeExecutionTask(
                runId,
                nodeId,
                attempt,
                ExecutionToken.create(runId, nodeId, attempt, Duration.ofMinutes(5)),
                context,
                RetryPolicy.none());
    }

    private static final class FakeEnrichmentService extends EnrichmentService {
        private final Map<String, Object> enrichment;

        private FakeEnrichmentService(Map<String, Object> enrichment) {
            this.enrichment = enrichment;
        }

        @Override
        public Uni<Map<String, Object>> enrich(EnrichmentSourceDto source, Object message, Map<String, Object> context) {
            return Uni.createFrom().item(enrichment);
        }
    }
}
