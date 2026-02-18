package tech.kayys.wayang.control.dto.designer;

import java.util.List;

/**
 * Result of integration route design validation.
 */
public record DesignerValidationResult(
        boolean isValid,
        List<DesignerValidationIssue> issues) {
}
