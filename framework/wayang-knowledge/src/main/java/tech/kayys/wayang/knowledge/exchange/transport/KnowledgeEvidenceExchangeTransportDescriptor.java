package tech.kayys.wayang.knowledge.exchange.transport;

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


import java.util.Map;
import java.util.Set;

public record KnowledgeEvidenceExchangeTransportDescriptor(

        String transportId,

        KnowledgeEvidenceExchangeTransportType type,

        Set<KnowledgeEvidenceExchangeTransportCapability>
                capabilities,

        String protocolVersion,

        int maxConcurrentStreams,

        long maxFrameBytes,

        Map<String, String> metadata

) {

    public KnowledgeEvidenceExchangeTransportDescriptor {
        capabilities = capabilities == null
                ? Set.of()
                : Set.copyOf(capabilities);

        metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }

    public boolean supports(
            KnowledgeEvidenceExchangeTransportCapability capability
    ) {
        return capabilities.contains(capability);
    }
}
