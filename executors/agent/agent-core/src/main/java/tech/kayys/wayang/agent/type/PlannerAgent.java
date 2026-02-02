package tech.kayys.wayang.agent.type;

import java.util.Set;

import tech.kayys.wayang.agent.AgentCapability;
import tech.kayys.wayang.agent.AgentRole;
import tech.kayys.wayang.agent.planner.PlanningStrategy;

/**
 * Planner Agent - Strategic planning and task decomposition
 */
public record PlannerAgent(
        PlanningStrategy strategy,
        int maxPlanDepth,
        boolean allowReplanning) implements AgentType {

    @Override
    public String typeName() {
        return "PLANNER_AGENT";
    }

    @Override
    public Set<AgentCapability> requiredCapabilities() {
        return Set.of(
                AgentCapability.REASONING,
                AgentCapability.PLANNING,
                AgentCapability.DECOMPOSITION);
    }

    @Override
    public AgentRole role() {
        return AgentRole.PLANNER;
    }
}
