package tech.kayys.wayang.knowledge.exchange.trust;

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

public sealed interface KnowledgeEvidenceExchangeKeyTrustDecision
        permits
        KnowledgeEvidenceExchangeKeyTrustDecision.Trusted,
        KnowledgeEvidenceExchangeKeyTrustDecision.Denied {

    record Trusted(
            String policyId,
            Map<String, String> metadata
    ) implements KnowledgeEvidenceExchangeKeyTrustDecision {

        public Trusted {
            metadata = metadata == null
                    ? Map.of()
                    : Map.copyOf(metadata);
        }
    }

    record Denied(
            String policyId,
            KnowledgeEvidenceExchangeKeyTrustStatus status,
            String reason,
            Map<String, String> metadata
    ) implements KnowledgeEvidenceExchangeKeyTrustDecision {

        public Denied {
            metadata = metadata == null
                    ? Map.of()
                    : Map.copyOf(metadata);
        }
    }
}
