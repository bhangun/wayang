package tech.kayys.wayang.websearch.executor;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import tech.kayys.gamelan.executor.AbstractNodeExecutor;
import tech.kayys.gamelan.executor.NodeExecutionContext;
import tech.kayys.gamelan.executor.NodeExecutionResult;
import tech.kayys.wayang.node.websearch.SearchOrchestrator;
import tech.kayys.wayang.node.websearch.api.SearchRequest;
import tech.kayys.wayang.node.websearch.node.WebSearchNodeTypes;

import java.util.List;
import java.util.Map;

/**
 * Node executor for performing web searches.
 * Standardized to align with NodeProvider SPI and reactive patterns.
 */
@ApplicationScoped
public class WebSearchNodeExecutor extends AbstractNodeExecutor {

    private static final Logger LOG = Logger.getLogger(WebSearchNodeExecutor.class);

    @Inject
    SearchOrchestrator searchOrchestrator;

    @Override
    public List<String> getSupportedNodeTypes() {
        return List.of(
                WebSearchNodeTypes.WEB_SEARCH,
                "web-search",
                "search",
                "web-search-node");
    }

    @Override
    public Uni<NodeExecutionResult> execute(NodeExecutionContext context) {
        LOG.infof("Executing web search for node: %s", context.nodeId());

        String query = extractSearchQuery(context);
        if (query == null || query.isBlank()) {
            return Uni.createFrom().failure(new IllegalArgumentException("Search query is required"));
        }

        Map<String, Object> config = context.config();

        SearchRequest.Builder builder = SearchRequest.builder()
                .query(query)
                .searchType((String) config.getOrDefault("searchType", "text"))
                .maxResults(((Number) config.getOrDefault("maxResults", 10)).intValue())
                .safeSearch(((Boolean) config.getOrDefault("safeSearch", true)));

        if (config.containsKey("providers")) {
            builder.providers((List<String>) config.get("providers"));
        }

        return searchOrchestrator.search(builder.build())
                .map(response -> NodeExecutionResult.success(Map.of(
                        "results", response.results(),
                        "totalResults", response.totalResults(),
                        "provider", response.providerUsed(),
                        "durationMs", response.durationMs())))
                .onFailure().recoverWithUni(throwable -> {
                    LOG.error("Web search failed", throwable);
                    return Uni.createFrom()
                            .item(NodeExecutionResult.failure("Web search failed: " + throwable.getMessage()));
                });
    }

    private String extractSearchQuery(NodeExecutionContext context) {
        // Try 'query' from input first
        String query = context.inputAsString("query");
        if (query != null && !query.isBlank())
            return query;

        // Try 'text' from input
        query = context.inputAsString("text");
        if (query != null && !query.isBlank())
            return query;

        // Try fallback 'input'
        query = context.inputAsString("input");
        if (query != null && !query.isBlank())
            return query;

        return null;
    }
}
