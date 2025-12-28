package tech.kayys.wayang.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent configuration for agentic workflows.
 */
public class AgentConfig {
    private String primaryAgent;
    private List<String> toolsEnabled = new ArrayList<>();
    private OrchestrationStrategy orchestrationStrategy = OrchestrationStrategy.SEQUENTIAL;
    private Integer maxIterations = 10;
    private boolean ragEnabled = false;
    private String memoryNamespace;
    private boolean selfHealingEnabled = true;
    private Map<String, Object> modelParams = new HashMap<>();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AgentConfig config = new AgentConfig();

        public Builder primaryAgent(String agent) {
            config.primaryAgent = agent;
            return this;
        }

        public Builder toolsEnabled(List<String> tools) {
            config.toolsEnabled = tools;
            return this;
        }

        public Builder orchestrationStrategy(OrchestrationStrategy strategy) {
            config.orchestrationStrategy = strategy;
            return this;
        }

        public Builder maxIterations(int max) {
            config.maxIterations = max;
            return this;
        }

        public Builder ragEnabled(boolean enabled) {
            config.ragEnabled = enabled;
            return this;
        }

        public Builder memoryNamespace(String namespace) {
            config.memoryNamespace = namespace;
            return this;
        }

        public Builder selfHealingEnabled(boolean enabled) {
            config.selfHealingEnabled = enabled;
            return this;
        }

        public Builder modelParam(String key, Object value) {
            config.modelParams.put(key, value);
            return this;
        }

        public AgentConfig build() {
            return config;
        }
    }

    // Getters and setters
    public String getPrimaryAgent() {
        return primaryAgent;
    }

    public void setPrimaryAgent(String primaryAgent) {
        this.primaryAgent = primaryAgent;
    }

    public List<String> getToolsEnabled() {
        return toolsEnabled;
    }

    public void setToolsEnabled(List<String> toolsEnabled) {
        this.toolsEnabled = toolsEnabled;
    }

    public OrchestrationStrategy getOrchestrationStrategy() {
        return orchestrationStrategy;
    }

    public void setOrchestrationStrategy(OrchestrationStrategy strategy) {
        this.orchestrationStrategy = strategy;
    }

    public Integer getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(Integer maxIterations) {
        this.maxIterations = maxIterations;
    }

    public boolean isRagEnabled() {
        return ragEnabled;
    }

    public void setRagEnabled(boolean ragEnabled) {
        this.ragEnabled = ragEnabled;
    }

    public String getMemoryNamespace() {
        return memoryNamespace;
    }

    public void setMemoryNamespace(String memoryNamespace) {
        this.memoryNamespace = memoryNamespace;
    }

    public boolean isSelfHealingEnabled() {
        return selfHealingEnabled;
    }

    public void setSelfHealingEnabled(boolean selfHealingEnabled) {
        this.selfHealingEnabled = selfHealingEnabled;
    }

    public Map<String, Object> getModelParams() {
        return modelParams;
    }

    public void setModelParams(Map<String, Object> modelParams) {
        this.modelParams = modelParams;
    }
}
