package tech.kayys.wayang.memory;

import tech.kayys.wayang.memory.node.MemoryNodeTypes;
import tech.kayys.wayang.memory.node.MemorySchemas;
import tech.kayys.wayang.plugin.spi.node.NodeDefinition;
import tech.kayys.wayang.plugin.spi.node.NodeProvider;

import java.util.List;
import java.util.Map;

/**
 * Exposes the Long Term Memory Executor capability as a Wayang node.
 */
public class LongTermNodeProvider implements NodeProvider {

    @Override
    public List<NodeDefinition> nodes() {
        return List.of(
                // Long Term Memory Node
                new NodeDefinition(
                        MemoryNodeTypes.LONG_TERM,
                        "Long Term Memory",
                        "Memory",
                        "Recall",
                        "Persistent storage for cross-session knowledge and wisdom.",
                        "database", // Icon
                        "#8B5CF6",
                        MemorySchemas.MEMORY_CONFIG,
                        "{}",
                        "{}",
                        Map.of(
                                "operation", "RETRIEVE",
                                "importance", 0.8,
                                "limit", 10,
                                "minSimilarity", 0.0)));
    }
}
