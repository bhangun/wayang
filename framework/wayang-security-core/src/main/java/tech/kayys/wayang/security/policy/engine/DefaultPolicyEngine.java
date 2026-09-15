package tech.kayys.wayang.security.policy.engine;

import tech.kayys.wayang.security.authz.AuthorizationRequest;
import tech.kayys.wayang.security.policy.Policy;
import tech.kayys.wayang.security.policy.PolicyDecision;
import tech.kayys.wayang.security.policy.PolicyEffect;
import tech.kayys.wayang.security.policy.PolicyRule;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * First-applicable policy engine: evaluates rules in priority order (highest first),
 * returns the first matching rule's decision. Defaults to DENY if no rule matches.
 */
public final class DefaultPolicyEngine implements PolicyEngine {

    private final List<Policy> policies;

    public DefaultPolicyEngine(List<Policy> policies) {
        this.policies = policies == null ? List.of() : List.copyOf(policies);
    }

    public DefaultPolicyEngine() {
        this(List.of());
    }

    @Override
    public PolicyDecision evaluate(AuthorizationRequest request) {
        Objects.requireNonNull(request, "request");

        String subjectId   = request.securityContext().principal().id();
        String capability  = request.capability();
        String action      = request.action();

        // Flatten all rules across all policies, sorted by priority (descending)
        List<PolicyRule> allRules = policies.stream()
                .flatMap(p -> p.rules().stream())
                .sorted(Comparator.comparingInt(PolicyRule::priority).reversed())
                .toList();

        for (PolicyRule rule : allRules) {
            if (rule.matches(subjectId, capability, action)) {
                return rule.effect() == PolicyEffect.ALLOW
                        ? PolicyDecision.allow(rule.id(), "Matched rule: " + rule.id(), rule.obligations())
                        : PolicyDecision.deny(rule.id(), "Denied by rule: " + rule.id());
            }
        }

        // Deny by default — secure-by-default stance
        return PolicyDecision.deny("default-deny", "No matching policy rule found");
    }

    /** Returns a new engine with an additional policy appended. */
    public DefaultPolicyEngine withPolicy(Policy policy) {
        List<Policy> updated = new ArrayList<>(policies);
        updated.add(policy);
        return new DefaultPolicyEngine(updated);
    }
}
