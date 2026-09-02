package tech.kayys.wayang.knowledge.exchange.sync;

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

public record KnowledgeEvidenceArtifactReconciliationResult(

        KnowledgeEvidenceArtifactConsistencyState state,

        String localRuntimeId,

        String remoteRuntimeId,

        List<String> missingLocally,

        List<String> missingRemotely,

        List<String> divergent,

        List<String> revoked,

        Map<String, String> diagnostics

) {

    public KnowledgeEvidenceArtifactReconciliationResult {

        missingLocally =
                missingLocally == null
                        ? List.of()
                        : List.copyOf(missingLocally);

        missingRemotely =
                missingRemotely == null
                        ? List.of()
                        : List.copyOf(missingRemotely);

        divergent =
                divergent == null
                        ? List.of()
                        : List.copyOf(divergent);

        revoked =
                revoked == null
                        ? List.of()
                        : List.copyOf(revoked);

        diagnostics =
                diagnostics == null
                        ? Map.of()
                        : Map.copyOf(diagnostics);
    }

    public boolean converged() {

        return state ==
                KnowledgeEvidenceArtifactConsistencyState.CONSISTENT;
    }
}
