package tech.kayys.wayang.integration.designer;

import java.util.Map;

public record AddNodeRequest(
        String nodeType,
        String label,
        Position position,
        Map<String, Object> configuration) {
}
