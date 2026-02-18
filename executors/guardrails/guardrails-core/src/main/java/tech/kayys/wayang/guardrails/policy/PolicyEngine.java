package tech.kayys.wayang.guardrails.policy;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import dev.cel.common.CelAbstractSyntaxTree;
import dev.cel.common.CelValidationException;
import dev.cel.common.types.SimpleType;
import dev.cel.compiler.CelCompiler;
import dev.cel.compiler.CelCompilerFactory;
import dev.cel.runtime.CelRuntime;
import dev.cel.runtime.CelRuntimeFactory;
import tech.kayys.wayang.guardrails.detector.CheckPhase;
import tech.kayys.wayang.guardrails.plugin.GuardrailDetectorPlugin;
import tech.kayys.wayang.guardrails.plugin.GuardrailPolicyPlugin;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class PolicyEngine {

    @Inject
    PolicyRepository policyRepository;
    
    @Inject
    Instance<GuardrailPolicyPlugin> policyPlugins;

    private final CelCompiler celCompiler;
    private final CelRuntime celRuntime;

    public PolicyEngine() {
        this.celCompiler = CelCompilerFactory.standardCelCompilerBuilder()
                .addVar("input", SimpleType.DYN)
                .addVar("context", SimpleType.DYN)
                .addVar("tenant", SimpleType.STRING)
                .addVar("user", SimpleType.STRING)
                .build();

        this.celRuntime = CelRuntimeFactory.standardCelRuntimeBuilder().build();
    }

    public Uni<PolicyEvaluationResult> evaluatePolicies(
            NodeContext context,
            tech.kayys.wayang.guardrails.detector.CheckPhase phase) {
        return Uni.combine().all()
                .unis(
                    evaluateTraditionalPolicies(context, convertCheckPhase(phase)),
                    evaluatePluginPolicies(context, phase)
                )
                .combinedWith(results -> {
                    List<PolicyCheckResult> traditionalResults = (List<PolicyCheckResult>) results.get(0);
                    List<PolicyCheckResult> pluginResults = (List<PolicyCheckResult>) results.get(1);
                    
                    List<PolicyCheckResult> allResults = new ArrayList<>();
                    allResults.addAll(traditionalResults);
                    allResults.addAll(pluginResults);
                    
                    return aggregateResults(allResults);
                });
    }

    private Uni<List<PolicyCheckResult>> evaluateTraditionalPolicies(
            NodeContext context,
            CheckPhase phase) {
        return policyRepository.findActivePolices(context.tenantId(), phase)
                .map(policies -> {
                    List<Uni<PolicyCheckResult>> evaluations = policies.stream()
                            .map(policy -> evaluatePolicy(policy, context))
                            .toList();

                    return Uni.join().all(evaluations).andFailFast().collectItems().asList();
                })
                .flatMap(uni -> uni);
    }

    private Uni<List<PolicyCheckResult>> evaluatePluginPolicies(
            NodeContext context,
            tech.kayys.wayang.guardrails.detector.CheckPhase phase) {
        
        List<GuardrailPolicyPlugin> applicablePlugins = getPolicyPluginsForPhase(phase);
        
        if (applicablePlugins.isEmpty()) {
            return Uni.createFrom().item(new ArrayList<>());
        }
        
        List<Uni<PolicyCheckResult>> pluginEvaluations = applicablePlugins.stream()
                .map(plugin -> plugin.evaluate(context))
                .collect(Collectors.toList());
                
        return Uni.join().all(pluginEvaluations).collectItems().asList();
    }
    
    private List<GuardrailPolicyPlugin> getPolicyPluginsForPhase(tech.kayys.wayang.guardrails.detector.CheckPhase phase) {
        List<GuardrailPolicyPlugin> plugins = new ArrayList<>();
        
        if (policyPlugins.isResolvable()) {
            for (GuardrailPolicyPlugin plugin : policyPlugins) {
                for (GuardrailDetectorPlugin.CheckPhase pluginPhase : plugin.applicablePhases()) {
                    if (convertToInternalPhase(pluginPhase) == phase) {
                        plugins.add(plugin);
                        break;
                    }
                }
            }
        }
        
        return plugins;
    }
    
    private CheckPhase convertCheckPhase(tech.kayys.wayang.guardrails.detector.CheckPhase externalPhase) {
        return switch (externalPhase) {
            case PRE_EXECUTION -> CheckPhase.PRE_EXECUTION;
            case POST_EXECUTION -> CheckPhase.POST_EXECUTION;
        };
    }
    
    private tech.kayys.wayang.guardrails.detector.CheckPhase convertToInternalPhase(GuardrailDetectorPlugin.CheckPhase pluginPhase) {
        return switch (pluginPhase) {
            case PRE_EXECUTION -> tech.kayys.wayang.guardrails.detector.CheckPhase.PRE_EXECUTION;
            case POST_EXECUTION -> tech.kayys.wayang.guardrails.detector.CheckPhase.POST_EXECUTION;
        };
    }

    private Uni<PolicyCheckResult> evaluatePolicy(Policy policy, NodeContext context) {
        try {
            CelAbstractSyntaxTree ast = celCompiler.compile(policy.expression()).getAst();

            Map<String, Object> celContext = Map.of(
                    "input", context.inputs(),
                    "context", context,
                    "tenant", context.tenantId(),
                    "user", context.metadata().userId());

            Object result = celRuntime.createProgram(ast).eval(celContext);

            boolean allowed = Boolean.TRUE.equals(result);

            return Uni.createFrom().item(new PolicyCheckResult(
                    policy.id(),
                    policy.name(),
                    allowed,
                    allowed ? null : policy.denyMessage()));

        } catch (CelValidationException e) {
            return Uni.createFrom().failure(
                    new PolicyEvaluationException("Invalid CEL expression in policy: " + policy.id(), e));
        }
    }

    private PolicyEvaluationResult aggregateResults(List<PolicyCheckResult> results) {
        List<PolicyCheckResult> violations = results.stream()
                .filter(r -> !r.allowed())
                .toList();

        if (violations.isEmpty()) {
            return PolicyEvaluationResult.allowed();
        }

        return PolicyEvaluationResult.denied(
                violations.get(0).denyMessage(),
                violations.get(0).policyId());
    }
}