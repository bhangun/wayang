package tech.kayys.wayang.agent;

import tech.kayys.wayang.plugin.spi.node.NodeDefinition;
import tech.kayys.wayang.plugin.spi.node.NodeProvider;
import tech.kayys.wayang.agent.schema.AgentConfig;
import tech.kayys.wayang.schema.generator.SchemaGeneratorUtils;

import java.util.List;
import java.util.Map;

/**
 * Contributes Agent-related node definitions to the unified catalog.
 */
public class AgentNodeProvider implements NodeProvider {

        @Override
        public String id() {
                return "tech.kayys.wayang.agent";
        }

        @Override
        public String name() {
                return "Agent Plugin";
        }

        @Override
        public String version() {
                return "1.0.0";
        }

        @Override
        public String description() {
                return "Contributes AI agent node definitions for workflows.";
        }

        @Override
        public List<NodeDefinition> nodes() {
                return List.of(
                                new NodeDefinition(
                                                "agent", "Agent", "AI", "Agent",
                                                "Dynamic skill-based agent",
                                                "bot", "#6366F1",
                                                SchemaGeneratorUtils.generateSchema(AgentConfig.class),
                                                null, null, Map.of()));
        }
}
