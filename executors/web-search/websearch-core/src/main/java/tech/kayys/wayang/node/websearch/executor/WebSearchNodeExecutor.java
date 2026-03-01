package tech.kayys.wayang.node.websearch.executor;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
 */
@ApplicationScoped
public class WebSearchNodeExecutor extends AbstractNodeExecutor {

    @Inject
    SearchOrchestrator searchOrchestrator;

    @Override
    public List<String> getSupportedNodeTypes() {
        return List.of(WebSearchNodeTypes.WEB_SEARCH);
    }

    @Override
    public Uni<NodeExecutionResult> execute(NodeExecutionContext context) {
        String query = context.inputAsString("query");
        if (query == null || query.isBlank()) {
            return Uni.createFrom().failure(new IllegalArgumentException("Input 'query' is required"));
        }

        Map<String, Object> config = context.config();

        SearchRequest.Builder builder = SearchRequest.builder()
                .query(query)
                .searchType((String) config.getOrDefault("searchType", "text"))
                .maxResults(((Number) config.getOrDefault("maxResults", 10)).intValue())
                .safeSearch((Boolean) config.getOrDefault("safeSearch", true));

        if (config.containsKey("providers")) {
            builder.providers((List<String>) config.get("providers"));
        }

        return searchOrchestrator.search(builder.build())
                .map(response -> NodeExecutionResult.success(Map.of(
                        "results", response.results(),
                        "totalResults", response.totalResults(),
                        "provider", response.providerUsed(),
                        "durationMs", response.durationMs())))
                .onFailure().recoverWithItem(
                        throwable -> NodeExecutionResult.failure("Web search failed: " + throwable.getMessage()));
    }
}
