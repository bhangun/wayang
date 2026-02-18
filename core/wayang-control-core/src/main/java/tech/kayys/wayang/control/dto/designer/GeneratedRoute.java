package tech.kayys.wayang.control.dto.designer;

/**
 * Representation of a generated integration route code/definition.
 */
public record GeneratedRoute(
        String routeId,
        String format, // e.g. "xml", "yaml", "java"
        String definition,
        String status) {
}
