package tech.kayys.wayang.a2a.descriptor;

import java.util.Map;

/**
 * Represents one transport interface advertised in the A2A Agent Card.
 */
public record A2AInterface(
        String url,
        String protocolBinding,
        String protocolVersion,
        Map<String, Object> metadata
) {

    public A2AInterface {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static A2AInterface of(String url, String protocolBinding, String protocolVersion) {
        return new A2AInterface(url, protocolBinding, protocolVersion, Map.of());
    }
}
