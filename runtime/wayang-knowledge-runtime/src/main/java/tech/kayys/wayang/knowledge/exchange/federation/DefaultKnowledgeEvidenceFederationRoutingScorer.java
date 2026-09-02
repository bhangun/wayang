package tech.kayys.wayang.knowledge.exchange.federation;

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


public final class DefaultKnowledgeEvidenceFederationRoutingScorer
        implements KnowledgeEvidenceFederationRoutingScorer {

    @Override
    public double score(
            KnowledgeEvidenceRuntimeLocation location,
            KnowledgeEvidenceRuntimeQueryCapability capability,
            KnowledgeEvidenceFederatedQuery query
    ) {

        double score = 0.0;

        if (location.local()) {
            score += 100.0;
        }

        if (location.trusted()) {
            score += 50.0;
        }

        if (location.verified()) {
            score += 30.0;
        }

        if (capability.governedRetrieval()) {
            score += 25.0;
        }

        if (query.requireVerification()
                && capability.verification()) {

            score += 20.0;
        }

        score -= Math.min(
                location.estimatedLatencyMs() / 10.0,
                50.0
        );

        return score;
    }
}
