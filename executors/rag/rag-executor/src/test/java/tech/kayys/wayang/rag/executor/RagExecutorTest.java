package tech.kayys.wayang.rag.executor;

import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tech.kayys.gamelan.engine.execution.ExecutionToken;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.engine.node.NodeId;
import tech.kayys.gamelan.engine.run.RetryPolicy;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;
import tech.kayys.gamelan.executor.rag.domain.RagMetrics;
import tech.kayys.gamelan.executor.rag.domain.RagResponse;
import tech.kayys.gamelan.executor.rag.examples.RagQueryService;
import tech.kayys.gamelan.executor.rag.domain.GenerationConfig;
import tech.kayys.gamelan.executor.rag.domain.RetrievalConfig;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RagExecutorTest {

    @Test
    void executeReturnsFailureWhenQueryMissing() {
        RagQueryService ragQueryService = Mockito.mock(RagQueryService.class);
        RagExecutor executor = new RagExecutor();
        executor.ragQueryService = ragQueryService;

        NodeExecutionTask task = createTask(Map.of("tenantId", "tenant-a"));
        NodeExecutionResult result = executor.execute(task).await().atMost(Duration.ofSeconds(2));

        assertFalse((Boolean) result.output().get("success"));
        assertEquals("Missing required field: query", result.output().get("error"));
        assertEquals("tenant-a", result.output().get("tenantId"));
        verifyNoInteractions(ragQueryService);
    }

    @Test
    void executeMapsSuccessfulResponse() {
        RagQueryService ragQueryService = Mockito.mock(RagQueryService.class);
        RagExecutor executor = new RagExecutor();
        executor.ragQueryService = ragQueryService;

        RagResponse response = new RagResponse(
                "What is Wayang?",
                "Wayang is a workflow platform.",
                List.of(),
                List.of(),
                new RagMetrics(123L, 4, 90, 0.82f, 2, 1, true),
                "ctx",
                Instant.parse("2026-02-12T00:00:00Z"),
                Map.of("provider", "openai"),
                List.of("doc-1"),
                Optional.empty());
        when(ragQueryService.query("tenant-b", "What is Wayang?", "knowledge"))
                .thenReturn(Uni.createFrom().item(response));

        NodeExecutionTask task = createTask(Map.of(
                "tenantId", "tenant-b",
                "query", "What is Wayang?",
                "collection", "knowledge"));
        NodeExecutionResult result = executor.execute(task).await().atMost(Duration.ofSeconds(2));

        assertTrue((Boolean) result.output().get("success"));
        assertEquals("Wayang is a workflow platform.", result.output().get("answer"));
        assertEquals(123L, result.output().get("durationMs"));
        assertEquals(4, result.output().get("retrievedDocs"));
        assertEquals(90, result.output().get("tokensGenerated"));
        verify(ragQueryService).query("tenant-b", "What is Wayang?", "knowledge");
    }

    @Test
    void executeMapsServiceFailureAsStructuredOutput() {
        RagQueryService ragQueryService = Mockito.mock(RagQueryService.class);
        RagExecutor executor = new RagExecutor();
        executor.ragQueryService = ragQueryService;

        when(ragQueryService.query("tenant-c", "q", "default"))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("boom")));

        NodeExecutionTask task = createTask(Map.of("tenantId", "tenant-c", "query", "q"));
        NodeExecutionResult result = executor.execute(task).await().atMost(Duration.ofSeconds(2));

        assertFalse((Boolean) result.output().get("success"));
        assertEquals("boom", result.output().get("error"));
        assertEquals("q", result.output().get("query"));
        assertEquals("tenant-c", result.output().get("tenantId"));
    }

    @Test
    void canHandleByNodeTypeAndPayload() {
        RagExecutor executor = new RagExecutor();

        assertTrue(executor.canHandle(createTask(Map.of("nodeType", "rag"))));
        assertTrue(executor.canHandle(createTask(Map.of("type", "RAG-QUERY"))));
        assertTrue(executor.canHandle(createTask(Map.of("executorType", "rag.answer"))));
        assertTrue(executor.canHandle(createTask(Map.of("query", "What is this?"))));
        assertFalse(executor.canHandle(createTask(Map.of("nodeType", "unknown"))));
    }

    @Test
    void configResolutionClampsOutOfRangeValues() {
        RagExecutor executor = new RagExecutor();

        RetrievalConfig retrieval = executor.resolveRetrievalConfig(Map.of(
                "topK", -5,
                "minSimilarity", 1.7f));
        GenerationConfig generation = executor.resolveGenerationConfig(Map.of(
                "temperature", -0.5f,
                "maxTokens", 0));

        assertEquals(1, retrieval.topK());
        assertEquals(1.0f, retrieval.minSimilarity());
        assertEquals(0.0f, generation.temperature());
        assertEquals(1, generation.maxTokens());
    }

    private NodeExecutionTask createTask(Map<String, Object> context) {
        WorkflowRunId runId = new WorkflowRunId(UUID.randomUUID().toString());
        NodeId nodeId = new NodeId("rag-node");
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
