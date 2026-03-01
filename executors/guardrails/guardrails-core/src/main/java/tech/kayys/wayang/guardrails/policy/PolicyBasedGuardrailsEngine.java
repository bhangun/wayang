package tech.kayys.wayang.guardrails.policy;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.guardrails.*;
import tech.kayys.wayang.guardrails.detector.PIIDetector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PolicyBasedGuardrailsEngine implements GuardrailsEngine {
    @Inject
    PolicyEngine policyEngine;
    @Inject
    PIIDetector piiDetector;
    @Inject
    ContentModerator contentModerator;
    @Inject
    Redactor redactor;

    @Override
    public GuardrailResult preCheck(ExecuteNodeTask task) {
        List<String> violations = new ArrayList<>();

        // Check policies
        tech.kayys.wayang.guardrails.NodeContext context = new tech.kayys.wayang.guardrails.NodeContext(
                task.tenantId(),
                task.inputs(),
                new tech.kayys.wayang.guardrails.NodeContext.NodeMetadata("system"));

        PolicyEvaluationResult policyEvaluationResult = policyEngine.evaluatePolicies(
                context,
                tech.kayys.wayang.guardrails.detector.CheckPhase.PRE_EXECUTION).await().indefinitely();

        if (!policyEvaluationResult.allowed()) {
            violations.add(policyEvaluationResult.policyId());
        }

        return violations.isEmpty() ? GuardrailResult.success()
                : GuardrailResult.failure("Policy violations detected", violations);
    }

    @Override
    public GuardrailResult postCheck(ExecuteNodeTask task, ExecutionResult result) {
        List<String> violations = new ArrayList<>();

        // Content moderation
        ModerationResult modResult = contentModerator.moderate(result.outputs());
        if (!modResult.violations().isEmpty()) {
            violations.addAll(modResult.violations().stream().map(PolicyViolation::code).toList());
        }

        ExecutionResult finalResult = result;
        // PII redaction if needed
        if (modResult.hasPII()) {
            Map<String, Object> redacted = redactor.redact(result.outputs());
            finalResult = new ExecutionResult(redacted, result.metadata());
        }

        return violations.isEmpty() ? GuardrailResult.success().withRedactedContent(finalResult.outputs())
                : GuardrailResult.failure("Post-execution violations detected", violations);
    }

    @Override
    public void registerPolicy(GuardrailPolicy policy) {
        // Implementation for dynamic policy registration
    }

    @Override
    public void updatePolicy(String policyId, GuardrailPolicy policy) {
        // Implementation for dynamic policy update
    }
}