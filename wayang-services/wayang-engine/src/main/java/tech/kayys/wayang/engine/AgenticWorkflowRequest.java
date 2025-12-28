package tech.kayys.wayang.engine;

/**
 * Request for executing agentic workflows with AI-driven decision making.
 * 
 * Example:
 * 
 * <pre>
 * AgenticWorkflowRequest request = AgenticWorkflowRequest.builder()
 *         .workflowId("customer-support-agent")
 *         .tenantId("acme-corp")
 *         .agentConfig(AgentConfig.builder()
 *                 .primaryAgent("support-agent-v2")
 *                 .orchestrationStrategy(OrchestrationStrategy.DYNAMIC)
 *                 .maxIterations(10)
 *                 .toolsEnabled(List.of("knowledge-base", "ticket-system", "email"))
 *                 .ragEnabled(true)
 *                 .memoryNamespace("customer-support")
 *                 .build())
 *         .input("query", "How do I reset my password?")
 *         .input("customerId", "12345")
 *         .build();
 * </pre>
 */
public class AgenticWorkflowRequest extends BaseWorkflowRequest {
    private AgentConfig agentConfig;
    private OrchestrationConfig orchestration;
    private boolean streamingEnabled = false;
    private Double confidenceThreshold = 0.7;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AgenticWorkflowRequest request = new AgenticWorkflowRequest();

        public Builder workflowId(String workflowId) {
            request.workflowId = workflowId;
            return this;
        }

        public Builder workflowVersion(String version) {
            request.workflowVersion = version;
            return this;
        }

        public Builder tenantId(String tenantId) {
            request.tenantId = tenantId;
            return this;
        }

        public Builder triggeredBy(String triggeredBy) {
            request.triggeredBy = triggeredBy;
            return this;
        }

        public Builder agentConfig(AgentConfig config) {
            request.agentConfig = config;
            return this;
        }

        public Builder orchestration(OrchestrationConfig config) {
            request.orchestration = config;
            return this;
        }

        public Builder streamingEnabled(boolean enabled) {
            request.streamingEnabled = enabled;
            return this;
        }

        public Builder confidenceThreshold(double threshold) {
            request.confidenceThreshold = threshold;
            return this;
        }

        public Builder input(String key, Object value) {
            request.inputs.put(key, value);
            return this;
        }

        public Builder metadata(String key, Object value) {
            request.metadata.put(key, value);
            return this;
        }

        public AgenticWorkflowRequest build() {
            if (request.workflowId == null) {
                throw new IllegalStateException("workflowId is required");
            }
            if (request.tenantId == null) {
                throw new IllegalStateException("tenantId is required");
            }
            return request;
        }
    }

    // Getters and setters
    public AgentConfig getAgentConfig() {
        return agentConfig;
    }

    public void setAgentConfig(AgentConfig agentConfig) {
        this.agentConfig = agentConfig;
    }

    public OrchestrationConfig getOrchestration() {
        return orchestration;
    }

    public void setOrchestration(OrchestrationConfig orchestration) {
        this.orchestration = orchestration;
    }

    public boolean isStreamingEnabled() {
        return streamingEnabled;
    }

    public void setStreamingEnabled(boolean streamingEnabled) {
        this.streamingEnabled = streamingEnabled;
    }

    public Double getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public void setConfidenceThreshold(Double confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }
}
