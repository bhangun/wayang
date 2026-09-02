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


import java.time.Instant;
import java.util.List;
import java.util.Map;

public record KnowledgeEvidenceSemanticQuery(

        String queryId,

        String text,

        String tenantId,

        String workspaceId,

        String projectId,

        String agentId,

        int limit,

        double minScore,

        Instant effectiveAt,

        List<String> requiredKnowledgeTypes,

        List<String> requiredTags,

        KnowledgeEvidenceFederatedRetrievalStrategy strategy,

        boolean requireVerification,

        boolean allowRemote,

        Map<String, String> metadata

) {

    public KnowledgeEvidenceSemanticQuery {

        if (queryId == null || queryId.isBlank()) {
            throw new IllegalArgumentException("queryId is required");
        }

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text is required");
        }

        if (limit < 1) {
            throw new IllegalArgumentException(
                    "limit must be positive"
            );
        }

        requiredKnowledgeTypes =
                requiredKnowledgeTypes == null
                        ? List.of()
                        : List.copyOf(requiredKnowledgeTypes);

        requiredTags =
                requiredTags == null
                        ? List.of()
                        : List.copyOf(requiredTags);

        metadata =
                metadata == null
                        ? Map.of()
                        : Map.copyOf(metadata);
    }
}
