package tech.kayys.wayang.knowledge.audit;

public record KnowledgeAuditPolicyConfig(
        KnowledgeAuditFailureMode failureMode,
        boolean requireTenant,
        boolean requireExecutionId,
        boolean requireAgentId
) {

    public static KnowledgeAuditPolicyConfig strict() {
        return new KnowledgeAuditPolicyConfig(
                KnowledgeAuditFailureMode.FAIL_CLOSED,
                true,
                true,
                true
        );
    }

    public static KnowledgeAuditPolicyConfig relaxed() {
        return new KnowledgeAuditPolicyConfig(
                KnowledgeAuditFailureMode.FAIL_OPEN,
                true,
                false,
                false
        );
    }
}
