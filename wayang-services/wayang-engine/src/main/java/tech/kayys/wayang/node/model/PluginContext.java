package tech.kayys.wayang.node.model;

import java.time.Instant;

import tech.kayys.wayang.schema.node.NodeDefinition;
import tech.kayys.wayang.schema.node.PluginDescriptor;

/**
 * Plugin context for tracking active plugins.
 */
@lombok.Data
@lombok.AllArgsConstructor
class PluginContext {
    private PluginDescriptor plugin;
    private NodeDefinition descriptor;
    private Instant loadedAt;
}
