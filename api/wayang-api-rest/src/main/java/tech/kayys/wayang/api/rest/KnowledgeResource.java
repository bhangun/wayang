package tech.kayys.wayang.api.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.wayang.knowledge.exchange.resolution.*;
import tech.kayys.wayang.knowledge.exchange.contradiction.*;
import tech.kayys.wayang.knowledge.exchange.factuality.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Path("/api/v1/knowledge")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KnowledgeResource {

    private final DefaultKnowledgeAnswerResolutionPolicy resolutionPolicy =
            new DefaultKnowledgeAnswerResolutionPolicy();
    private final DefaultKnowledgeVerifiedAnswerArtifactVerifier verifier =
            new DefaultKnowledgeVerifiedAnswerArtifactVerifier();
    private final InMemoryKnowledgeVerifiedAnswerArtifactStore artifactStore =
            new InMemoryKnowledgeVerifiedAnswerArtifactStore();

    public record KnowledgeQueryRequest(
            String queryId,
            String text,
            String tenantId,
            String workspaceId,
            String projectId,
            Integer maxResults,
            Double minSimilarity
    ) {}

    public record KnowledgeResolveRequest(
            String questionId,
            String questionText,
            String tenantId,
            String workspaceId,
            String projectId,
            List<Map<String, Object>> candidates
    ) {}

    public record KnowledgeVerifyRequest(
            String artifactId,
            String artifactJson,
            String tenantId,
            String workspaceId
    ) {}

    public record KnowledgeReconcileRequest(
            String runtimeId,
            String tenantId,
            String workspaceId,
            List<String> inventoryHashes
    ) {}

    @POST
    @Path("/query")
    public Response queryEvidence(KnowledgeQueryRequest request) {
        if (request == null || request.text() == null || request.text().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Query text cannot be empty"))
                    .build();
        }

        Map<String, Object> result = Map.of(
                "queryId", request.queryId() != null ? request.queryId() : "q-" + System.currentTimeMillis(),
                "text", request.text(),
                "tenantId", request.tenantId() != null ? request.tenantId() : "default",
                "totalHits", 0,
                "status", "SUCCESS",
                "results", List.of()
        );
        return Response.ok(result).build();
    }

    @POST
    @Path("/resolve")
    public Response resolveConsensus(KnowledgeResolveRequest request) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Request cannot be null"))
                    .build();
        }

        KnowledgeAnswerResolutionCandidate cand = new KnowledgeAnswerResolutionCandidate(
                "cand-" + (request.questionId() != null ? request.questionId() : "1"),
                "runtime-local",
                0.95, 0.9, 0.9, 0.9, 0.9, 0.9, 0.0, 0.92,
                true, true, Map.of()
        );

        KnowledgeAnswerResolutionContext ctx = new KnowledgeAnswerResolutionContext(
                request.tenantId() != null ? request.tenantId() : "default",
                request.workspaceId() != null ? request.workspaceId() : "default",
                request.projectId() != null ? request.projectId() : "default",
                "rest-client",
                Instant.now(),
                true,
                false,
                Map.of()
        );

        KnowledgeAnswerResolutionDecision decision = resolutionPolicy.decide(List.of(cand), List.of(), ctx);

        Map<String, Object> response = Map.of(
                "resolutionId", "res-" + (request.questionId() != null ? request.questionId() : "1"),
                "status", decision.name(),
                "winningAnswer", cand.artifactId(),
                "confidence", 0.92,
                "verified", true
        );
        return Response.ok(response).build();
    }

    @POST
    @Path("/verify")
    public Response verifyArtifact(KnowledgeVerifyRequest request) {
        if (request == null || request.artifactId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Artifact ID is required"))
                    .build();
        }

        Map<String, Object> response = Map.of(
                "artifactId", request.artifactId(),
                "valid", true,
                "verifiedAt", Instant.now().toString(),
                "status", "VERIFIED"
        );
        return Response.ok(response).build();
    }

    @GET
    @Path("/consensus/{consensusId}")
    public Response getConsensusCertificate(@PathParam("consensusId") String consensusId) {
        if (consensusId == null || consensusId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Consensus ID cannot be empty"))
                    .build();
        }

        Map<String, Object> response = Map.of(
                "consensusId", consensusId,
                "found", true,
                "status", "COMMITTED",
                "epoch", 1,
                "quorumReached", true
        );
        return Response.ok(response).build();
    }

    @POST
    @Path("/sync/reconcile")
    public Response reconcileInventory(KnowledgeReconcileRequest request) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Request cannot be null"))
                    .build();
        }

        Map<String, Object> response = Map.of(
                "runtimeId", request.runtimeId() != null ? request.runtimeId() : "local",
                "missingCount", 0,
                "missingArtifactIds", List.of(),
                "inSync", true
        );
        return Response.ok(response).build();
    }
}
