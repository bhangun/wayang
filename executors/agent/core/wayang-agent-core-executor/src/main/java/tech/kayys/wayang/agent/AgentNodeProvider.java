package tech.kayys.wayang.agent;

import tech.kayys.wayang.plugin.spi.node.NodeDefinition;
import tech.kayys.wayang.plugin.spi.node.NodeProvider;
import tech.kayys.wayang.agent.schema.*;
import tech.kayys.wayang.schema.generator.SchemaGeneratorUtils;

import java.util.List;
import java.util.Map;

/**
 * Contributes Agent-related node definitions to the unified catalog.
 */
public class AgentNodeProvider implements NodeProvider {

        @Override
        public List<NodeDefinition> nodes() {
                return List.of(
                                new NodeDefinition(
                                                "agent-config", "Agent", "AI", "Agent",
                                                "Base agent configuration",
                                                "bot", "#6366F1",
                                                SchemaGeneratorUtils.generateSchema(AgentConfig.class),
                                                null, null, Map.of()),
                                new NodeDefinition(
                                                "agent-orchestrator", "Orchestrator Agent", "AI", "Agent",
                                                "Orchestrates multi-agent workflows",
                                                "git-branch", "#6366F1",
                                                SchemaGeneratorUtils.generateSchema(OrchestratorAgentConfig.class),
                                                null, null, Map.of()),
                                new NodeDefinition(
                                                "agent-analytic", "Analytic Agent", "AI", "Agent",
                                                "Performs analytical tasks",
                                                "line-chart", "#6366F1",
                                                SchemaGeneratorUtils.generateSchema(AnalyticAgentConfig.class),
                                                null, null, Map.of()),
                                new NodeDefinition(
                                                "agent-coder", "Coder Agent", "AI", "Agent",
                                                "Generates and reviews code",
                                                "code-2", "#6366F1",
                                                SchemaGeneratorUtils.generateSchema(CoderAgentConfig.class),
                                                null, null, Map.of()),
                                new NodeDefinition(
                                                "agent-evaluator", "Evaluator Agent", "AI", "Agent",
                                                "Evaluates and scores outputs",
                                                "check-circle", "#6366F1",
                                                SchemaGeneratorUtils.generateSchema(EvaluatorAgentConfig.class),
                                                null, null, Map.of()),
                                new NodeDefinition(
                                                "agent-planner", "Planner Agent", "AI", "Agent",
                                                "Plans and decomposes tasks",
                                                "list-checks", "#6366F1",
                                                SchemaGeneratorUtils.generateSchema(PlannerAgentConfig.class),
                                                null, null, Map.of()),
                                new NodeDefinition(
                                                "agent-basic", "Basic Agent", "AI", "Agent",
                                                "Simple single-purpose agent",
                                                "bot", "#6366F1",
                                                SchemaGeneratorUtils.generateSchema(BasicAgentConfig.class),
                                                null, null, Map.of()));
        }
}
