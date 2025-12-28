package tech.kayys.wayang.engine;

/**
 * Unified request that can handle any workflow type.
 * The engine auto-detects type based on configuration.
 */
public class UnifiedWorkflowRequest extends BaseWorkflowRequest {
    private String workflowType; // "agentic", "integration", "business"
    private AgentConfig agentConfig;
    private IntegrationConfig integrationConfig;
    private BusinessConfig businessConfig;
    private OrchestrationConfig orchestration;
    private BatchConfig batchConfig;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final UnifiedWorkflowRequest request = new UnifiedWorkflowRequest();

        public Builder workflowId(String workflowId) {
            request.workflowId = workflowId;
            return this;
        }

        public Builder workflowType(String type) {
            request.workflowType = type;
            return this;
        }

        public Builder tenantId(String tenantId) {
            request.tenantId = tenantId;
            return this;
        }

        public Builder agentConfig(AgentConfig config) {
            request.agentConfig = config;
            request.workflowType = "agentic";
            return this;
        }

        public Builder integrationConfig(IntegrationConfig config) {
            request.integrationConfig = config;
            request.workflowType = "integration";
            return this;
        }

        public Builder businessConfig(BusinessConfig config) {
            request.businessConfig = config;
            request.workflowType = "business";
            return this;
        }

        public Builder input(String key, Object value) {
            request.inputs.put(key, value);
            return this;
        }

        public UnifiedWorkflowRequest build() {
            return request;
        }
    }

    // Getters and setters
    public String getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(String type) {
        this.workflowType = type;
    }

    public AgentConfig getAgentConfig() {
        return agentConfig;
    }

    public void setAgentConfig(AgentConfig config) {
        this.agentConfig = config;
    }

    public IntegrationConfig getIntegrationConfig() {
        return integrationConfig;
    }

    public void setIntegrationConfig(IntegrationConfig config) {
        this.integrationConfig = config;
    }

    public BusinessConfig getBusinessConfig() {
        return businessConfig;
    }

    public void setBusinessConfig(BusinessConfig config) {
        this.businessConfig = config;
    }

    public OrchestrationConfig getOrchestration() {
        return orchestration;
    }

    public void setOrchestration(OrchestrationConfig config) {
        this.orchestration = config;
    }

    public BatchConfig getBatchConfig() {
        return batchConfig;
    }

    public void setBatchConfig(BatchConfig config) {
        this.batchConfig = config;
    }
}
