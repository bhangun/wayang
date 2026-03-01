package tech.kayys.wayang.websearch.executor;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import tech.kayys.gamelan.engine.error.ErrorInfo;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.sdk.executor.core.AbstractWorkflowExecutor;
import tech.kayys.gamelan.sdk.executor.core.Executor;
import tech.kayys.gamelan.sdk.executor.core.SimpleNodeExecutionResult;
import tech.kayys.wayang.node.websearch.SearchOrchestrator;
import tech.kayys.wayang.node.websearch.api.SearchRequest;
import tech.kayys.wayang.node.websearch.node.WebSearchNodeTypes;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;

/**
 * Web Search Executor for the Gamelan workflow engine.
 * Standardized to align with NodeProvider SPI and reactive patterns.
 */
@ApplicationScoped
@Executor(executorType = "web-search-executor", supportedNodeTypes = {
        WebSearchNodeTypes.WEB_SEARCH
}, description = "Default Web Search Executor")
public class WebSearchExecutor extends AbstractWorkflowExecutor {

    private static final Logger LOG = Logger.getLogger(WebSearchExecutor.class);

    @Inject
    SearchOrchestrator searchOrchestrator;

    @Override
    public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
        LOG.infof("Executing web search task for node: %s", task.nodeId().value());

        Map<String, Object> context = task.context();
        String query = extractSearchQuery(context);

        if (query == null || query.trim().isEmpty()) {
            return Uni.createFrom().item(SimpleNodeExecutionResult.failure(
                    task.runId(), task.nodeId(), task.attempt(),
                    ErrorInfo.of(new IllegalArgumentException("Missing search query")),
                    task.token()));
        }

        String searchType = extractSearchType(context);
        int maxResults = extractMaxResults(context);

        SearchRequest searchRequest = SearchRequest.builder()
                .query(query.trim())
                .searchType(searchType)
                .maxResults(maxResults)
                .build();

        return searchOrchestrator.search(searchRequest)
                .map(response -> {
                    Map<String, Object> output = Map.of(
                            "results", response.results(),
                            "totalResults", response.totalResults(),
                            "provider", response.providerUsed(),
                            "durationMs", response.durationMs());

                    return SimpleNodeExecutionResult.success(
                            task.runId(), task.nodeId(), task.attempt(),
                            output, task.token(), Duration.ZERO);
                })
                .onFailure().recoverWithItem(throwable -> {
                    LOG.error("Web search failed", throwable);
                    return SimpleNodeExecutionResult.failure(
                            task.runId(), task.nodeId(), task.attempt(),
                            ErrorInfo.of(throwable), task.token());
                });
    }

    private String extractSearchQuery(Map<String, Object> context) {
        Object query = context.get("query");
        if (query != null && !query.toString().isBlank())
            return query.toString();

        query = context.get("searchQuery");
        if (query != null && !query.toString().isBlank())
            return query.toString();

        query = context.get("input");
        if (query != null && !query.toString().isBlank())
            return query.toString();

        return null;
    }

    private String extractSearchType(Map<String, Object> context) {
        Object type = context.get("searchType");
        if (type == null)
            return "text";
        String normalized = type.toString().trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? "text" : normalized;
    }

    private int extractMaxResults(Map<String, Object> context) {
        Object value = context.get("maxResults");
        if (value instanceof Number number)
            return number.intValue();
        if (value instanceof String s) {
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return 10;
    }
}
