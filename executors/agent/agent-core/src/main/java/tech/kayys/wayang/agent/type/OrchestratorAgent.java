package tech.kayys.wayang.agent.type;

import java.util.Set;

import tech.kayys.wayang.agent.AgentCapability;
import tech.kayys.wayang.agent.AgentRole;
import tech.kayys.wayang.agent.BuiltInAgents;
import tech.kayys.wayang.agent.orchestrator.OrchestrationType;

/**
 * Orchestrator Agent - Coordinates multiple agents
 * Contains built-in sub-agents for planning, execution, and evaluation
 */
public record OrchestratorAgent(
        OrchestrationType orchestrationType,
        BuiltInAgents builtInAgents,
        CoordinationStrategy coordinationStrategy,
        int maxConcurrentAgents,
        boolean supportsRecursiveOrchestration) implements AgentType {

    @Override
    public String typeName() {
        return "ORCHESTRATOR_AGENT";
    }

    @Override
    public Set<AgentCapability> requiredCapabilities() {
        return Set.of(
                AgentCapability.ORCHESTRATION,
                AgentCapability.PLANNING,
                AgentCapability.COORDINATION,
                AgentCapability.EVALUATION);
    }

    @Override
    public AgentRole role() {
        return AgentRole.ORCHESTRATOR;
    }
}
