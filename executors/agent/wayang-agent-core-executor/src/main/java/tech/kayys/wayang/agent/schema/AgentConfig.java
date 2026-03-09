package tech.kayys.wayang.agent.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import tech.kayys.wayang.schema.common.Metadata;
import tech.kayys.wayang.schema.common.RateLimit;
import tech.kayys.wayang.schema.common.RetryPolicy;
import tech.kayys.wayang.schema.common.CircuitBreaker;
import java.util.Map;

/**
 * Configuration for an AI agent in the system.
 */
public class AgentConfig {
    @JsonProperty("metadata")
    private Metadata metadata;

    @JsonProperty("model")
    private String model;

    @JsonProperty("temperature")
    private Double temperature;

    @JsonProperty("maxTokens")
    private Integer maxTokens;

    @JsonProperty("topP")
    private Double topP;

    @JsonProperty("frequencyPenalty")
    private Double frequencyPenalty;

    @JsonProperty("presencePenalty")
    private Double presencePenalty;

    @JsonProperty("stopSequences")
    private String[] stopSequences;

    @JsonProperty("rateLimit")
    private RateLimit rateLimit;

    @JsonProperty("retryPolicy")
    private RetryPolicy retryPolicy;

    @JsonProperty("circuitBreaker")
    private CircuitBreaker circuitBreaker;

    @JsonProperty("parameters")
    private Map<String, Object> parameters;

    public AgentConfig() {
    }

    public AgentConfig(Metadata metadata, String model, Double temperature, Integer maxTokens,
            Double topP, Double frequencyPenalty, Double presencePenalty,
            String[] stopSequences, RateLimit rateLimit, RetryPolicy retryPolicy,
            CircuitBreaker circuitBreaker, Map<String, Object> parameters) {
        this.metadata = metadata;
        this.model = model;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.topP = topP;
        this.frequencyPenalty = frequencyPenalty;
        this.presencePenalty = presencePenalty;
        this.stopSequences = stopSequences;
        this.rateLimit = rateLimit;
        this.retryPolicy = retryPolicy;
        this.circuitBreaker = circuitBreaker;
        this.parameters = parameters;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
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

    public Double getTopP() {
        return topP;
    }

    public void setTopP(Double topP) {
        this.topP = topP;
    }

    public Double getFrequencyPenalty() {
        return frequencyPenalty;
    }

    public void setFrequencyPenalty(Double frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
    }

    public Double getPresencePenalty() {
        return presencePenalty;
    }

    public void setPresencePenalty(Double presencePenalty) {
        this.presencePenalty = presencePenalty;
    }

    public String[] getStopSequences() {
        return stopSequences;
    }

    public void setStopSequences(String[] stopSequences) {
        this.stopSequences = stopSequences;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimit rateLimit) {
        this.rateLimit = rateLimit;
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }

    public void setCircuitBreaker(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
