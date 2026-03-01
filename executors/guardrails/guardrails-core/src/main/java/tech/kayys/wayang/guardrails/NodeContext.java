package tech.kayys.wayang.guardrails;

import java.util.Map;

public record NodeContext(
        String tenantId,
        Map<String, Object> inputs,
        NodeMetadata metadata) {

    public record NodeMetadata(String userId) {
    }
}
