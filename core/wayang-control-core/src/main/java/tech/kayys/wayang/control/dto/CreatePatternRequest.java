package tech.kayys.wayang.control.dto;

import java.util.Map;

/**
 * Request to create a new integration pattern.
 */
public record CreatePatternRequest(
        String name,
        String description,
        EIPPatternType patternType,
        String configurationSchema,
        Map<String, String> metadata) {
}
