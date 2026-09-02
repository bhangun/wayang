package tech.kayys.wayang.knowledge.exchange.transfer;

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
import java.util.Map;

public record KnowledgeEvidenceTransferSession(

        String transferId,

        KnowledgeEvidenceTransferOperation operation,

        String sessionId,

        String streamId,

        String artifactId,

        String resourceId,

        long startOffset,

        long currentOffset,

        long totalLength,

        long nextSequence,

        KnowledgeEvidenceTransferState state,

        String artifactFingerprint,

        String resourceFingerprint,

        String merkleRoot,

        Instant createdAt,

        Instant updatedAt,

        Map<String, String> metadata

) {

    public KnowledgeEvidenceTransferSession {

        metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }

    public boolean terminal() {

        return switch (state) {

            case COMPLETED,
                 CANCELLED,
                 FAILED,
                 EXPIRED -> true;

            default -> false;
        };
    }
}
