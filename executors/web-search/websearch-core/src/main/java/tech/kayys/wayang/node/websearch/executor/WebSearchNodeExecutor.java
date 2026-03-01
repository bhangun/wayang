package tech.kayys.wayang.node.websearch.executor;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
import java.util.List;
import java.util.Map;

/**
 * Node executor for performing web searches.
 */
@ApplicationScoped
@Executor(executorType = "web-search-executor", supportedNodeTypes = {
        WebSearchNodeTypes.WEB_SEARCH
}, description = "Performs web searches using various providers")
public class WebSearchNodeExecutor extends AbstractWorkflowExecutor {

    @Inject
    SearchOrchestrator searchOrchestrator;

    @Override
    public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
        Map<String, Object> context = task.context();
        String query = (String) context.get("query");

        if (query == null || query.isBlank()) {
            return Uni.createFrom().item(SimpleNodeExecutionResult.failure(
                    task.runId(), task.nodeId(), task.attempt(),
                    ErrorInfo.of(new IllegalArgumentException("Input 'query' is required")),
                    task.token()));
        }

        SearchRequest.Builder builder = SearchRequest.builder()
                .query(query)
                .searchType((String) context.getOrDefault("searchType", "text"))
                .maxResults(((Number) context.getOrDefault("maxResults", 10)).intValue())
                .safeSearch((Boolean) context.getOrDefault("safeSearch", true));

        if (context.containsKey("providers")) {
            builder.providers((List<String>) context.get("providers"));
        }

        return searchOrchestrator.search(builder.build())
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
                .onFailure().recoverWithItem(throwable -> SimpleNodeExecutionResult.failure(
                        task.runId(), task.nodeId(), task.attempt(),
                        ErrorInfo.of(throwable), task.token()));
    }
}
