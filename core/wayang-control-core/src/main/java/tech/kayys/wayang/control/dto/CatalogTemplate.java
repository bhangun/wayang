package tech.kayys.wayang.control.dto;

import java.util.List;
import tech.kayys.wayang.domain.CanvasDefinition;

/**
 * Catalog template descriptor for starter workflows.
 */
public record CatalogTemplate(
        String id,
        String name,
        String description,
        String category,
        TemplateType templateType,
        CanvasDefinition canvasDefinition,
        List<String> tags,
        String difficulty) {
}
