package tech.kayys.wayang.policy;

import java.util.ArrayList;
import java.util.List;

import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.client.ProductSurfacePolicy;
import tech.kayys.wayang.client.WayangProductCatalog;

public final class SurfacePolicyPreflight {

    private SurfacePolicyPreflight() {
    }

    public static SurfacePolicyAssessment assess(AgentRequest request) {
        AgentRequest normalized = request;
        String surfaceId = normalized.metadata() != null ? (String) normalized.metadata().get("surfaceId") : "";
        ProductSurfacePolicy policy = WayangProductCatalog.policyFor(surfaceId);
        List<String> satisfied = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String key : policy.requiredContextKeys()) {
            if (hasContext(normalized, key)) {
                satisfied.add(key);
            } else {
                missing.add(key);
            }
        }

        List<String> recommendations = new ArrayList<>();
        for (String key : missing) {
            addMissingContextRecommendation(recommendations, key);
        }
        if (policy.memoryPreferred() && !hasContext(normalized, "memory")) {
            recommendations.add("Enable memory context for this surface.");
        }
        if (policy.workspacePreferred() && !hasContext(normalized, "workspace")) {
            addIfAbsent(recommendations, "Attach workspace context with --workspace <path>.");
        }
        if (policy.harnessPreferred() && !hasContext(normalized, "harness")) {
            recommendations.add("Attach planned verification checks with --harness.");
        }
        if (!hasContext(normalized, "workflowId") && policy.workflowPreferred()) {
            addIfAbsent(recommendations, "Set a workflow id with --workflow <id>.");
        }
        if (!hasContext(normalized, "skills") && !policy.suggestedSkills().isEmpty()) {
            recommendations.add("Consider surface skills: " + String.join(", ", policy.suggestedSkills()) + ".");
        }

        return new SurfacePolicyAssessment(
                surfaceId,
                missing.isEmpty(),
                satisfied,
                missing,
                recommendations,
                policy.routingHints());
    }

    private static boolean hasContext(AgentRequest request, String key) {
        if (request.metadata() == null) return false;
        return switch (key) {
            case "surfaceId" -> request.metadata().containsKey("surfaceId");
            case "tenantId" -> request.tenantId() != null && !request.tenantId().isBlank();
            case "workflowId" -> request.metadata().containsKey("workflowId");
            case "workspace" -> Boolean.TRUE.equals(request.parameters().get("workspaceEnabled"));
            case "harness" -> Boolean.TRUE.equals(request.parameters().get("harnessEnabled"));
            case "memory" -> Boolean.TRUE.equals(request.parameters().get("memoryEnabled"));
            case "skills" -> request.parameters().containsKey("skills");
            default -> false;
        };
    }

    private static void addMissingContextRecommendation(List<String> recommendations, String key) {
        switch (key) {
            case "workspace" -> recommendations.add("Attach workspace context with --workspace <path>.");
            case "harness" -> recommendations.add("Attach planned verification checks with --harness.");
            case "workflowId" -> recommendations.add("Set a workflow id with --workflow <id>.");
            case "tenantId" -> recommendations.add("Set a tenant id with --tenant <id>.");
            case "memory" -> recommendations.add("Enable memory context for this surface.");
            default -> recommendations.add("Provide required context key: " + key + ".");
        }
    }

    private static void addIfAbsent(List<String> recommendations, String recommendation) {
        if (!recommendations.contains(recommendation)) {
            recommendations.add(recommendation);
        }
    }
}
