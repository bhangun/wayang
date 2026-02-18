package tech.kayys.wayang.agent.evaluator;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.agent.AgentType;
import tech.kayys.wayang.agent.plugin.AgentPlugin;

@ApplicationScoped
public class EvaluatorAgentPlugin implements AgentPlugin {

    @Override
    public String id() {
        return "agent-evaluator";
    }

    @Override
    public String name() {
        return "Evaluator Agent";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String description() {
        return "Responsible for evaluating outputs and providing feedback.";
    }

    @Override
    public AgentType getAgentType() {
        return AgentType.EVALUATOR;
    }
}
