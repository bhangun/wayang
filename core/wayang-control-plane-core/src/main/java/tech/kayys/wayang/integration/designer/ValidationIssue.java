package tech.kayys.wayang.integration.designer;

public record ValidationIssue(
        String code,
        String severity,
        String message,
        String nodeId) {
}
