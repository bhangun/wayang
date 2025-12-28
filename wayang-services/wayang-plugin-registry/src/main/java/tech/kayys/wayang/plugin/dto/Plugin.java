package tech.kayys.wayang.plugin.dto;

import java.util.*;

public interface Plugin {
    /*
     * public sealed interface Plugin permits AgentPlugin, ConnectorPlugin,
     * TransformerPlugin,
     * KnowledgePlugin, CorePlugin, ExtensionPlugin {
     */
    String getId();

    String getName();

    SemanticVersion getVersion();

    PluginType getType();

    PluginDescriptor getDescriptor();

    PluginContext getContext();

    Uni<Void> initialize(PluginContext context);

    Uni<Void> destroy();
}