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


import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class KnowledgeVerifiedAnswerDiscoveryService {

    private final DefaultKnowledgeAnswerArtifactFederatedDiscoveryEngine
            discovery;

    private final DefaultKnowledgeVerifiedAnswerExchangeService
            exchange;

    public KnowledgeVerifiedAnswerDiscoveryService(
            DefaultKnowledgeAnswerArtifactFederatedDiscoveryEngine discovery,
            DefaultKnowledgeVerifiedAnswerExchangeService exchange) {

        this.discovery = discovery;
        this.exchange = exchange;
    }

    public CompletableFuture<
            List<KnowledgeAnswerArtifactCandidate>> discoverVerified(
            KnowledgeAnswerArtifactQuery query,
            List<KnowledgeAnswerArtifactIndexDescriptor> descriptors) {

        return discovery
                .discover(query, descriptors)
                .thenCompose(result -> {

                    List<CompletableFuture<
                            KnowledgeAnswerArtifactCandidate>>
                            verified =
                            result.selected()
                                    .stream()
                                    .map(this::verify)
                                    .toList();

                    return CompletableFuture
                            .allOf(
                                    verified.toArray(
                                            new CompletableFuture[0]
                                    )
                            )
                            .thenApply(ignored ->
                                    verified.stream()
                                            .map(CompletableFuture::join)
                                            .filter(
                                                    java.util.Objects
                                                            ::nonNull
                                            )
                                            .toList()
                            );
                });
    }

    private CompletableFuture<
            KnowledgeAnswerArtifactCandidate> verify(
            KnowledgeAnswerArtifactCandidate candidate) {

        var request =
                new KnowledgeVerifiedAnswerExchangeRequest(
                        java.util.UUID.randomUUID()
                                .toString(),
                        KnowledgeVerifiedAnswerExchangeOperation
                                .GET_ARTIFACT,
                        candidate.artifactId(),
                        candidate.responseId(),
                        null,
                        candidate.tenantId(),
                        candidate.workspaceId(),
                        candidate.projectId(),
                        candidate.runtimeId(),
                        true,
                        true,
                        true,
                        candidate.sealed(),
                        true,
                        java.time.Instant.now(),
                        java.time.Instant.now()
                                .plusSeconds(60),
                        java.util.Map.of()
                );

        return exchange.fetch(request)
                .thenApply(result ->
                        result.accepted()
                                ? candidate
                                : null
                );
    }
}
