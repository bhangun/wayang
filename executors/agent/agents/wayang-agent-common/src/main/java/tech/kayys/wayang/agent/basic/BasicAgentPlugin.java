package tech.kayys.wayang.agent.basic;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.agent.AgentType;
import tech.kayys.wayang.agent.plugin.AgentPlugin;

@ApplicationScoped
public class BasicAgentPlugin implements AgentPlugin {

    @Override
    public String id() {
        return "agent-common";
    }

    @Override
    public String name() {
        return "Common Agent";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String description() {
        return "Responsible for general purpose tasks.";
    }

    @Override
    public AgentType getAgentType() {
        return AgentType.COMMON;
    }
}
