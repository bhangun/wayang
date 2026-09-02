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


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class DefaultKnowledgeEvidenceArtifactReplicationCoordinator
        implements KnowledgeEvidenceArtifactReplicationCoordinator {

    private final List<
            KnowledgeEvidenceArtifactRemoteReplica
            > replicas;

    public DefaultKnowledgeEvidenceArtifactReplicationCoordinator(
            List<
                    KnowledgeEvidenceArtifactRemoteReplica
                    > replicas
    ) {

        this.replicas =
                replicas == null
                        ? List.of()
                        : List.copyOf(replicas);
    }

    @Override
    public CompletableFuture<
            KnowledgeEvidenceArtifactReplicationResult
            > replicate(
                    KnowledgeEvidenceArtifactReplicationRequest request
            ) {

        return CompletableFuture.supplyAsync(() -> {

            if (request.expiredAt(
                    Instant.now()
            )) {

                throw new KnowledgeEvidenceExchangeTransportException(
                        "Replication request expired"
                );
            }

            var successful =
                    new ArrayList<String>();

            var failed =
                    new ArrayList<String>();

            int verified = 0;

            for (
                    var runtimeId :
                            request.targetRuntimeIds()
            ) {

                var replica =
                        replicas.stream()
                                .filter(
                                        r ->
                                                r.runtimeId()
                                                        .equals(runtimeId)
                                )
                                .findFirst()
                                .orElse(null);

                if (replica == null) {

                    failed.add(runtimeId);

                    continue;
                }

                try {

                    var result =
                            replica.replicate(
                                    request
                            ).join();

                    if (result.quorumSatisfied()) {

                        successful.add(
                                runtimeId
                        );

                        verified +=
                                result.verifiedReplicas();

                    } else {

                        failed.add(
                                runtimeId
                        );
                    }

                } catch (Exception e) {

                    failed.add(
                            runtimeId
                    );
                }
            }

            int desired =
                    request.desiredReplicas();

            boolean quorum =
                    successful.size() >= desired;

            return new KnowledgeEvidenceArtifactReplicationResult(
                    request.replicationId(),
                    request.artifactId(),
                    request.targetRuntimeIds().size(),
                    successful.size(),
                    verified,
                    successful,
                    failed,
                    quorum,
                    java.util.Map.of(
                            "sourceRuntime",
                            String.valueOf(
                                    request.sourceRuntimeId()
                            )
                    )
            );
        });
    }
}
