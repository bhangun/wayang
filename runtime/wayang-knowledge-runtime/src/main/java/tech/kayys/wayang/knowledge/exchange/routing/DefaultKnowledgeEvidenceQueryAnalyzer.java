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


import java.util.Arrays;
import java.util.List;

public final class DefaultKnowledgeEvidenceQueryAnalyzer
        implements KnowledgeEvidenceQueryAnalyzer {

    @Override
    public KnowledgeEvidenceQueryIntent analyze(
            KnowledgeEvidenceSemanticQuery query
    ) {

        List<String> keywords =
                Arrays.stream(
                                query.text()
                                        .toLowerCase()
                                        .split("\\s+")
                        )
                        .map(token ->
                                token.replaceAll(
                                        "[^\\p{L}\\p{N}_-]",
                                        ""
                                )
                        )
                        .filter(token ->
                                !token.isBlank()
                        )
                        .distinct()
                        .toList();

        return new KnowledgeEvidenceQueryIntent(
                query.text(),
                keywords,
                List.of(),
                keywords,
                query.requiredTags(),
                java.util.Map.of()
        );
    }
}
