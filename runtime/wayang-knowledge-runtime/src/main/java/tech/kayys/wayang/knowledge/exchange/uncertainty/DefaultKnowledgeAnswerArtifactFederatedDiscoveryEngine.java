package tech.kayys.wayang.knowledge.exchange.uncertainty;

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
import tech.kayys.wayang.knowledge.exchange.coverage.*;
import tech.kayys.wayang.knowledge.exchange.gap.*;
import tech.kayys.wayang.knowledge.exchange.attribution.*;
import tech.kayys.wayang.knowledge.exchange.contradiction.*;
import tech.kayys.wayang.knowledge.exchange.factuality.*;
import tech.kayys.wayang.knowledge.exchange.uncertainty.*;
import tech.kayys.wayang.knowledge.exchange.compact.*;
import tech.kayys.wayang.knowledge.exchange.resolution.*;
import tech.kayys.wayang.knowledge.exchange.quorum.*;
import tech.kayys.wayang.knowledge.exchange.selection.*;
import tech.kayys.wayang.knowledge.exchange.coordination.*;
import tech.kayys.wayang.knowledge.exchange.attestation.*;
import tech.kayys.wayang.knowledge.exchange.proof.*;
import tech.kayys.wayang.knowledge.exchange.validity.*;
import tech.kayys.wayang.knowledge.exchange.lease.*;
import tech.kayys.wayang.knowledge.exchange.recovery.*;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class DefaultKnowledgeAnswerArtifactFederatedDiscoveryEngine {

    private final KnowledgeAnswerArtifactDiscoveryEngine local;

    private final KnowledgeAnswerArtifactDiscoveryPlanner planner;

    private final List<KnowledgeAnswerArtifactRemoteIndex> remotes;

    public DefaultKnowledgeAnswerArtifactFederatedDiscoveryEngine(
            KnowledgeAnswerArtifactDiscoveryEngine local,
            KnowledgeAnswerArtifactDiscoveryPlanner planner,
            List<KnowledgeAnswerArtifactRemoteIndex> remotes) {

        this.local = local;
        this.planner = planner;
        this.remotes = List.copyOf(remotes);
    }

    public CompletableFuture<
            KnowledgeAnswerArtifactDiscoveryResult> discover(
            KnowledgeAnswerArtifactQuery query,
            List<KnowledgeAnswerArtifactIndexDescriptor> descriptors) {

        var localResult =
                local.discover(query);

        if (!query.allowRemote()) {
            return CompletableFuture.completedFuture(
                    localResult
            );
        }

        var selected =
                planner.select(
                        query,
                        descriptors
                );

        List<CompletableFuture<
                KnowledgeAnswerArtifactDiscoveryResult>> futures =
                new ArrayList<>();

        for (var descriptor : selected) {

            remotes.stream()
                    .filter(remote ->
                            remote.runtimeId()
                                    .equals(
                                            descriptor.runtimeId()
                                    ))
                    .findFirst()
                    .ifPresent(remote ->
                            futures.add(
                                    remote.search(query)
                            )
                    );
        }

        if (futures.isEmpty()) {
            return CompletableFuture.completedFuture(
                    localResult
            );
        }

        return CompletableFuture
                .allOf(
                        futures.toArray(
                                new CompletableFuture[0]
                        )
                )
                .thenApply(ignored -> {

                    List<
                            KnowledgeAnswerArtifactCandidate>
                            all = new ArrayList<>();

                    all.addAll(
                            localResult.candidates()
                    );

                    for (var future : futures) {

                        var result =
                                future.join();

                        all.addAll(
                                result.candidates()
                        );
                    }

                    all.sort(
                            java.util.Comparator
                                    .comparingDouble(
                                            KnowledgeAnswerArtifactCandidate
                                                    ::finalScore
                                    )
                                    .reversed()
                    );

                    var selectedCandidates =
                            all.stream()
                                    .limit(query.limit())
                                    .toList();

                    return new KnowledgeAnswerArtifactDiscoveryResult(
                            query.queryId(),
                            all,
                            selectedCandidates,
                            true,
                            true,
                            java.util.Map.of(
                                    "runtimeCount",
                                    Integer.toString(
                                            selected.size()
                                    )
                            )
                    );
                });
    }
}
