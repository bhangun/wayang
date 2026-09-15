package tech.kayys.wayang.communication.api;

import tech.kayys.wayang.communication.protocol.ProtocolId;

import java.util.List;

public record CommunicationOptions(
        List<ProtocolId> preferredProtocols,
        boolean allowFallback
) {

    public CommunicationOptions {
        preferredProtocols =
                preferredProtocols == null
                        ? List.of()
                        : List.copyOf(preferredProtocols);
    }

    public static CommunicationOptions automatic() {
        return new CommunicationOptions(
                List.of(),
                true
        );
    }

    public static CommunicationOptions prefer(ProtocolId... protocols) {
        return new CommunicationOptions(
                List.of(protocols),
                true
        );
    }

    public static CommunicationOptions strict(ProtocolId protocol) {
        return new CommunicationOptions(
                List.of(protocol),
                false
        );
    }
}
