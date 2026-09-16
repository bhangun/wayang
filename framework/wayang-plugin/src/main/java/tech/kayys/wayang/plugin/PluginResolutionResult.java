package tech.kayys.wayang.plugin;

import tech.kayys.wayang.spi.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable record representing the result of plugin dependency resolution.
 */
public record PluginResolutionResult(
        List<Plugin> startupOrder,
        Map<String, List<String>> dependencies
) {

    public PluginResolutionResult {
        startupOrder = List.copyOf(
                Objects.requireNonNull(
                        startupOrder,
                        "startupOrder"));

        dependencies = Map.copyOf(
                Objects.requireNonNull(
                        dependencies,
                        "dependencies"));
    }

    public List<String> startupIds() {
        return startupOrder.stream()
                .map(Plugin::id)
                .toList();
    }
}
