package tech.kayys.wayang.knowledge.exchange.uncertainty;

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
import tech.kayys.wayang.knowledge.exchange.coverage.*;
import tech.kayys.wayang.knowledge.exchange.gap.*;
import tech.kayys.wayang.knowledge.exchange.attribution.*;
import tech.kayys.wayang.knowledge.exchange.contradiction.*;
import tech.kayys.wayang.knowledge.exchange.factuality.*;
import tech.kayys.wayang.knowledge.exchange.uncertainty.*;
import tech.kayys.wayang.knowledge.exchange.compact.*;
import tech.kayys.wayang.knowledge.exchange.resolution.*;
import tech.kayys.wayang.knowledge.exchange.quorum.*;
import tech.kayys.wayang.knowledge.exchange.selection.*;
import tech.kayys.wayang.knowledge.exchange.coordination.*;
import tech.kayys.wayang.knowledge.exchange.attestation.*;
import tech.kayys.wayang.knowledge.exchange.proof.*;
import tech.kayys.wayang.knowledge.exchange.validity.*;
import tech.kayys.wayang.knowledge.exchange.lease.*;
import tech.kayys.wayang.knowledge.exchange.recovery.*;


import java.util.ArrayList;
import java.util.List;

public final class DefaultKnowledgeAnswerArtifactDiscoveryEngine
        implements KnowledgeAnswerArtifactDiscoveryEngine {

    private final KnowledgeAnswerArtifactIndex index;

    private final KnowledgeAnswerArtifactQueryAnalyzer analyzer;

    private final KnowledgeAnswerArtifactSemanticScorer semanticScorer;

    private final KnowledgeAnswerArtifactAuthorityScorer authorityScorer;

    private final KnowledgeAnswerArtifactTrustScorer trustScorer;

    private final KnowledgeAnswerArtifactRanker ranker;

    public DefaultKnowledgeAnswerArtifactDiscoveryEngine(
            KnowledgeAnswerArtifactIndex index) {

        this(
                index,
                new DefaultKnowledgeAnswerArtifactQueryAnalyzer(),
                new DefaultKnowledgeAnswerArtifactSemanticScorer(),
                new DefaultKnowledgeAnswerArtifactAuthorityScorer(),
                new DefaultKnowledgeAnswerArtifactTrustScorer(),
                new DefaultKnowledgeAnswerArtifactRanker()
        );
    }

    public DefaultKnowledgeAnswerArtifactDiscoveryEngine(
            KnowledgeAnswerArtifactIndex index,
            KnowledgeAnswerArtifactQueryAnalyzer analyzer,
            KnowledgeAnswerArtifactSemanticScorer semanticScorer,
            KnowledgeAnswerArtifactAuthorityScorer authorityScorer,
            KnowledgeAnswerArtifactTrustScorer trustScorer,
            KnowledgeAnswerArtifactRanker ranker) {

        this.index = index;
        this.analyzer = analyzer;
        this.semanticScorer = semanticScorer;
        this.authorityScorer = authorityScorer;
        this.trustScorer = trustScorer;
        this.ranker = ranker;
    }

    @Override
    public KnowledgeAnswerArtifactDiscoveryResult discover(
            KnowledgeAnswerArtifactQuery query) {

        var intent =
                analyzer.analyze(query.text());

        var entries =
                index.search(query);

        List<KnowledgeAnswerArtifactCandidate>
                candidates = new ArrayList<>();

        for (var entry : entries) {

            double semantic =
                    semanticScorer.score(
                            intent,
                            entry
                    );

            double authority =
                    authorityScorer.score(entry);

            double trust =
                    trustScorer.score(entry);

            double freshness =
                    freshness(entry);

            double verification =
                    entry.verified()
                            ? 1.0
                            : 0.0;

            double finalScore =
                    semantic * 0.40
                            + authority * 0.20
                            + trust * 0.20
                            + freshness * 0.10
                            + verification * 0.10;

            candidates.add(
                    new KnowledgeAnswerArtifactCandidate(
                            entry.artifactId(),
                            entry.responseId(),
                            entry.runtimeId(),
                            entry.tenantId(),
                            entry.workspaceId(),
                            entry.projectId(),
                            entry.agentId(),
                            semantic,
                            authority,
                            trust,
                            freshness,
                            verification,
                            finalScore,
                            entry.verified(),
                            entry.sealed(),
                            false,
                            entry.metadata()
                    )
            );
        }

        var ranked =
                ranker.rank(candidates);

        var selected =
                ranked.stream()
                        .limit(query.limit())
                        .toList();

        return new KnowledgeAnswerArtifactDiscoveryResult(
                query.queryId(),
                ranked,
                selected,
                true,
                false,
                java.util.Map.of(
                        "candidateCount",
                        Integer.toString(ranked.size())
                )
        );
    }

    private double freshness(
            KnowledgeAnswerArtifactIndexEntry entry) {

        if (entry.effectiveAt() == null) {
            return 0.5;
        }

        return 1.0;
    }
}
