package tech.kayys.wayang.api.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.wayang.knowledge.exchange.compact.InMemoryKnowledgeAnswerArtifactGraphStore;
import tech.kayys.wayang.knowledge.exchange.selection.InMemoryKnowledgeAnswerResolutionDependencyGraph;
import tech.kayys.wayang.knowledge.graph.DefaultKnowledgeGraphProjectionService;
import tech.kayys.wayang.knowledge.graph.KnowledgeGraphProjectionService;
import tech.kayys.wayang.knowledge.graph.KnowledgeGraphQuery;
import tech.kayys.wayang.knowledge.graph.KnowledgeGraphView;
import tech.kayys.wayang.knowledge.lineage.InMemoryKnowledgeLineageStore;
import tech.kayys.wayang.knowledge.snapshot.dependency.InMemoryKnowledgeSnapshotDependencyGraph;

import java.util.Map;

/**
 * REST API exposing the knowledge graph to UI visualization clients.
 *
 * <p>All endpoints return a normalized {@code {nodes, edges, stats}} envelope
 * consumable by any graph rendering library (D3.js, Cytoscape.js, vis.js, etc.).</p>
 *
 * <p>Base path: {@code /api/v1/knowledge/graph}</p>
 *
 * <p>CORS is handled here for direct UI-to-API calls.</p>
 */
@Path("/api/v1/knowledge/graph")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KnowledgeGraphResource {

    private final KnowledgeGraphProjectionService projectionService;

    public KnowledgeGraphResource() {
        this.projectionService = new DefaultKnowledgeGraphProjectionService(
                new InMemoryKnowledgeAnswerArtifactGraphStore(),
                new InMemoryKnowledgeAnswerResolutionDependencyGraph(),
                new InMemoryKnowledgeLineageStore(),
                new InMemoryKnowledgeSnapshotDependencyGraph()
        );
    }

    // ─── OPTIONS preflight for CORS ────────────────────────────────────────────

    @OPTIONS
    @Path("{any:.*}")
    public Response corsPreFlight(@HeaderParam("Origin") String origin) {
        return corsResponse(Response.ok()).build();
    }

    // ─── Artifact graph ────────────────────────────────────────────────────────

    /**
     * Returns the artifact relationship graph for a given answer artifact.
     * Useful for rendering an "answer canvas" showing how artifacts relate.
     */
    @GET
    @Path("/artifact/{artifactId}")
    public Response getArtifactGraph(
            @PathParam("artifactId") String artifactId,
            @QueryParam("tenantId") @DefaultValue("default") String tenantId,
            @QueryParam("workspaceId") @DefaultValue("default") String workspaceId,
            @QueryParam("projectId") String projectId,
            @QueryParam("sessionId") String sessionId,
            @QueryParam("dimensions") @DefaultValue("3") int dimensions) {

        if (artifactId == null || artifactId.isBlank()) {
            return badRequest("artifactId is required");
        }
        KnowledgeGraphQuery query = new KnowledgeGraphQuery(
                artifactId, tenantId, workspaceId, projectId, sessionId,
                KnowledgeGraphQuery.DEFAULT_MAX_DEPTH, dimensions);
        return corsResponse(Response.ok(projectionService.artifactGraph(query))).build();
    }

    // ─── Provenance graph ──────────────────────────────────────────────────────

    /**
     * Returns the provenance graph for a response — answering "why this answer?".
     */
    @GET
    @Path("/provenance/{responseId}")
    public Response getProvenanceGraph(
            @PathParam("responseId") String responseId,
            @QueryParam("tenantId") @DefaultValue("default") String tenantId,
            @QueryParam("workspaceId") @DefaultValue("default") String workspaceId,
            @QueryParam("projectId") String projectId,
            @QueryParam("sessionId") String sessionId,
            @QueryParam("dimensions") @DefaultValue("3") int dimensions) {

        if (responseId == null || responseId.isBlank()) {
            return badRequest("responseId is required");
        }
        KnowledgeGraphQuery query = new KnowledgeGraphQuery(
                responseId, tenantId, workspaceId, projectId, sessionId,
                KnowledgeGraphQuery.DEFAULT_MAX_DEPTH, dimensions);
        return corsResponse(Response.ok(projectionService.provenanceGraph(query))).build();
    }

    // ─── Claim-contradiction graph ─────────────────────────────────────────────

    /**
     * Returns the claim-contradiction graph for a query — showing evidence disputes.
     */
    @GET
    @Path("/claims/{queryId}")
    public Response getClaimGraph(
            @PathParam("queryId") String queryId,
            @QueryParam("tenantId") @DefaultValue("default") String tenantId,
            @QueryParam("workspaceId") @DefaultValue("default") String workspaceId,
            @QueryParam("projectId") String projectId,
            @QueryParam("sessionId") String sessionId,
            @QueryParam("dimensions") @DefaultValue("3") int dimensions) {

        if (queryId == null || queryId.isBlank()) {
            return badRequest("queryId is required");
        }
        KnowledgeGraphQuery query = new KnowledgeGraphQuery(
                queryId, tenantId, workspaceId, projectId, sessionId,
                KnowledgeGraphQuery.DEFAULT_MAX_DEPTH, dimensions);
        return corsResponse(Response.ok(projectionService.claimGraph(query))).build();
    }

    // ─── Evidence fusion graph ────────────────────────────────────────────────

    /**
     * Returns the evidence fusion graph for a session — showing how evidence was merged.
     */
    @GET
    @Path("/fusion/{sessionId}")
    public Response getFusionGraph(
            @PathParam("sessionId") String sessionId,
            @QueryParam("tenantId") @DefaultValue("default") String tenantId,
            @QueryParam("workspaceId") @DefaultValue("default") String workspaceId,
            @QueryParam("projectId") String projectId,
            @QueryParam("dimensions") @DefaultValue("3") int dimensions) {

        if (sessionId == null || sessionId.isBlank()) {
            return badRequest("sessionId is required");
        }
        KnowledgeGraphQuery query = new KnowledgeGraphQuery(
                sessionId, tenantId, workspaceId, projectId, sessionId,
                KnowledgeGraphQuery.DEFAULT_MAX_DEPTH, dimensions);
        return corsResponse(Response.ok(projectionService.fusionGraph(query))).build();
    }

    // ─── Resolution dependency graph ──────────────────────────────────────────

    /**
     * Returns the resolution dependency chain for a resolution ID — the "decision trace".
     */
    @GET
    @Path("/resolution/{resolutionId}")
    public Response getResolutionDependencyGraph(
            @PathParam("resolutionId") String resolutionId,
            @QueryParam("tenantId") @DefaultValue("default") String tenantId,
            @QueryParam("workspaceId") @DefaultValue("default") String workspaceId,
            @QueryParam("projectId") String projectId,
            @QueryParam("sessionId") String sessionId,
            @QueryParam("dimensions") @DefaultValue("3") int dimensions) {

        if (resolutionId == null || resolutionId.isBlank()) {
            return badRequest("resolutionId is required");
        }
        KnowledgeGraphQuery query = new KnowledgeGraphQuery(
                resolutionId, tenantId, workspaceId, projectId, sessionId,
                KnowledgeGraphQuery.DEFAULT_MAX_DEPTH, dimensions);
        return corsResponse(Response.ok(projectionService.resolutionDependencyGraph(query))).build();
    }

    // ─── Lineage graph ────────────────────────────────────────────────────────

    /**
     * Returns lineage ancestors and descendants for a knowledge node.
     * {@code depth} controls traversal depth (default 5, max 10).
     */
    @GET
    @Path("/lineage/{nodeId}")
    public Response getLineageGraph(
            @PathParam("nodeId") String nodeId,
            @QueryParam("tenantId") @DefaultValue("default") String tenantId,
            @QueryParam("workspaceId") @DefaultValue("default") String workspaceId,
            @QueryParam("projectId") String projectId,
            @QueryParam("sessionId") String sessionId,
            @QueryParam("depth") @DefaultValue("5") int depth,
            @QueryParam("dimensions") @DefaultValue("3") int dimensions) {

        if (nodeId == null || nodeId.isBlank()) {
            return badRequest("nodeId is required");
        }
        KnowledgeGraphQuery query = new KnowledgeGraphQuery(
                nodeId, tenantId, workspaceId, projectId, sessionId, depth, dimensions);
        return corsResponse(Response.ok(projectionService.lineageGraph(query))).build();
    }

    // ─── Snapshot dependency graph ────────────────────────────────────────────

    /**
     * Returns the snapshot dependency graph for a given snapshot ID.
     */
    @GET
    @Path("/snapshot/{snapshotId}/deps")
    public Response getSnapshotDependencyGraph(
            @PathParam("snapshotId") String snapshotId,
            @QueryParam("tenantId") @DefaultValue("default") String tenantId,
            @QueryParam("workspaceId") @DefaultValue("default") String workspaceId,
            @QueryParam("projectId") String projectId,
            @QueryParam("sessionId") String sessionId,
            @QueryParam("dimensions") @DefaultValue("3") int dimensions) {

        if (snapshotId == null || snapshotId.isBlank()) {
            return badRequest("snapshotId is required");
        }
        KnowledgeGraphQuery query = new KnowledgeGraphQuery(
                snapshotId, tenantId, workspaceId, projectId, sessionId,
                KnowledgeGraphQuery.DEFAULT_MAX_DEPTH, dimensions);
        return corsResponse(Response.ok(projectionService.snapshotDependencyGraph(query))).build();
    }

    // ─── Full composed graph ──────────────────────────────────────────────────

    /**
     * Returns a composed full knowledge graph scoped by tenant, workspace,
     * and optionally project/session — merging all graph types.
     * Intended for overview dashboards.
     */
    @GET
    @Path("/full")
    public Response getFullGraph(
            @QueryParam("tenantId") @DefaultValue("default") String tenantId,
            @QueryParam("workspaceId") @DefaultValue("default") String workspaceId,
            @QueryParam("projectId") String projectId,
            @QueryParam("sessionId") String sessionId,
            @QueryParam("depth") @DefaultValue("5") int depth,
            @QueryParam("dimensions") @DefaultValue("3") int dimensions) {

        KnowledgeGraphQuery query = new KnowledgeGraphQuery(
                workspaceId, tenantId, workspaceId, projectId, sessionId, depth, dimensions);
        return corsResponse(Response.ok(projectionService.fullGraph(query))).build();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Response badRequest(String message) {
        return corsResponse(
                Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", message)))
                .build();
    }

    private Response.ResponseBuilder corsResponse(Response.ResponseBuilder builder) {
        return builder
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Tenant-Id, X-Workspace-Id");
    }
}
