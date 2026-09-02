package tech.kayys.wayang.api.grpc;

import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.knowledge.exchange.resolution.*;
import tech.kayys.wayang.knowledge.exchange.contradiction.*;
import tech.kayys.wayang.knowledge.exchange.factuality.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@GrpcService
public class KnowledgeGrpcService implements KnowledgeService {

    private final DefaultKnowledgeAnswerResolutionPolicy resolutionPolicy =
            new DefaultKnowledgeAnswerResolutionPolicy();
    private final DefaultKnowledgeVerifiedAnswerArtifactVerifier verifier =
            new DefaultKnowledgeVerifiedAnswerArtifactVerifier();
    private final InMemoryKnowledgeVerifiedAnswerArtifactStore artifactStore =
            new InMemoryKnowledgeVerifiedAnswerArtifactStore();

    @Override
    public Uni<KnowledgeQueryProtoReply> queryEvidence(KnowledgeQueryProtoRequest request) {
        // Query simulation or engine lookup
        String text = request.getText();
        String json = "{\"query\":\"" + text + "\",\"status\":\"SUCCESS\",\"results\":[]}";
        return Uni.createFrom().item(
                KnowledgeQueryProtoReply.newBuilder()
                        .setQueryId(request.getQueryId().isBlank() ? "q-" + System.currentTimeMillis() : request.getQueryId())
                        .setTotalHits(0)
                        .setResultsJson(json)
                        .build()
        );
    }

    @Override
    public Uni<KnowledgeResolveProtoReply> resolveConsensus(KnowledgeResolveProtoRequest request) {
        KnowledgeAnswerResolutionCandidate cand = new KnowledgeAnswerResolutionCandidate(
                "cand-" + request.getQuestionId(),
                "runtime-local",
                0.95, 0.9, 0.9, 0.9, 0.9, 0.9, 0.0, 0.92,
                true, true, Map.of()
        );

        KnowledgeAnswerResolutionContext ctx = new KnowledgeAnswerResolutionContext(
                request.getTenantId(),
                request.getWorkspaceId(),
                request.getProjectId(),
                "grpc-client",
                Instant.now(),
                true,
                false,
                Map.of()
        );

        KnowledgeAnswerResolutionDecision decision = resolutionPolicy.decide(List.of(cand), List.of(), ctx);

        return Uni.createFrom().item(
                KnowledgeResolveProtoReply.newBuilder()
                        .setResolutionId("res-" + request.getQuestionId())
                        .setStatus(decision.name())
                        .setWinningAnswer("cand-" + request.getQuestionId())
                        .setConfidence(0.92)
                        .setResolutionJson("{\"status\":\"" + decision.name() + "\",\"confidence\":0.92}")
                        .build()
        );
    }

    @Override
    public Uni<KnowledgeVerifyProtoReply> verifyArtifact(KnowledgeVerifyProtoRequest request) {
        return Uni.createFrom().item(
                KnowledgeVerifyProtoReply.newBuilder()
                        .setArtifactId(request.getArtifactId())
                        .setValid(true)
                        .setVerificationJson("{\"artifactId\":\"" + request.getArtifactId() + "\",\"valid\":true}")
                        .build()
        );
    }

    @Override
    public Uni<KnowledgeCertificateProtoReply> getCertificate(KnowledgeCertificateProtoRequest request) {
        return Uni.createFrom().item(
                KnowledgeCertificateProtoReply.newBuilder()
                        .setConsensusId(request.getConsensusId())
                        .setFound(true)
                        .setCertificateJson("{\"consensusId\":\"" + request.getConsensusId() + "\",\"status\":\"COMMITTED\"}")
                        .build()
        );
    }

    @Override
    public Uni<KnowledgeReconcileProtoReply> reconcileInventory(KnowledgeReconcileProtoRequest request) {
        return Uni.createFrom().item(
                KnowledgeReconcileProtoReply.newBuilder()
                        .setRuntimeId(request.getRuntimeId())
                        .setMissingCount(0)
                        .setReconcileJson("{\"runtimeId\":\"" + request.getRuntimeId() + "\",\"inSync\":true}")
                        .build()
        );
    }
}
