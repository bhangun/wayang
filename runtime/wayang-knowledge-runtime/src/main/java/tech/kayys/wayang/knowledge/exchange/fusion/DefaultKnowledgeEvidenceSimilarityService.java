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


import java.util.HashSet;

public final class DefaultKnowledgeEvidenceSimilarityService
        implements KnowledgeEvidenceSimilarityService {

    @Override
    public double similarity(
            KnowledgeEvidenceReference left,
            KnowledgeEvidenceReference right
    ) {

        if (left.knowledgeId()
                .equals(right.knowledgeId())) {

            return 1.0;
        }

        if (left.fragmentId() != null
                && left.fragmentId().equals(
                        right.fragmentId()
                )) {

            return 1.0;
        }

        String a =
                normalize(left.excerpt());

        String b =
                normalize(right.excerpt());

        if (a.isBlank() || b.isBlank()) {
            return 0.0;
        }

        var leftTokens =
                new HashSet<>(
                        java.util.List.of(
                                a.split("\\s+")
                        )
                );

        var rightTokens =
                new HashSet<>(
                        java.util.List.of(
                                b.split("\\s+")
                        )
                );

        var intersection =
                new HashSet<>(leftTokens);

        intersection.retainAll(rightTokens);

        var union =
                new HashSet<>(leftTokens);

        union.addAll(rightTokens);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size()
                / union.size();
    }

    private String normalize(String value) {

        if (value == null) {
            return "";
        }

        return value
                .toLowerCase()
                .replaceAll(
                        "[^\\p{L}\\p{N}\\s]",
                        " "
                )
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }
}
