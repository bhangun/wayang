package tech.kayys.wayang.engine;

/**
 * Request for executing integration workflows with system-to-system data flow.
 * 
 * Example:
 * 
 * <pre>
 * IntegrationWorkflowRequest request = IntegrationWorkflowRequest.builder()
 *         .workflowId("salesforce-to-erp")
 *         .tenantId("acme-corp")
 *         .integrationConfig(IntegrationConfig.builder()
 *                 .sourceConnector("salesforce-api")
 *                 .targetConnector("sap-erp")
 *                 .transformationRules("mapping-v2.yaml")
 *                 .errorStrategy(ErrorStrategy.DEAD_LETTER_QUEUE)
 *                 .idempotencyKey("invoice-sync-${date}")
 *                 .build())
 *         .batchConfig(BatchConfig.builder()
 *                 .enabled(true)
 *                 .batchSize(1000)
 *                 .parallelism(4)
 *                 .build())
 *         .build();
 * </pre>
 */
public class IntegrationWorkflowRequest extends BaseWorkflowRequest {
    private IntegrationConfig integrationConfig;
    private BatchConfig batchConfig;
    private boolean transactional = false;
    private Long rateLimitPerSecond;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final IntegrationWorkflowRequest request = new IntegrationWorkflowRequest();

        public Builder workflowId(String workflowId) {
            request.workflowId = workflowId;
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

        public Builder integrationConfig(IntegrationConfig config) {
            request.integrationConfig = config;
            return this;
        }

        public Builder batchConfig(BatchConfig config) {
            request.batchConfig = config;
            return this;
        }

        public Builder transactional(boolean transactional) {
            request.transactional = transactional;
            return this;
        }

        public Builder rateLimit(long perSecond) {
            request.rateLimitPerSecond = perSecond;
            return this;
        }

        public Builder input(String key, Object value) {
            request.inputs.put(key, value);
            return this;
        }

        public IntegrationWorkflowRequest build() {
            if (request.workflowId == null) {
                throw new IllegalStateException("workflowId is required");
            }
            return request;
        }
    }

    // Getters and setters
    public IntegrationConfig getIntegrationConfig() {
        return integrationConfig;
    }

    public void setIntegrationConfig(IntegrationConfig config) {
        this.integrationConfig = config;
    }

    public BatchConfig getBatchConfig() {
        return batchConfig;
    }

    public void setBatchConfig(BatchConfig config) {
        this.batchConfig = config;
    }

    public boolean isTransactional() {
        return transactional;
    }

    public void setTransactional(boolean transactional) {
        this.transactional = transactional;
    }

    public Long getRateLimitPerSecond() {
        return rateLimitPerSecond;
    }

    public void setRateLimitPerSecond(Long rateLimit) {
        this.rateLimitPerSecond = rateLimit;
    }

    public Integer getBatchSize() {
        return batchConfig != null ? batchConfig.getBatchSize() : null;
    }
}
