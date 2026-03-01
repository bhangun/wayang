package tech.kayys.wayang.node.websearch.node;

import tech.kayys.wayang.plugin.spi.node.NodeDefinition;
import tech.kayys.wayang.plugin.spi.node.NodeProvider;

import java.util.List;
import java.util.Map;

/**
 * Implementation of NodeProvider for web search nodes.
 */
public class WebSearchNodeProvider implements NodeProvider {

    @Override
    public List<NodeDefinition> nodes() {
        return List.of(
                new NodeDefinition(
                        WebSearchNodeTypes.WEB_SEARCH,
                        "Web Search",
                        "Search",
                        "Source",
                        "Performs web searches using various providers (Google, Bing, DuckDuckGo) and returns relevant results.",
                        "search", // Icon
                        "#3B82F6", // Blue
                        WebSearchSchemas.WEB_SEARCH_CONFIG,
                        "{}", // Input schema (managed by task)
                        "{}", // Output schema
                        Map.of(
                                "maxResults", 10,
                                "safeSearch", true)));
    }
}
