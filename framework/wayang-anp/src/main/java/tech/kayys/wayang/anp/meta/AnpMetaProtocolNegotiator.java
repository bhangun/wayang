package tech.kayys.wayang.anp.meta;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Negotiates the application-level protocol between two agents per ANP Spec 06.
 *
 * <p>Prioritizes {@code a2a/1.0} for seamless cross-framework interop,
 * falling back to {@code anp/1.1} native format.
 */
public final class AnpMetaProtocolNegotiator {

    public AnpCapabilitySelection negotiate(
            AnpCapabilityAdvertisement initiator,
            AnpCapabilityAdvertisement responder) {

        // Find common protocols, respecting initiator preference order
        for (String proto : initiator.supportedProtocols()) {
            if (responder.supportedProtocols().stream().anyMatch(p -> p.equalsIgnoreCase(proto))) {
                String codec = selectCommonCodec(initiator.supportedCodecs(), responder.supportedCodecs());
                return new AnpCapabilitySelection(proto, codec, Map.of("negotiatedBy", "wayang-meta-protocol"));
            }
        }

        // Fallback default
        return new AnpCapabilitySelection("anp/1.1", "application/json", Map.of("negotiatedBy", "fallback"));
    }

    private String selectCommonCodec(List<String> initiator, List<String> responder) {
        for (String codec : initiator) {
            if (responder.stream().anyMatch(c -> c.equalsIgnoreCase(codec))) {
                return codec;
            }
        }
        return "application/json";
    }
}
