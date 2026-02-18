package tech.kayys.wayang.rag.executor;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.engine.protocol.CommunicationType;
import tech.kayys.gamelan.executor.rag.domain.GenerationConfig;
import tech.kayys.gamelan.executor.rag.domain.RagResponse;
import tech.kayys.gamelan.executor.rag.domain.RagWorkflowInput;
import tech.kayys.gamelan.executor.rag.domain.RetrievalConfig;
import tech.kayys.gamelan.executor.rag.examples.RagQueryService;
import tech.kayys.gamelan.sdk.executor.core.AbstractWorkflowExecutor;
import tech.kayys.gamelan.sdk.executor.core.Executor;
import tech.kayys.gamelan.sdk.executor.core.SimpleNodeExecutionResult;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
@Executor(executorType = "rag-executor", communicationType = CommunicationType.GRPC, maxConcurrentTasks = 20, supportedNodeTypes = {
        "rag", "RAG", "rag-query", "rag.answer" }, version = "1.0.0")
public class RagExecutor extends AbstractWorkflowExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(RagExecutor.class);
    private static final Set<String> SUPPORTED_NODE_TYPES = Set.of("rag", "rag-query", "rag.answer");

    @Inject
    RagQueryService ragQueryService;

    @Override
    public boolean canHandle(NodeExecutionTask task) {
        Map<String, Object> context = task.context() == null ? Map.of() : task.context();
        for (String key : Arrays.asList("nodeType", "type", "executorType")) {
            String value = normalize(stringValue(context.get(key)));
            if (value != null && SUPPORTED_NODE_TYPES.contains(value)) {
                return true;
            }
        }
        // Fallback: allow handling if it looks like a RAG query payload.
        return resolveQuery(context) != null && !resolveQuery(context).isBlank();
    }

    @Override
    public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
        Instant startedAt = Instant.now();
        Map<String, Object> context = task.context() == null ? Map.of() : task.context();

        String tenantId = resolveTenantId(context);
        String query = resolveQuery(context);
        if (query == null || query.isBlank()) {
            return Uni.createFrom().item(SimpleNodeExecutionResult.success(
                    task.runId(),
                    task.nodeId(),
                    task.attempt(),
                    Map.of(
                            "success", false,
                            "error", "Missing required field: query",
                            "tenantId", tenantId),
                    task.token(),
                    Duration.between(startedAt, Instant.now())));
        }

        RetrievalConfig retrievalConfig = resolveRetrievalConfig(context);
        GenerationConfig generationConfig = resolveGenerationConfig(context);
        RagWorkflowInput input = new RagWorkflowInput(tenantId, query, retrievalConfig, generationConfig);

        LOG.info("Executing RAG executor for tenant={}, node={}", tenantId, task.nodeId().value());

        return ragQueryService
                .query(input.tenantId(), input.query(), resolveCollection(context))
                .onItem().transform(response -> toSuccessResult(task, tenantId, response, startedAt))
                .onFailure().recoverWithItem(error -> toFailureResult(task, tenantId, query, error, startedAt));
    }

    NodeExecutionResult toSuccessResult(
            NodeExecutionTask task,
            String tenantId,
            RagResponse response,
            Instant startedAt) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("success", true);
        payload.put("tenantId", tenantId);
        payload.put("query", response.query());
        payload.put("answer", response.answer());
        payload.put("sources", response.sources());
        payload.put("citations", response.citations());
        payload.put("sourceDocuments", response.sourceDocuments());
        payload.put("metadata", response.metadata());
        payload.put("timestamp", response.timestamp().toString());
        payload.put("error", response.error().orElse(null));
        if (response.metrics() != null) {
            payload.put("durationMs", response.metrics().totalDurationMs());
            payload.put("retrievedDocs", response.metrics().documentsRetrieved());
            payload.put("tokensGenerated", response.metrics().tokensGenerated());
        }
        return SimpleNodeExecutionResult.success(
                task.runId(),
                task.nodeId(),
                task.attempt(),
                payload,
                task.token(),
                Duration.between(startedAt, Instant.now()));
    }

    NodeExecutionResult toFailureResult(
            NodeExecutionTask task,
            String tenantId,
            String query,
            Throwable error,
            Instant startedAt) {
        LOG.error("RAG executor failed for tenant={}, node={}", tenantId, task.nodeId().value(), error);
        return SimpleNodeExecutionResult.success(
                task.runId(),
                task.nodeId(),
                task.attempt(),
                Map.of(
                        "success", false,
                        "tenantId", tenantId,
                        "query", query,
                        "error", error.getMessage() == null ? "RAG execution failed" : error.getMessage()),
                task.token(),
                Duration.between(startedAt, Instant.now()));
    }

    String resolveTenantId(Map<String, Object> context) {
        String tenantId = stringValue(context.get("tenantId"));
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = stringValue(context.get("tenant"));
        }
        return (tenantId == null || tenantId.isBlank()) ? "default-tenant" : tenantId;
    }

    String resolveQuery(Map<String, Object> context) {
        String query = stringValue(context.get("query"));
        if (query == null || query.isBlank()) {
            query = stringValue(context.get("question"));
        }
        if (query == null || query.isBlank()) {
            query = stringValue(context.get("prompt"));
        }
        return query;
    }

    String resolveCollection(Map<String, Object> context) {
        String collection = stringValue(context.get("collection"));
        return (collection == null || collection.isBlank()) ? "default" : collection;
    }

    RetrievalConfig resolveRetrievalConfig(Map<String, Object> context) {
        RetrievalConfig defaults = RetrievalConfig.defaults();
        int topK = Math.max(1, intValue(context.get("topK"), defaults.topK()));
        float minSimilarity = clamp(floatValue(context.get("minSimilarity"), defaults.minSimilarity()), 0.0f, 1.0f);

        return new RetrievalConfig(
                topK,
                minSimilarity,
                defaults.maxChunkSize(),
                defaults.chunkOverlap(),
                defaults.enableReranking(),
                defaults.rerankingModel(),
                defaults.enableHybridSearch(),
                defaults.hybridAlpha(),
                defaults.enableMultiQuery(),
                defaults.numQueryVariations(),
                defaults.enableMmr(),
                defaults.mmrLambda(),
                defaults.metadataFilters(),
                defaults.excludedFields(),
                defaults.enableGrouping(),
                defaults.enableDeduplication());
    }

    GenerationConfig resolveGenerationConfig(Map<String, Object> context) {
        GenerationConfig defaults = GenerationConfig.defaults();
        String provider = stringValue(context.get("provider"));
        String model = stringValue(context.get("model"));
        float temperature = clamp(floatValue(context.get("temperature"), defaults.temperature()), 0.0f, 2.0f);
        int maxTokens = Math.max(1, intValue(context.get("maxTokens"), defaults.maxTokens()));
        String systemPrompt = stringValue(context.get("systemPrompt"));

        return new GenerationConfig(
                provider == null || provider.isBlank() ? defaults.provider() : provider,
                model == null || model.isBlank() ? defaults.model() : model,
                temperature,
                maxTokens,
                defaults.topP(),
                defaults.frequencyPenalty(),
                defaults.presencePenalty(),
                defaults.stopSequences(),
                systemPrompt == null || systemPrompt.isBlank() ? defaults.systemPrompt() : systemPrompt,
                defaults.additionalParams(),
                defaults.enableCitations(),
                defaults.enableGrounding(),
                defaults.citationStyle(),
                defaults.enableFactualityChecks(),
                defaults.enableBiasDetection(),
                defaults.safetySettings());
    }

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static int intValue(Object value, int fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static float floatValue(Object value, float fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number number) {
            return number.floatValue();
        }
        try {
            return Float.parseFloat(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
