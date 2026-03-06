package tech.kayys.wayang.agent.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Configuration for a Planner Agent. */
public class PlannerAgentConfig extends AgentConfig {
    @JsonProperty("goal")
    private String goal;

    @JsonProperty("objective")
    private String objective;

    @JsonProperty("instruction")
    private String instruction;

    @JsonProperty("strategy")
    private String strategy;

    @JsonProperty("preferredProvider")
    private String preferredProvider;

    public PlannerAgentConfig() {
        super();
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public String getPreferredProvider() {
        return preferredProvider;
    }

    public void setPreferredProvider(String preferredProvider) {
        this.preferredProvider = preferredProvider;
    }
}
