package tech.kayys.wayang.knowledge.exchange.replication;

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


import java.util.Map;

public record KnowledgeEvidenceArtifactReplicationPolicy(

        int minimumReplicas,

        int targetReplicas,

        boolean requireVerification,

        boolean allowRemoteReplication,

        boolean allowOfflineQueue,

        long maxArtifactBytes,

        Map<String, String> metadata

) {

    public KnowledgeEvidenceArtifactReplicationPolicy {

        if (minimumReplicas < 1) {
            throw new IllegalArgumentException(
                    "minimumReplicas must be >= 1"
            );
        }

        if (targetReplicas < minimumReplicas) {
            throw new IllegalArgumentException(
                    "targetReplicas must be >= minimumReplicas"
            );
        }

        metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }

    public static KnowledgeEvidenceArtifactReplicationPolicy defaults() {

        return new KnowledgeEvidenceArtifactReplicationPolicy(
                1,
                2,
                true,
                true,
                true,
                4L * 1024L * 1024L * 1024L,
                Map.of()
        );
    }
}
