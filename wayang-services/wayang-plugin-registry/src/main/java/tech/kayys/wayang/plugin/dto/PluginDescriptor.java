package tech.kayys.wayang.plugin.dto;

import java.time.Instant;

public record PluginDescriptor(
        String id,
        String name,
        SemanticVersion version,
        PluginType type,
        String description,
        String vendor,
        String license,
        Set<PluginCapability> capabilities,
        Set<PluginRequirement> requirements,
        Set<PluginDependency> dependencies,
        Set<String> tags,
        Map<String, Object> metadata,
        Path pluginPath,
        Instant installedAt,
        String checksum,
        PluginStatus status) {
}