package tech.kayys.wayang.communication.protocol;

import java.util.Map;

public record ProtocolContext(
        String tenantId,
        Map<String, Object> attributes
) {

    public ProtocolContext {
        attributes = attributes == null
                ? Map.of()
                : Map.copyOf(attributes);
    }

    public static ProtocolContext of(String tenantId) {
        return new ProtocolContext(tenantId, Map.of());
    }

    public static ProtocolContext empty() {
        return new ProtocolContext(
                null,
                Map.of()
        );
    }
}
