package tech.kayys.wayang.knowledge.exchange.fusion;

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
import tech.kayys.wayang.knowledge.exchange.transfer.*;
import tech.kayys.wayang.knowledge.exchange.replication.*;
import tech.kayys.wayang.knowledge.exchange.sync.*;
import tech.kayys.wayang.knowledge.exchange.federation.*;
import tech.kayys.wayang.knowledge.exchange.routing.*;
import tech.kayys.wayang.knowledge.exchange.fusion.*;

public final class DefaultKnowledgeEvidenceTrustScorer
        implements KnowledgeEvidenceTrustScorer {

    @Override
    public double score(KnowledgeEvidenceReference evidence) {
        if (evidence == null) {
            return 0.0;
        }
        if (evidence.trust() > 0.0) {
            return evidence.trust();
        }
        Object metaTrust = evidence.metadata().get("trust");
        if (metaTrust != null) {
            String trust = metaTrust.toString().toLowerCase();
            return switch (trust) {
                case "verified", "attested", "trusted" -> 1.0;
                case "known" -> 0.75;
                case "unknown" -> 0.35;
                case "untrusted" -> 0.0;
                default -> 0.50;
            };
        }
        return 0.50;
    }
}
