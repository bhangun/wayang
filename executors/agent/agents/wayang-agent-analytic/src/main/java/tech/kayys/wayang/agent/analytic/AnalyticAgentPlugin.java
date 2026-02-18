package tech.kayys.wayang.agent.analytic;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.agent.AgentType;
import tech.kayys.wayang.agent.plugin.AgentPlugin;

@ApplicationScoped
public class AnalyticAgentPlugin implements AgentPlugin {

    @Override
    public String id() {
        return "agent-analytic";
    }

    @Override
    public String name() {
        return "Analytic Agent";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String description() {
        return "Responsible for data analysis and insights.";
    }

    @Override
    public AgentType getAgentType() {
        return AgentType.ANALYTICS;
    }
}
