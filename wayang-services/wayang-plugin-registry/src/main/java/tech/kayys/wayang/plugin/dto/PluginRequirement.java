package tech.kayys.wayang.plugin.dto;

public record PluginRequirement(
        String name,
        SemanticVersion minVersion,
        boolean optional) {
}