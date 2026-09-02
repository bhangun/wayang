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

public final class DefaultKnowledgeEvidenceAuthorityScorer
        implements KnowledgeEvidenceAuthorityScorer {

    @Override
    public double score(KnowledgeEvidenceReference evidence) {
        if (evidence == null) {
            return 0.0;
        }
        if (evidence.authority() > 0.0) {
            return evidence.authority();
        }
        Object metaAuthority = evidence.metadata().get("authority");
        if (metaAuthority != null) {
            String authority = metaAuthority.toString().toLowerCase();
            return switch (authority) {
                case "authoritative", "approved", "primary" -> 1.0;
                case "official" -> 0.95;
                case "secondary" -> 0.60;
                case "commentary" -> 0.40;
                case "unverified" -> 0.10;
                default -> 0.50;
            };
        }
        return 0.50;
    }
}
