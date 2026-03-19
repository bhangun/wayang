package tech.kayys.wayang.assistant.agent.tool;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.assistant.agent.WayangAssistantService.DocSearchResult;
import tech.kayys.wayang.assistant.knowledge.KnowledgeSearchService;
import tech.kayys.wayang.tool.spi.Tool;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tool SPI implementation that searches the Wayang official documentation.
 * This tool is discovered via CDI and registered with the tool registry.
 *
 * <p>Injects {@link KnowledgeSearchService} directly (not WayangAssistantService)
 * to avoid a circular CDI dependency chain.
 */
@ApplicationScoped
public class WayangDocSearchTool implements Tool {

    @Inject
    public KnowledgeSearchService searchService;

    @Override
    public String id() {
        return "wayang-doc-search";
    }

    @Override
    public String name() {
        return "Wayang Documentation Search";
    }

    @Override
    public String description() {
        return "Search the official Wayang platform documentation (wayang.github.io) for information about "
                + "agents, RAG, workflows, tools, MCP, orchestration, HITL, guardrails, and more.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "query", Map.of(
                                "type", "string",
                                "description", "Search query – use natural language or specific Wayang terms"
                        ),
                        "maxResults", Map.of(
                                "type", "integer",
                                "description", "Maximum number of results to return (default 10)"
                        )
                ),
                "required", List.of("query")
        );
    }

    @Override
    public Uni<Map<String, Object>> execute(Map<String, Object> arguments, Map<String, Object> context) {
        String query = (String) arguments.get("query");
        if (query == null || query.isBlank()) {
            return Uni.createFrom().failure(new IllegalArgumentException("'query' parameter is required"));
        }

        List<DocSearchResult> results = searchService.searchDocumentation(query);

        List<Map<String, Object>> resultList = results.stream()
                .map(r -> Map.<String, Object>of(
                        "title", r.getTitle(),
                        "url", r.getUrl(),
                        "snippet", r.getSnippet(),
                        "score", r.getScore()
                ))
                .collect(Collectors.toList());

        return Uni.createFrom().item(Map.of(
                "query", query,
                "resultCount", results.size(),
                "results", resultList
        ));
    }
}
