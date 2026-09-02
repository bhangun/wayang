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
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class DefaultKnowledgeEvidenceExchangeCapabilityDiscoveryService
        implements KnowledgeEvidenceExchangeCapabilityDiscoveryService {

    private final KnowledgeEvidenceExchangeRuntimeIdentityRegistry
            identityRegistry;

    private final KnowledgeEvidenceExchangeCapabilityManifestFingerprinter
            fingerprinter;

    private final KnowledgeEvidenceExchangeProtocolVersion
            protocolVersion;

    private final List<KnowledgeEvidenceExchangeRuntimeCapability>
            capabilities;

    public DefaultKnowledgeEvidenceExchangeCapabilityDiscoveryService(
            KnowledgeEvidenceExchangeRuntimeIdentityRegistry
                    identityRegistry,
            KnowledgeEvidenceExchangeCapabilityManifestFingerprinter
                    fingerprinter,
            KnowledgeEvidenceExchangeProtocolVersion protocolVersion,
            List<KnowledgeEvidenceExchangeRuntimeCapability>
                    capabilities
    ) {

        this.identityRegistry =
                Objects.requireNonNull(identityRegistry);

        this.fingerprinter =
                Objects.requireNonNull(fingerprinter);

        this.protocolVersion =
                Objects.requireNonNull(protocolVersion);

        this.capabilities =
                capabilities == null
                        ? List.of()
                        : List.copyOf(capabilities);
    }

    @Override
    public KnowledgeEvidenceExchangeCapabilityManifest discover(
            String runtimeId,
            Instant now
    ) {

        var identity =
                identityRegistry.resolve(
                        runtimeId,
                        now
                ).orElseThrow(() ->
                        new IllegalStateException(
                                "Runtime identity not found: "
                                        + runtimeId
                        )
                );

        var provisional =
                new KnowledgeEvidenceExchangeCapabilityManifest(
                        identity.runtimeId(),
                        identity.identityVersion(),
                        identity.identityFingerprint(),
                        protocolVersion,
                        capabilities,
                        now,
                        now.plusSeconds(300),
                        "",
                        Map.of()
                );

        String fingerprint =
                fingerprinter.fingerprint(provisional);

        return new KnowledgeEvidenceExchangeCapabilityManifest(
                provisional.runtimeId(),
                provisional.identityVersion(),
                provisional.identityFingerprint(),
                provisional.protocolVersion(),
                provisional.capabilities(),
                provisional.issuedAt(),
                provisional.expiresAt(),
                fingerprint,
                provisional.metadata()
        );
    }
}
