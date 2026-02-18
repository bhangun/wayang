package tech.kayys.wayang.integration.designer;

import java.util.Map;

public record UpdateRouteRequest(
        String name,
        String description,
        Map<String, Object> metadata) {
}
