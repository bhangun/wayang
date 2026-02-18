package tech.kayys.wayang.agent.orchestrator;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.agent.AgentType;
import tech.kayys.wayang.agent.plugin.AgentPlugin;

@ApplicationScoped
public class OrchestratorAgentPlugin implements AgentPlugin {

    @Override
    public String id() {
        return "agent-orchestrator";
    }

    @Override
    public String name() {
        return "Orchestrator Agent";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String description() {
        return "Coordinates multiple agents to achieve a goal.";
    }

    @Override
    public AgentType getAgentType() {
        return AgentType.ORCHESTRATOR;
    }
}
