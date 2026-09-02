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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DefaultKnowledgeEvidenceCoherenceSelector
        implements KnowledgeEvidenceCoherenceSelector {

    @Override
    public List<KnowledgeEvidenceFusionCandidate> select(
            List<KnowledgeEvidenceFusionCandidate> candidates,
            List<KnowledgeEvidenceFusionRelation> relations,
            List<KnowledgeEvidenceFusionConflict> conflicts,
            KnowledgeEvidenceSemanticQuery query
    ) {

        var selected =
                new ArrayList<
                        KnowledgeEvidenceFusionCandidate
                        >();

        var seenKnowledge =
                new HashSet<String>();

        for (var candidate : candidates) {

            if (selected.size() >= query.limit()) {
                break;
            }

            var evidence =
                    candidate.evidence();

            String key =
                    evidence.knowledgeId()
                            + ":"
                            + evidence.versionId()
                            + ":"
                            + evidence.fragmentId();

            if (!seenKnowledge.add(key)) {
                continue;
            }

            /*
             * Avoid flooding the context with extremely weak evidence.
             */
            if (candidate.finalScore()
                    < query.minScore()) {

                continue;
            }

            selected.add(candidate);
        }

        return List.copyOf(selected);
    }
}
