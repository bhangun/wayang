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

    @JsonProperty("maxIterations")
    private Integer maxIterations;

    @JsonProperty("maxDelegations")
    private Integer maxDelegations;

    @JsonProperty("maxLatencyMs")
    private Long maxLatencyMs;

    @JsonProperty("maxAgentLatencyMs")
    private Long maxAgentLatencyMs;

    @JsonProperty("maxRetriesPerDelegation")
    private Integer maxRetriesPerDelegation;

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

    public Integer getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(Integer maxIterations) {
        this.maxIterations = maxIterations;
    }

    public Integer getMaxDelegations() {
        return maxDelegations;
    }

    public void setMaxDelegations(Integer maxDelegations) {
        this.maxDelegations = maxDelegations;
    }

    public Long getMaxLatencyMs() {
        return maxLatencyMs;
    }

    public void setMaxLatencyMs(Long maxLatencyMs) {
        this.maxLatencyMs = maxLatencyMs;
    }

    public Long getMaxAgentLatencyMs() {
        return maxAgentLatencyMs;
    }

    public void setMaxAgentLatencyMs(Long maxAgentLatencyMs) {
        this.maxAgentLatencyMs = maxAgentLatencyMs;
    }

    public Integer getMaxRetriesPerDelegation() {
        return maxRetriesPerDelegation;
    }

    public void setMaxRetriesPerDelegation(Integer maxRetriesPerDelegation) {
        this.maxRetriesPerDelegation = maxRetriesPerDelegation;
    }
}
