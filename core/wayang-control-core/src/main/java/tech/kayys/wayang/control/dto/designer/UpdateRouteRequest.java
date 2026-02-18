package tech.kayys.wayang.control.dto.designer;

import java.util.Map;

/**
 * Request to update an integration route's basic properties.
 */
public record UpdateRouteRequest(
        String name,
        String description,
        String category,
        Map<String, Object> metadata) {
}
