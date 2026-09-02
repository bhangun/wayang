package tech.kayys.wayang.knowledge.exchange.capability;

import tech.kayys.wayang.knowledge.*;
import tech.kayys.wayang.knowledge.seal.*;
import tech.kayys.wayang.knowledge.snapshot.*;
import tech.kayys.wayang.knowledge.snapshot.pack.*;
import tech.kayys.wayang.knowledge.snapshot.artifact.*;
import tech.kayys.wayang.knowledge.snapshot.merkle.*;
import tech.kayys.wayang.knowledge.exchange.*;
import tech.kayys.wayang.knowledge.exchange.auth.*;
import tech.kayys.wayang.knowledge.exchange.session.*;
import tech.kayys.wayang.knowledge.exchange.binding.*;
import tech.kayys.wayang.knowledge.exchange.envelope.*;
import tech.kayys.wayang.knowledge.exchange.trust.*;
import tech.kayys.wayang.knowledge.exchange.identity.*;
import tech.kayys.wayang.knowledge.exchange.capability.*;
import tech.kayys.wayang.knowledge.exchange.protocol.*;
import tech.kayys.wayang.knowledge.exchange.transport.*;
import tech.kayys.wayang.knowledge.exchange.framing.*;


import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class DefaultKnowledgeEvidenceExchangeCapabilityNegotiator
        implements KnowledgeEvidenceExchangeCapabilityNegotiator {

    @Override
    public KnowledgeEvidenceExchangeCapabilityNegotiationResult negotiate(
            KnowledgeEvidenceExchangeCapabilityManifest local,
            KnowledgeEvidenceExchangeCapabilityManifest remote,
            Instant now
    ) {

        Objects.requireNonNull(local);
        Objects.requireNonNull(remote);
        Objects.requireNonNull(now);

        if (!local.activeAt(now) ||
                !remote.activeAt(now)) {

            return failure(
                    KnowledgeEvidenceExchangeCapabilityNegotiationStatus
                            .MANIFEST_EXPIRED,
                    local,
                    remote,
                    "Capability manifest expired"
            );
        }

        var protocol =
                negotiateProtocol(
                        local.protocolVersion(),
                        remote.protocolVersion()
                );

        if (protocol == null) {

            return failure(
                    KnowledgeEvidenceExchangeCapabilityNegotiationStatus
                            .NO_COMMON_PROTOCOL,
                    local,
                    remote,
                    "No compatible protocol version"
            );
        }

        List<KnowledgeEvidenceExchangeNegotiatedCapability>
                negotiated = new ArrayList<>();

        for (var localCapability :
                local.capabilities()) {

            if (!localCapability.supported()) {
                continue;
            }

            var remoteCapability =
                    remote.capabilities()
                            .stream()
                            .filter(c ->
                                    c.type() ==
                                            localCapability.type())
                            .filter(
                                    KnowledgeEvidenceExchangeRuntimeCapability
                                            ::supported
                            )
                            .findFirst();

            if (remoteCapability.isEmpty()) {

                if (localCapability.required()) {

                    return failure(
                            KnowledgeEvidenceExchangeCapabilityNegotiationStatus
                                    .REQUIRED_CAPABILITY_MISSING,
                            local,
                            remote,
                            "Required capability missing: "
                                    + localCapability.type()
                    );
                }

                continue;
            }

            var intersection =
                    negotiateCapability(
                            localCapability,
                            remoteCapability.get()
                    );

            if (intersection != null) {
                negotiated.add(intersection);
            }
        }

        return new KnowledgeEvidenceExchangeCapabilityNegotiationResult(
                KnowledgeEvidenceExchangeCapabilityNegotiationStatus
                        .NEGOTIATED,
                protocol,
                negotiated,
                local.manifestFingerprint(),
                remote.manifestFingerprint(),
                List.of(),
                java.util.Map.of()
        );
    }

    private KnowledgeEvidenceExchangeProtocolVersion
    negotiateProtocol(
            KnowledgeEvidenceExchangeProtocolVersion local,
            KnowledgeEvidenceExchangeProtocolVersion remote
    ) {

        /*
         * Major versions are incompatible.
         * Minor versions are backward compatible.
         */
        if (local.major() != remote.major()) {
            return null;
        }

        return local.compareTo(remote) <= 0
                ? local
                : remote;
    }

    private KnowledgeEvidenceExchangeNegotiatedCapability
    negotiateCapability(
            KnowledgeEvidenceExchangeRuntimeCapability local,
            KnowledgeEvidenceExchangeRuntimeCapability remote
    ) {

        Set<String> algorithms =
                new HashSet<>(local.algorithms());

        algorithms.retainAll(remote.algorithms());

        Set<String> formats =
                new HashSet<>(local.formats());

        formats.retainAll(remote.formats());

        long maxBytes;

        if (local.maxArtifactBytes() <= 0) {
            maxBytes = remote.maxArtifactBytes();
        } else if (remote.maxArtifactBytes() <= 0) {
            maxBytes = local.maxArtifactBytes();
        } else {
            maxBytes = Math.min(
                    local.maxArtifactBytes(),
                    remote.maxArtifactBytes()
            );
        }

        /*
         * Capability does not necessarily require algorithms/formats.
         */
        if ((!local.algorithms().isEmpty() &&
                !remote.algorithms().isEmpty() &&
                algorithms.isEmpty()) ||

            (!local.formats().isEmpty() &&
                !remote.formats().isEmpty() &&
                formats.isEmpty())) {

            if (local.required()) {
                throw new IllegalStateException(
                        "Required capability has no compatible algorithm/format: "
                                + local.type()
                );
            }

            return null;
        }

        return new KnowledgeEvidenceExchangeNegotiatedCapability(
                local.type(),
                algorithms,
                formats,
                maxBytes
        );
    }

    private KnowledgeEvidenceExchangeCapabilityNegotiationResult
    failure(
            KnowledgeEvidenceExchangeCapabilityNegotiationStatus status,
            KnowledgeEvidenceExchangeCapabilityManifest local,
            KnowledgeEvidenceExchangeCapabilityManifest remote,
            String diagnostic
    ) {

        return new KnowledgeEvidenceExchangeCapabilityNegotiationResult(
                status,
                null,
                List.of(),
                local.manifestFingerprint(),
                remote.manifestFingerprint(),
                List.of(diagnostic),
                java.util.Map.of()
        );
    }
}
