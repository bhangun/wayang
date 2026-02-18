package tech.kayys.wayang.agent.planner;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.agent.AgentType;
import tech.kayys.wayang.agent.plugin.AgentPlugin;

@ApplicationScoped
public class PlannerAgentPlugin implements AgentPlugin {

    @Override
    public String id() {
        return "agent-planner";
    }

    @Override
    public String name() {
        return "Planner Agent";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String description() {
        return "Responsible for breaking down goals into tasks.";
    }

    @Override
    public AgentType getAgentType() {
        return AgentType.PLANNER;
    }
}
