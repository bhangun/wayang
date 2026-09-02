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


public final class DefaultKnowledgeEvidenceFusionScorer
        implements KnowledgeEvidenceFusionScorer {

    private final KnowledgeEvidenceAuthorityScorer authority;

    private final KnowledgeEvidenceTrustScorer trust;

    private final KnowledgeEvidenceFreshnessScorer freshness;

    public DefaultKnowledgeEvidenceFusionScorer(
            KnowledgeEvidenceAuthorityScorer authority,
            KnowledgeEvidenceTrustScorer trust,
            KnowledgeEvidenceFreshnessScorer freshness
    ) {

        this.authority = authority;
        this.trust = trust;
        this.freshness = freshness;
    }

    @Override
    public KnowledgeEvidenceFusionCandidate score(
            KnowledgeEvidenceReference evidence,
            String runtimeId,
            KnowledgeEvidenceSemanticQuery query
    ) {

        double retrieval =
                clamp(evidence.relevance());

        double authorityScore =
                clamp(
                        authority.score(evidence)
                );

        double trustScore =
                clamp(
                        trust.score(evidence)
                );

        double freshnessScore =
                clamp(
                        freshness.score(
                                evidence,
                                query.effectiveAt()
                        )
                );

        /*
         * Default weights are deliberately transparent.
         * Domain implementations may override this SPI.
         */
        double finalScore =
                retrieval * 0.45
                        + authorityScore * 0.25
                        + trustScore * 0.20
                        + freshnessScore * 0.10;

        return new KnowledgeEvidenceFusionCandidate(
                evidence,
                runtimeId,
                retrieval,
                retrieval,
                authorityScore,
                trustScore,
                freshnessScore,
                finalScore,
                java.util.Map.of()
        );
    }

    private double clamp(double value) {

        if (Double.isNaN(value)
                || Double.isInfinite(value)) {

            return 0.0;
        }

        return Math.max(
                0.0,
                Math.min(
                        1.0,
                        value
                )
        );
    }
}
