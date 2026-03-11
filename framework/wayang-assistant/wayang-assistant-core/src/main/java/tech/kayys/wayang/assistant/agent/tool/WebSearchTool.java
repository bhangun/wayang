package tech.kayys.wayang.assistant.agent.tool;

import jakarta.enterprise.context.ApplicationScoped;
import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.tool.spi.Tool;

import java.util.List;
import java.util.Map;

/**
 * Tool for performing web searches.
 * Note: This is a placeholder. Actual web search requires proper provider configuration.
 */
@ApplicationScoped
public class WebSearchTool implements Tool {

    @Override
    public String id() {
        return "web-search";
    }

    @Override
    public String name() {
        return "Web Search";
    }

    @Override
    public String description() {
        return "Search the web for real-time information. Note: Requires proper search provider configuration.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "query", Map.of(
                                "type", "string", 
                                "description", "Search query"
                        )
                ),
                "required", List.of("query")
        );
    }

    @Override
    public Uni<Map<String, Object>> execute(Map<String, Object> arguments, Map<String, Object> context) {
        String query = (String) arguments.get("query");
        
        if (query == null || query.trim().isEmpty()) {
            return Uni.createFrom().failure(
                new IllegalArgumentException("Query parameter is required")
            );
        }

        // Placeholder - actual implementation requires search provider
        return Uni.createFrom().item(Map.of(
                "query", query,
                "message", "Web search is available. Configure a search provider (Google, Bing, or DuckDuckGo) to enable actual searches.",
                "tip", "Use wayang-doc-search first for Wayang-specific questions"
        ));
    }
}
