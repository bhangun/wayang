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


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DefaultKnowledgeEvidenceConflictDetector
        implements KnowledgeEvidenceConflictDetector {

    private final KnowledgeEvidenceSimilarityService similarity;

    public DefaultKnowledgeEvidenceConflictDetector(
            KnowledgeEvidenceSimilarityService similarity
    ) {

        this.similarity = similarity;
    }

    @Override
    public List<KnowledgeEvidenceFusionConflict> detect(
            List<KnowledgeEvidenceFusionCandidate> candidates
    ) {

        var conflicts =
                new ArrayList<
                        KnowledgeEvidenceFusionConflict
                        >();

        for (int i = 0;
             i < candidates.size();
             i++) {

            for (int j = i + 1;
                 j < candidates.size();
                 j++) {

                var left =
                        candidates.get(i);

                var right =
                        candidates.get(j);

                var a =
                        left.evidence();

                var b =
                        right.evidence();

                /*
                 * Same knowledge/version is not a conflict.
                 */
                if (a.knowledgeId()
                        .equals(b.knowledgeId())
                        && java.util.Objects.equals(
                                a.versionId(),
                                b.versionId()
                        )) {

                    continue;
                }

                /*
                 * Core cannot safely infer contradiction from
                 * arbitrary natural-language excerpts.
                 *
                 * A domain-specific conflict detector should
                 * implement semantic contradiction.
                 */
                double similarityScore =
                        similarity.similarity(
                                a,
                                b
                        );

                if (similarityScore >= 0.95
                        && !java.util.Objects.equals(
                                a.versionId(),
                                b.versionId()
                        )) {

                    conflicts.add(
                            new KnowledgeEvidenceFusionConflict(
                                    UUID.randomUUID()
                                            .toString(),
                                    evidenceId(a),
                                    evidenceId(b),
                                    KnowledgeEvidenceConflictType
                                            .VERSION_CONFLICT,
                                    similarityScore,
                                    "Highly similar evidence has different versions",
                                    java.util.Map.of()
                            )
                    );
                }
            }
        }

        return List.copyOf(conflicts);
    }

    private String evidenceId(
            KnowledgeEvidenceReference evidence
    ) {

        return evidence.knowledgeId()
                + ":"
                + evidence.versionId()
                + ":"
                + evidence.fragmentId();
    }
}
