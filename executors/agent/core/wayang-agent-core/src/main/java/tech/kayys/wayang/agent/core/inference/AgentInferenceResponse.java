package tech.kayys.wayang.agent.core.inference;

import java.time.Duration;
import java.util.Map;

/**
 * Agent-specific inference response wrapper.
 */
public class AgentInferenceResponse {
    private String content;
    private String providerUsed;
    private String modelUsed;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private Duration latency;
    private Boolean cached = false;
    private String error;
    private Map<String, Object> metadata;

    public AgentInferenceResponse() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getProviderUsed() {
        return providerUsed;
    }

    public void setProviderUsed(String providerUsed) {
        this.providerUsed = providerUsed;
    }

    public String getModelUsed() {
        return modelUsed;
    }

    public void setModelUsed(String modelUsed) {
        this.modelUsed = modelUsed;
    }

    public Integer getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(Integer promptTokens) {
        this.promptTokens = promptTokens;
    }

    public Integer getCompletionTokens() {
        return completionTokens;
    }

    public void setCompletionTokens(Integer completionTokens) {
        this.completionTokens = completionTokens;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(Integer totalTokens) {
        this.totalTokens = totalTokens;
    }

    public Duration getLatency() {
        return latency;
    }

    public void setLatency(Duration latency) {
        this.latency = latency;
    }

    public Boolean getCached() {
        return cached;
    }

    public void setCached(Boolean cached) {
        this.cached = cached;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public boolean isSuccess() {
        return error == null;
    }

    public boolean isError() {
        return error != null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AgentInferenceResponse response = new AgentInferenceResponse();

        public Builder content(String content) {
            response.setContent(content);
            return this;
        }

        public Builder providerUsed(String providerUsed) {
            response.setProviderUsed(providerUsed);
            return this;
        }

        public Builder modelUsed(String modelUsed) {
            response.setModelUsed(modelUsed);
            return this;
        }

        public Builder promptTokens(Integer promptTokens) {
            response.setPromptTokens(promptTokens);
            return this;
        }

        public Builder completionTokens(Integer completionTokens) {
            response.setCompletionTokens(completionTokens);
            return this;
        }

        public Builder totalTokens(Integer totalTokens) {
            response.setTotalTokens(totalTokens);
            return this;
        }

        public Builder latency(Duration latency) {
            response.setLatency(latency);
            return this;
        }

        public Builder cached(Boolean cached) {
            response.setCached(cached);
            return this;
        }

        public Builder error(String error) {
            response.setError(error);
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            response.setMetadata(metadata);
            return this;
        }

        public AgentInferenceResponse build() {
            return response;
        }
    }
}
