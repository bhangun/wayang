package tech.kayys.wayang.control.integration.designer;

public record ValidationIssue(
        String code,
        String severity,
        String message,
        String nodeId) {
}
