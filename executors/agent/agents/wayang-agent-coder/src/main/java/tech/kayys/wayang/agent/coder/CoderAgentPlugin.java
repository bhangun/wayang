package tech.kayys.wayang.agent.coder;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.agent.AgentType;
import tech.kayys.wayang.agent.plugin.AgentPlugin;

@ApplicationScoped
public class CoderAgentPlugin implements AgentPlugin {

    @Override
    public String id() {
        return "agent-coder";
    }

    @Override
    public String name() {
        return "Coder Agent";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String description() {
        return "Responsible for generating and reviewing code.";
    }

    @Override
    public AgentType getAgentType() {
        return AgentType.CODER;
    }
}
