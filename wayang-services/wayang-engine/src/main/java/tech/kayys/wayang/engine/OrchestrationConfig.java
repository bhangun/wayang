package tech.kayys.wayang.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestration configuration for complex agent workflows.
 */
public class OrchestrationConfig {
    private OrchestrationStrategy strategy;
    private List<String> targetAgents = new ArrayList<>();
    private String decisionPolicy;
    private Integer maxConcurrency = 4;
    private Long timeoutMs = 60000L;
    private String failureStrategy = "retry";
    private boolean emitPlan = true;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final OrchestrationConfig config = new OrchestrationConfig();

        public Builder strategy(OrchestrationStrategy strategy) {
            config.strategy = strategy;
            return this;
        }

        public Builder targetAgents(List<String> agents) {
            config.targetAgents = agents;
            return this;
        }

        public Builder decisionPolicy(String policy) {
            config.decisionPolicy = policy;
            return this;
        }

        public Builder maxConcurrency(int max) {
            config.maxConcurrency = max;
            return this;
        }

        public Builder timeout(long ms) {
            config.timeoutMs = ms;
            return this;
        }

        public Builder failureStrategy(String strategy) {
            config.failureStrategy = strategy;
            return this;
        }

        public Builder emitPlan(boolean emit) {
            config.emitPlan = emit;
            return this;
        }

        public OrchestrationConfig build() {
            return config;
        }
    }

    // Getters and setters
    public OrchestrationStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(OrchestrationStrategy strategy) {
        this.strategy = strategy;
    }

    public List<String> getTargetAgents() {
        return targetAgents;
    }

    public void setTargetAgents(List<String> targetAgents) {
        this.targetAgents = targetAgents;
    }

    public String getDecisionPolicy() {
        return decisionPolicy;
    }

    public void setDecisionPolicy(String decisionPolicy) {
        this.decisionPolicy = decisionPolicy;
    }

    public Integer getMaxConcurrency() {
        return maxConcurrency;
    }

    public void setMaxConcurrency(Integer maxConcurrency) {
        this.maxConcurrency = maxConcurrency;
    }

    public Long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(Long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public String getFailureStrategy() {
        return failureStrategy;
    }

    public void setFailureStrategy(String failureStrategy) {
        this.failureStrategy = failureStrategy;
    }

    public boolean isEmitPlan() {
        return emitPlan;
    }

    public void setEmitPlan(boolean emitPlan) {
        this.emitPlan = emitPlan;
    }
}