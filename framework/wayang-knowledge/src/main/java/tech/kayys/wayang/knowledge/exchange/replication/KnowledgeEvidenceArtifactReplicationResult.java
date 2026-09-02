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


import java.util.List;
import java.util.Map;

public record KnowledgeEvidenceArtifactReplicationResult(

        String replicationId,

        String artifactId,

        int requestedReplicas,

        int successfulReplicas,

        int verifiedReplicas,

        List<String> successfulRuntimeIds,

        List<String> failedRuntimeIds,

        boolean quorumSatisfied,

        Map<String, String> diagnostics

) {

    public KnowledgeEvidenceArtifactReplicationResult {

        successfulRuntimeIds =
                successfulRuntimeIds == null
                        ? List.of()
                        : List.copyOf(successfulRuntimeIds);

        failedRuntimeIds =
                failedRuntimeIds == null
                        ? List.of()
                        : List.copyOf(failedRuntimeIds);

        diagnostics =
                diagnostics == null
                        ? Map.of()
                        : Map.copyOf(diagnostics);
    }
}
