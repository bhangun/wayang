package tech.kayys.wayang.engine;

/**
 * Request for executing business automation workflows with human approval.
 * 
 * Example:
 * 
 * <pre>
 * BusinessWorkflowRequest request = BusinessWorkflowRequest.builder()
 *         .workflowId("expense-approval")
 *         .tenantId("acme-corp")
 *         .triggeredBy("user:john.doe")
 *         .businessConfig(BusinessConfig.builder()
 *                 .approvalChain(List.of("manager", "finance-lead", "cfo"))
 *                 .slaHours(48)
 *                 .escalationPolicy(EscalationPolicy.AUTO_APPROVE_ON_TIMEOUT)
 *                 .formTemplate("expense-form-v2")
 *                 .notificationChannels(List.of("email", "slack"))
 *                 .build())
 *         .input("expenseReport", reportData)
 *         .input("amount", 1500.00)
 *         .build();
 * </pre>
 */
public class BusinessWorkflowRequest extends BaseWorkflowRequest {
    private BusinessConfig businessConfig;
    private boolean requiresApproval = true;
    private String priority = "NORMAL";

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final BusinessWorkflowRequest request = new BusinessWorkflowRequest();

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

        public Builder businessConfig(BusinessConfig config) {
            request.businessConfig = config;
            return this;
        }

        public Builder requiresApproval(boolean required) {
            request.requiresApproval = required;
            return this;
        }

        public Builder priority(String priority) {
            request.priority = priority;
            return this;
        }

        public Builder input(String key, Object value) {
            request.inputs.put(key, value);
            return this;
        }

        public BusinessWorkflowRequest build() {
            if (request.workflowId == null) {
                throw new IllegalStateException("workflowId is required");
            }
            return request;
        }
    }

    // Getters and setters
    public BusinessConfig getBusinessConfig() {
        return businessConfig;
    }

    public void setBusinessConfig(BusinessConfig config) {
        this.businessConfig = config;
    }

    public boolean isRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(boolean required) {
        this.requiresApproval = required;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
