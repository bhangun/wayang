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


public final class DefaultKnowledgeEvidenceDuplicateDetector
        implements KnowledgeEvidenceDuplicateDetector {

    private final KnowledgeEvidenceSimilarityService similarity;

    private final double threshold;

    public DefaultKnowledgeEvidenceDuplicateDetector(
            KnowledgeEvidenceSimilarityService similarity,
            double threshold
    ) {

        this.similarity = similarity;
        this.threshold = threshold;
    }

    @Override
    public boolean duplicate(
            KnowledgeEvidenceReference left,
            KnowledgeEvidenceReference right
    ) {

        if (left.knowledgeId()
                .equals(right.knowledgeId())
                && java.util.Objects.equals(
                        left.versionId(),
                        right.versionId()
                )
                && java.util.Objects.equals(
                        left.fragmentId(),
                        right.fragmentId()
                )) {

            return true;
        }

        return similarity.similarity(
                left,
                right
        ) >= threshold;
    }
}
