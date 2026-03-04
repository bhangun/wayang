package tech.kayys.wayang.agent.orchestrator;

import java.util.Set;

import tech.kayys.wayang.agent.AgentCapabilityType;
import tech.kayys.wayang.agent.AgentRole;
import tech.kayys.wayang.agent.BuiltInAgents;
import tech.kayys.wayang.agent.orchestrator.OrchestrationType;
import tech.kayys.wayang.agent.orchestrator.CoordinationStrategy;

/**
 * Orchestrator Agent - Coordinates multiple agents
 * Contains built-in sub-agents for planning, execution, and evaluation
 */
public record OrchestratorAgent(
        OrchestrationType orchestrationType,
        BuiltInAgents builtInAgents,
        CoordinationStrategy coordinationStrategy,
        int maxConcurrentAgents,
        boolean supportsRecursiveOrchestration) {

    public String typeName() {
        return "ORCHESTRATOR_AGENT";
    }

    public Set<AgentCapabilityType> requiredCapabilities() {
        return Set.of(
                AgentCapabilityType.ORCHESTRATION,
                AgentCapabilityType.PLANNING,
                AgentCapabilityType.COORDINATION,
                AgentCapabilityType.EVALUATION);
    }

    public AgentRole role() {
        return AgentRole.ORCHESTRATOR;
    }
}
