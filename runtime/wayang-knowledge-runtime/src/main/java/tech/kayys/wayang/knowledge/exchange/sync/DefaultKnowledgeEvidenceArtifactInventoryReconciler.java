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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public final class DefaultKnowledgeEvidenceArtifactInventoryReconciler
        implements KnowledgeEvidenceArtifactInventoryReconciler {

    @Override
    public KnowledgeEvidenceArtifactReconciliationResult reconcile(
            KnowledgeEvidenceArtifactInventorySnapshot local,
            KnowledgeEvidenceArtifactInventorySnapshot remote
    ) {

        var localMap =
                new HashMap<String,
                        KnowledgeEvidenceArtifactInventoryEntry>();

        var remoteMap =
                new HashMap<String,
                        KnowledgeEvidenceArtifactInventoryEntry>();

        local.entries().forEach(
                e -> localMap.put(
                        e.artifactId(),
                        e
                )
        );

        remote.entries().forEach(
                e -> remoteMap.put(
                        e.artifactId(),
                        e
                )
        );

        var missingLocally =
                new ArrayList<String>();

        var missingRemotely =
                new ArrayList<String>();

        var divergent =
                new ArrayList<String>();

        var revoked =
                new ArrayList<String>();

        var allIds =
                new HashSet<String>();

        allIds.addAll(localMap.keySet());
        allIds.addAll(remoteMap.keySet());

        for (String id : allIds) {

            var l = localMap.get(id);
            var r = remoteMap.get(id);

            if (l == null) {

                missingLocally.add(id);
                continue;
            }

            if (r == null) {

                missingRemotely.add(id);
                continue;
            }

            if (l.revoked() || r.revoked()) {

                revoked.add(id);
                continue;
            }

            boolean same =
                    java.util.Objects.equals(
                            l.fingerprint(),
                            r.fingerprint()
                    )
                    &&
                    java.util.Objects.equals(
                            l.merkleRoot(),
                            r.merkleRoot()
                    )
                    &&
                    l.size() == r.size();

            if (!same) {
                divergent.add(id);
            }
        }

        KnowledgeEvidenceArtifactConsistencyState state;

        if (!divergent.isEmpty()) {

            state =
                    KnowledgeEvidenceArtifactConsistencyState
                            .DIVERGENT;

        } else if (!missingLocally.isEmpty()) {

            state =
                    KnowledgeEvidenceArtifactConsistencyState
                            .MISSING_LOCAL;

        } else if (!missingRemotely.isEmpty()) {

            state =
                    KnowledgeEvidenceArtifactConsistencyState
                            .MISSING_REMOTE;

        } else if (!revoked.isEmpty()) {

            state =
                    KnowledgeEvidenceArtifactConsistencyState
                            .REVOKED;

        } else {

            state =
                    KnowledgeEvidenceArtifactConsistencyState
                            .CONSISTENT;
        }

        return new KnowledgeEvidenceArtifactReconciliationResult(
                state,
                local.runtimeId(),
                remote.runtimeId(),
                missingLocally,
                missingRemotely,
                divergent,
                revoked,
                java.util.Map.of(
                        "localInventory",
                        String.valueOf(
                                local.inventoryFingerprint()
                        ),
                        "remoteInventory",
                        String.valueOf(
                                remote.inventoryFingerprint()
                        )
                )
        );
    }
}
