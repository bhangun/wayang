package tech.kayys.wayang.anp.meta;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Declares what protocols and codecs an agent supports during ANP meta-protocol negotiation.
 */
public record AnpCapabilityAdvertisement(
        String agentDid,
        List<String> supportedProtocols,
        List<String> supportedCodecs,
        Map<String, Object> parameters
) {
    public AnpCapabilityAdvertisement {
        Objects.requireNonNull(agentDid, "agentDid");
        supportedProtocols = supportedProtocols == null ? List.of("a2a/1.0", "anp/1.1") : List.copyOf(supportedProtocols);
        supportedCodecs = supportedCodecs == null ? List.of("application/json") : List.copyOf(supportedCodecs);
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }

    /** Default advertisement for Wayang agents (prefers A2A over ANP transport for maximum interop). */
    public static AnpCapabilityAdvertisement defaultWayang(String agentDid) {
        return new AnpCapabilityAdvertisement(
                agentDid,
                List.of("a2a/1.0", "anp/1.1"),
                List.of("application/json"),
                Map.of("framework", "wayang")
        );
    }
}
