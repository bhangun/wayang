package tech.kayys.wayang.websearch.executor;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import tech.kayys.gamelan.engine.node.*;
import tech.kayys.gamelan.sdk.executor.core.WorkflowExecutor;
import tech.kayys.wayang.node.websearch.SearchOrchestrator;
import tech.kayys.wayang.node.websearch.api.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Web Search Executor for the Gamelan workflow engine
 * Integrates with the existing SearchOrchestrator to perform web searches
 */
@ApplicationScoped
public class WebSearchExecutor implements WorkflowExecutor {

    private static final Logger LOG = Logger.getLogger(WebSearchExecutor.class);

    @Inject
    SearchOrchestrator searchOrchestrator;

    @Override
    public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
        LOG.infof("Executing web search task for node: %s", task.nodeId().value());

        try {
            // Extract search parameters from the task context
            Map<String, Object> context = task.context() == null ? Map.of() : task.context();
            
            // Get the search query from context - could be in different formats
            String query = extractSearchQuery(context);
            if (query == null || query.trim().isEmpty()) {
                LOG.error("Missing or empty search query in task context");
                return Uni.createFrom().item(createFailedResult(task, "Missing search query"));
            }
            query = query.trim();

            // Validate query length
            if (query.length() < 2) {
                LOG.error("Search query too short: " + query);
                return Uni.createFrom().item(createFailedResult(task, "Search query must be at least 2 characters"));
            }

            // Extract other search parameters with defaults and validation
            String searchType = extractSearchType(context);
            
            // Validate search type
            if (!isValidSearchType(searchType)) {
                LOG.warn("Invalid search type: " + searchType + ", defaulting to 'text'");
                searchType = "text";
            }
            
            int maxResults = extractMaxResults(context);
            
            // Validate max results range
            if (maxResults < 1 || maxResults > 100) {
                LOG.warn("Max results out of range [1-100]: " + maxResults + ", defaulting to 10");
                maxResults = 10;
            }

            LOG.infof("Performing search: query='%s', type=%s, max=%d", 
                      query, searchType, maxResults);

            // Create the search request
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .searchType(searchType)
                    .maxResults(maxResults)
                    .build();

            // Perform the search using the existing orchestrator
            return searchOrchestrator.search(searchRequest)
                    .map(searchResponse -> {
                        // Convert search response to execution result
                        Map<String, Object> output = new HashMap<>();
                        output.put("results", searchResponse.results());
                        output.put("totalResults", searchResponse.totalResults());
                        output.put("providerUsed", searchResponse.providerUsed());
                        output.put("durationMs", searchResponse.durationMs());
                        
                        LOG.infof("Search completed successfully with %d results", 
                                  searchResponse.results().size());
                        
                        NodeExecutionResult result = new DefaultNodeExecutionResult(
                                task.runId(),
                                task.nodeId(),
                                task.attempt(),
                                NodeExecutionStatus.COMPLETED,
                                output,
                                null, // No error
                                task.token()
                        );
                        return result;
                    })
                    .onFailure().recoverWithItem(throwable -> {
                        LOG.error("Web search execution failed", throwable);
                        return createFailedResult(task, formatErrorMessage(throwable));
                    });

        } catch (Exception e) {
            LOG.error("Unexpected error during web search execution", e);
            return Uni.createFrom().item(createFailedResult(task, formatErrorMessage(e)));
        }
    }

    private boolean isValidSearchType(String searchType) {
        if (searchType == null) return false;
        
        // Since SearchCapability#fromString doesn't throw exceptions and returns TEXT_SEARCH as default,
        // we'll just check if it's one of the known types
        String lowerType = searchType.toLowerCase(Locale.ROOT);
        return lowerType.equals("text") || 
               lowerType.equals("image") || 
               lowerType.equals("video") || 
               lowerType.equals("news");
    }

    private String extractSearchType(Map<String, Object> context) {
        Object raw = context.get("searchType");
        if (raw == null) {
            return "text";
        }
        String normalized = raw.toString().trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? "text" : normalized;
    }

    private int extractMaxResults(Map<String, Object> context) {
        Object value = context.get("maxResults");
        if (value == null) {
            return 10;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String stringValue) {
            try {
                return Integer.parseInt(stringValue.trim());
            } catch (NumberFormatException ignored) {
                LOG.warnf("Could not parse maxResults value '%s', defaulting to 10", stringValue);
            }
        }
        return 10;
    }

    private String formatErrorMessage(Throwable throwable) {
        if (throwable == null) return "Unknown error";
        
        String message = throwable.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return throwable.getClass().getSimpleName();
        }
        return message;
    }

    private String extractSearchQuery(Map<String, Object> context) {
        // Try different possible keys for the search query
        Object queryObj = context.get("query");
        if (queryObj != null) {
            return queryObj.toString();
        }
        
        queryObj = context.get("searchQuery");
        if (queryObj != null) {
            return queryObj.toString();
        }
        
        queryObj = context.get("input");
        if (queryObj != null) {
            return queryObj.toString();
        }
        
        // Check if there's a nested input object
        Object inputObj = context.get("inputData");
        if (inputObj instanceof Map) {
            Map<?, ?> inputData = (Map<?, ?>) inputObj;
            Object inputQuery = inputData.get("query");
            if (inputQuery != null) {
                return inputQuery.toString();
            }
        }
        
        return null;
    }

    private NodeExecutionResult createFailedResult(NodeExecutionTask task, String errorMessage) {
        var errorInfo = new tech.kayys.gamelan.engine.error.ErrorInfo(
                "WEB_SEARCH_ERROR", 
                errorMessage != null ? errorMessage : "Unknown error during web search",
                null, // stack trace - will be populated by the engine if needed
                Map.of("nodeId", task.nodeId().value())
        );

        return new DefaultNodeExecutionResult(
                task.runId(),
                task.nodeId(),
                task.attempt(),
                NodeExecutionStatus.FAILED,
                null, // No output on failure
                errorInfo,
                task.token()
        );
    }

    @Override
    public String getExecutorType() {
        return "web-search";
    }

    @Override
    public String[] getSupportedNodeTypes() {
        return new String[]{"web-search", "search", "web-search-node"};
    }
}
