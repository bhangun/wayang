package tech.kayys.wayang.plugin.service;

public record PluginDescriptor(
        String id,
        String name,
        String version,
        PluginType type,
        String description,
        List<PluginDependency> dependencies,
        List<NodeDefinition> nodeTypes,
        List<PatternDefinition> patterns,
        List<ConnectorDefinition> connectors,
        List<TransformerDefinition> transformers,
        Map<String, Object> metadata) {
}
