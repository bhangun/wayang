package tech.kayys.wayang.knowledge.exchange.routing;

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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public final class DefaultKnowledgeEvidenceFusionService
        implements KnowledgeEvidenceFusionService {

    @Override
    public List<KnowledgeEvidenceReference> fuse(
            List<KnowledgeEvidenceFederationQueryResult> results
    ) {

        var merged =
                new LinkedHashMap<
                        String,
                        KnowledgeEvidenceReference
                        >();

        for (var result : results) {

            if (result == null || !result.authorized()) {
                continue;
            }

            for (var evidence : result.evidence()) {

                String key =
                        evidence.knowledgeId()
                                + ":"
                                + String.valueOf(
                                        evidence.versionId()
                                )
                                + ":"
                                + String.valueOf(
                                        evidence.fragmentId()
                                );

                merged.merge(
                        key,
                        evidence,
                        (existing, incoming) ->
                                incoming.relevance()
                                        > existing.relevance()
                                        ? incoming
                                        : existing
                );
            }
        }

        return merged.values()
                .stream()
                .sorted(
                        Comparator.comparingDouble(
                                        KnowledgeEvidenceReference
                                                ::relevance
                                )
                                .reversed()
                )
                .toList();
    }
}
