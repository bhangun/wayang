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

public record KnowledgeEvidenceExchangeNegotiatedSession(

        String sessionId,

        String localRuntimeId,

        String remoteRuntimeId,

        KnowledgeEvidenceExchangeProtocolVersion protocolVersion,

        List<KnowledgeEvidenceExchangeNegotiatedCapability>
                capabilities,

        String localManifestFingerprint,

        String remoteManifestFingerprint,

        Instant establishedAt,

        Instant expiresAt,

        Map<String, String> metadata

) {

    public KnowledgeEvidenceExchangeNegotiatedSession {
        capabilities = capabilities == null
                ? List.of()
                : List.copyOf(capabilities);

        metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }

    public boolean activeAt(Instant at) {

        return expiresAt == null ||
                at.isBefore(expiresAt);
    }

    public boolean supports(
            KnowledgeEvidenceExchangeCapabilityType type
    ) {

        return capabilities.stream()
                .anyMatch(
                        capability -> capability.type() == type
                );
    }
}
