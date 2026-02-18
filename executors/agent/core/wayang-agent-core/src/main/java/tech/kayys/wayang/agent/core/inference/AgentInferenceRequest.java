package tech.kayys.wayang.agent.core.inference;

import java.util.HashMap;
import java.util.Map;

/**
 * Agent-specific inference request wrapper.
 */
public class AgentInferenceRequest {
    private String systemPrompt;
    private String userPrompt;
    private String preferredProvider;
    private Double temperature = 0.7;
    private Integer maxTokens = 2048;
    private String model;
    private Map<String, Object> additionalParams = new HashMap<>();
    private String agentId;
    private Boolean useMemory = false;
    private Boolean stream = false;

    public AgentInferenceRequest() {
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String getUserPrompt() {
        return userPrompt;
    }

    public void setUserPrompt(String userPrompt) {
        this.userPrompt = userPrompt;
    }

    public String getPreferredProvider() {
        return preferredProvider;
    }

    public void setPreferredProvider(String preferredProvider) {
        this.preferredProvider = preferredProvider;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Map<String, Object> getAdditionalParams() {
        return additionalParams;
    }

    public void setAdditionalParams(Map<String, Object> additionalParams) {
        this.additionalParams = additionalParams;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public Boolean getUseMemory() {
        return useMemory;
    }

    public void setUseMemory(Boolean useMemory) {
        this.useMemory = useMemory;
    }

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AgentInferenceRequest request = new AgentInferenceRequest();

        public Builder systemPrompt(String systemPrompt) {
            request.setSystemPrompt(systemPrompt);
            return this;
        }

        public Builder userPrompt(String userPrompt) {
            request.setUserPrompt(userPrompt);
            return this;
        }

        public Builder preferredProvider(String preferredProvider) {
            request.setPreferredProvider(preferredProvider);
            return this;
        }

        public Builder temperature(Double temperature) {
            request.setTemperature(temperature);
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            request.setMaxTokens(maxTokens);
            return this;
        }

        public Builder model(String model) {
            request.setModel(model);
            return this;
        }

        public Builder additionalParams(Map<String, Object> additionalParams) {
            request.setAdditionalParams(additionalParams);
            return this;
        }

        public Builder agentId(String agentId) {
            request.setAgentId(agentId);
            return this;
        }

        public Builder useMemory(Boolean useMemory) {
            request.setUseMemory(useMemory);
            return this;
        }

        public Builder stream(Boolean stream) {
            request.setStream(stream);
            return this;
        }

        public AgentInferenceRequest build() {
            return request;
        }
    }
}
