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


import java.util.Set;

public final class DefaultKnowledgeAnswerArtifactIndexingService
        implements KnowledgeAnswerArtifactIndexingService {

    private final KnowledgeAnswerArtifactIndex index;

    public DefaultKnowledgeAnswerArtifactIndexingService(
            KnowledgeAnswerArtifactIndex index) {

        this.index = index;
    }

    @Override
    public void index(
            KnowledgeVerifiedAnswerArtifact artifact) {

        var response =
                artifact.response();

        Set<String> claimTypes =
                response.claims()
                        .stream()
                        .map(claim ->
                                claim.type().name())
                        .collect(
                                java.util.stream.Collectors.toSet()
                        );

        Set<String> concepts =
                response.claims()
                        .stream()
                        .flatMap(claim ->
                                java.util.Arrays.stream(
                                        claim.text()
                                                .toLowerCase()
                                                .split("\\W+")
                                ))
                        .filter(s -> !s.isBlank())
                        .collect(
                                java.util.stream.Collectors.toSet()
                        );

        var entry =
                new KnowledgeAnswerArtifactIndexEntry(
                        artifact.artifactId(),
                        artifact.responseId(),
                        artifact.snapshotId(),
                        "local",
                        artifact.tenantId(),
                        artifact.workspaceId(),
                        artifact.projectId(),
                        artifact.agentId(),
                        response.status().name(),
                        response.disposition().name(),
                        response.confidence(),
                        response.status()
                                == KnowledgeVerifiedResponseStatus
                                .VERIFIED,
                        false,
                        artifact.createdAt(),
                        artifact.createdAt(),
                        Set.of(),
                        concepts,
                        claimTypes,
                        artifact.responseFingerprint(),
                        java.util.Map.of()
                );

        index.index(entry);
    }

    @Override
    public void remove(
            String artifactId) {

        index.remove(artifactId);
    }
}
