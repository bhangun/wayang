package tech.kayys.wayang.control.integration.designer;

import java.util.List;

public record ValidationResult(
        String routeId,
        boolean valid,
        List<ValidationIssue> issues) {
}
