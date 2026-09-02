package tech.kayys.wayang.knowledge.exchange.protocol;

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

public interface KnowledgeEvidenceExchangeSecureHandshakeService {

    KnowledgeEvidenceExchangeProtocolSession initiate(
            KnowledgeEvidenceExchangeRuntimeIdentity localIdentity,
            KnowledgeEvidenceExchangeProtocolHello localHello,
            Instant now
    );

    KnowledgeEvidenceExchangeProtocolSession receiveHello(
            KnowledgeEvidenceExchangeProtocolSession session,
            KnowledgeEvidenceExchangeProtocolHello remoteHello,
            Instant now
    );

    KnowledgeEvidenceExchangeProtocolSession authenticate(
            KnowledgeEvidenceExchangeProtocolSession session,
            KnowledgeEvidenceExchangeProtocolAuthenticationMessage
                    authentication,
            Instant now
    );

    KnowledgeEvidenceExchangeProtocolSession negotiate(
            KnowledgeEvidenceExchangeProtocolSession session,
            KnowledgeEvidenceExchangeCapabilityNegotiationResult
                    negotiation,
            Instant now
    );

    KnowledgeEvidenceExchangeProtocolSession establish(
            KnowledgeEvidenceExchangeProtocolSession session,
            String handshakeId,
            Instant now
    );
}
