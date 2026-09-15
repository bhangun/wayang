package tech.kayys.wayang.communication.core.protocol;

import tech.kayys.wayang.communication.api.AgentRequest;
import tech.kayys.wayang.communication.api.CommunicationOptions;
import tech.kayys.wayang.communication.api.RequestMode;
import tech.kayys.wayang.communication.capability.ProtocolCapability;
import tech.kayys.wayang.communication.endpoint.EndpointType;
import tech.kayys.wayang.communication.protocol.AgentProtocol;
import tech.kayys.wayang.communication.protocol.ProtocolContext;
import tech.kayys.wayang.communication.protocol.ProtocolId;
import tech.kayys.wayang.communication.protocol.WellKnownProtocols;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class DefaultProtocolSelectionPolicy
        implements ProtocolSelectionPolicy {

    @Override
    public List<ProtocolCandidate> rank(
            Collection<AgentProtocol> protocols,
            AgentRequest request,
            ProtocolContext context
    ) {
        var candidates = new ArrayList<ProtocolCandidate>();
        CommunicationOptions options = request.communication();
        List<ProtocolId> preferred = options != null ? options.preferredProtocols() : List.of();
        boolean allowFallback = options == null || options.allowFallback();

        for (var protocol : protocols) {
            if (!protocol.supports(
                    request.target().endpoint(),
                    request
            )) {
                continue;
            }

            if (!allowFallback && !preferred.isEmpty() && !preferred.contains(protocol.id())) {
                continue;
            }

            var score = 0;
            var reasons = new ArrayList<String>();

            if (!preferred.isEmpty()) {
                int prefIndex = preferred.indexOf(protocol.id());
                if (prefIndex >= 0) {
                    score += 500 - (prefIndex * 50);
                    reasons.add("explicitly preferred protocol");
                }
            }

            if (request.target().endpoint().type() == EndpointType.LOCAL) {
                if (protocol.id().equals(WellKnownProtocols.IN_PROCESS)) {
                    score += 100;
                    reasons.add("local endpoint matching in-process protocol");
                } else {
                    score += 10;
                    reasons.add("local endpoint");
                }
            }

            if (request.target().endpoint().type() == EndpointType.REMOTE) {
                score += 20;
                reasons.add("remote endpoint");
            }

            if (request.mode() == RequestMode.STREAM
                    && protocol.capabilities().contains(ProtocolCapability.STREAMING)) {
                score += 30;
                reasons.add("streaming supported");
            }

            if (request.mode() == RequestMode.ASYNC
                    && protocol.capabilities().contains(ProtocolCapability.ASYNC_TASK)) {
                score += 30;
                reasons.add("async task supported");
            }

            if (request.mode() == RequestMode.SYNC
                    && protocol.capabilities().contains(ProtocolCapability.REQUEST_RESPONSE)) {
                score += 20;
                reasons.add("request/response supported");
            }

            if (request.mode() == RequestMode.AUTO) {
                if (protocol.capabilities().contains(ProtocolCapability.REQUEST_RESPONSE)) {
                    score += 10;
                }
                if (protocol.capabilities().contains(ProtocolCapability.ASYNC_TASK)) {
                    score += 5;
                }
            }

            candidates.add(new ProtocolCandidate(protocol, score, reasons));
        }

        candidates.sort(
                Comparator.comparingInt(ProtocolCandidate::score).reversed()
        );

        return List.copyOf(candidates);
    }
}
