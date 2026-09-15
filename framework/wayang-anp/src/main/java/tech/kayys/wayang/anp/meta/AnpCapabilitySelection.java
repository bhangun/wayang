package tech.kayys.wayang.anp.meta;

import java.util.Map;
import java.util.Objects;

/**
 * The outcome of ANP meta-protocol negotiation between two agents.
 */
public record AnpCapabilitySelection(
        String protocol,
        String codec,
        Map<String, Object> parameters
) {
    public AnpCapabilitySelection {
        Objects.requireNonNull(protocol, "protocol");
        Objects.requireNonNull(codec, "codec");
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }

    public boolean isA2A() {
        return protocol.toLowerCase().startsWith("a2a");
    }
}
