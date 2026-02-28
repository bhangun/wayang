package tech.kayys.wayang.agent.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Configuration for an Orchestrator Agent.
 */
public class OrchestratorAgentConfig extends AgentConfig {

    @JsonProperty(value = "orchestrationType", defaultValue = "COLLABORATIVE")
    private String orchestrationType;

    @JsonProperty(value = "coordinationStrategy", defaultValue = "CENTRALIZED")
    private String coordinationStrategy;

    @JsonProperty("agentTasks")
    private List<Map<String, Object>> agentTasks;

    public OrchestratorAgentConfig() {
        super();
    }

    public String getOrchestrationType() {
        return orchestrationType;
    }

    public void setOrchestrationType(String orchestrationType) {
        this.orchestrationType = orchestrationType;
    }

    public String getCoordinationStrategy() {
        return coordinationStrategy;
    }

    public void setCoordinationStrategy(String coordinationStrategy) {
        this.coordinationStrategy = coordinationStrategy;
    }

    public List<Map<String, Object>> getAgentTasks() {
        return agentTasks;
    }

    public void setAgentTasks(List<Map<String, Object>> agentTasks) {
        this.agentTasks = agentTasks;
    }
}
