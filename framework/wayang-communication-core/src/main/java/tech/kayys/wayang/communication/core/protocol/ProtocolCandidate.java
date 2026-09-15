package tech.kayys.wayang.communication.core.protocol;

import tech.kayys.wayang.communication.protocol.AgentProtocol;

import java.util.List;
import java.util.Objects;

public record ProtocolCandidate(
        AgentProtocol protocol,
        int score,
        List<String> reasons
) {

    public ProtocolCandidate {
        Objects.requireNonNull(protocol, "protocol");
        reasons = reasons == null
                ? List.of()
                : List.copyOf(reasons);
    }
}
