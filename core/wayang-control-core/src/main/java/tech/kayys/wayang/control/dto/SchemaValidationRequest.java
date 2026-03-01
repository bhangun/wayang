package tech.kayys.wayang.control.dto;

import java.util.Map;

/**
 * Request to validate data against a JSON schema.
 */
public record SchemaValidationRequest(
        String schema,
        Map<String, Object> data) {
}
