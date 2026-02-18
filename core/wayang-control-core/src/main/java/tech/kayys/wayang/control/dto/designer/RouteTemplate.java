package tech.kayys.wayang.control.dto.designer;

import java.util.List;

/**
 * Template for creating new routes.
 */
public record RouteTemplate(
        String templateId,
        String name,
        String description,
        String category,
        List<String> tags) {
}
