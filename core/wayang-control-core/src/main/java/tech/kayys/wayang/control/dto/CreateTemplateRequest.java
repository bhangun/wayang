package tech.kayys.wayang.control.dto;

import java.util.List;
import tech.kayys.wayang.control.domain.CanvasDefinition;

/**
 * Request to create a new workflow template.
 */
public record CreateTemplateRequest(
        String templateName,
        String description,
        String version,
        TemplateType templateType,
        CanvasDefinition canvasDefinition,
        List<String> tags) {
}
