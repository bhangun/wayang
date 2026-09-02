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


public interface KnowledgeEvidenceExchangeProtocolVersionPolicy {

    KnowledgeEvidenceExchangeProtocolVersion negotiate(
            KnowledgeEvidenceExchangeProtocolVersion localPreferred,
            java.util.Set<KnowledgeEvidenceExchangeProtocolVersion>
                    localSupported,
            KnowledgeEvidenceExchangeProtocolVersion remotePreferred,
            java.util.Set<KnowledgeEvidenceExchangeProtocolVersion>
                    remoteSupported
    );

    void validateNoDowngrade(
            KnowledgeEvidenceExchangeProtocolVersion selected,
            java.util.Set<KnowledgeEvidenceExchangeProtocolVersion>
                    localSupported,
            java.util.Set<KnowledgeEvidenceExchangeProtocolVersion>
                    remoteSupported
    );
}
