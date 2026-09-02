package tech.kayys.wayang.api.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.wayang.memory.visual.DefaultMemoryVisualProjectionService;
import tech.kayys.wayang.memory.visual.MemoryVisualProjectionService;
import tech.kayys.wayang.memory.visual.MemoryVisualQuery;
import tech.kayys.wayang.memory.visual.MemoryVisualView;

import java.util.Map;

/**
 * REST API exposing agent and system memory for UI visualization.
 *
 * <p>Base path: {@code /api/v1/memory/visualize}</p>
 *
 * <p>Endpoints provide views for:
 * <ul>
 *   <li>4-tier memory hierarchy overview (Working, Episodic, Semantic, Procedural, Long-Term)</li>
 *   <li>Semantic concepts & facts network graph (D3.js / Cytoscape)</li>
 *   <li>2D/3D embedding scatter points with category tags (Plotly / Deck.gl)</li>
 *   <li>Real-time working memory attention weights & active facts</li>
 *   <li>Learned procedural task patterns and skill proficiencies</li>
 *   <li>Episodic timeline & decay curves</li>
 *   <li>Full composed visualization envelope</li>
 * </ul>
 * </p>
 */
@Path("/api/v1/memory/visualize")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MemoryVisualizationResource {

    private final MemoryVisualProjectionService projectionService;

    public MemoryVisualizationResource() {
        this.projectionService = new DefaultMemoryVisualProjectionService();
    }

    public MemoryVisualizationResource(MemoryVisualProjectionService projectionService) {
        this.projectionService = projectionService;
    }

    // ─── OPTIONS preflight for CORS ────────────────────────────────────────────

    @OPTIONS
    @Path("{any:.*}")
    public Response corsPreFlight(@HeaderParam("Origin") String origin) {
        return corsResponse(Response.ok()).build();
    }

    // ─── 4-Tier Memory Hierarchy Overview ──────────────────────────────────────

    /**
     * Returns the 4-tier memory hierarchy summary and key retention insights.
     */
    @GET
    @Path("/overview/{agentId}")
    public Response getOverview(
            @PathParam("agentId") String agentId,
            @QueryParam("userId") String userId,
            @QueryParam("tenantId") @DefaultValue("default") String tenantId,
            @QueryParam("workspaceId") @DefaultValue("default") String workspaceId,
            @QueryParam("sessionId") String sessionId) {

        if (agentId == null || agentId.isBlank()) {
            return badRequest("agentId is required");
        }
        MemoryVisualQuery query = new MemoryVisualQuery(
                agentId, userId, tenantId, workspaceId, sessionId, null, 100);
        MemoryVisualView view = projectionService.overview(query);
        return corsResponse(Response.ok(view)).build();
    }

    // ─── Semantic Concept & Fact Graph ─────────────────────────────────────────

    /**
     * Returns the semantic knowledge concepts & facts network for force-directed graph rendering.
     */
    @GET
    @Path("/graph/{agentId}")
    public Response getGraph(
            @PathParam("agentId") String agentId,
            @QueryParam("userId") String userId,
            @QueryParam("tenantId") @DefaultValue("default") String tenantId,
            @QueryParam("workspaceId") @DefaultValue("default") String workspaceId,
            @QueryParam("limit") @DefaultValue("100") int limit) {

        if (agentId == null || agentId.isBlank()) {
            return badRequest("agentId is required");
        }
        MemoryVisualQuery query = new MemoryVisualQuery(
                agentId, userId, tenantId, workspaceId, null, null, limit);
        MemoryVisualView view = projectionService.graph(query);
        return corsResponse(Response.ok(view)).build();
    }

    // ─── Vector / Semantic Embedding Map ───────────────────────────────────────

    /**
     * Returns 2D/3D projected embedding coordinates with category tags for scatter plot visualization.
     */
    @GET
    @Path("/vectors/{agentId}")
    public Response getVectors(
            @PathParam("agentId") String agentId,
            @QueryParam("userId") String userId,
            @QueryParam("tenantId") @DefaultValue("default") String tenantId,
            @QueryParam("workspaceId") @DefaultValue("default") String workspaceId,
            @QueryParam("category") String category,
            @QueryParam("limit") @DefaultValue("200") int limit) {

        if (agentId == null || agentId.isBlank()) {
            return badRequest("agentId is required");
        }
        MemoryVisualQuery query = new MemoryVisualQuery(
                agentId, userId, tenantId, workspaceId, null, category, limit);
        MemoryVisualView view = projectionService.vectors(query);
        return corsResponse(Response.ok(view)).build();
    }

    // ─── Working Memory Attention State ────────────────────────────────────────

    /**
     * Returns current working memory attention weights, token budget, and active facts for a session.
     */
    @GET
    @Path("/working/{sessionId}")
    public Response getWorkingMemory(
            @PathParam("sessionId") String sessionId,
            @QueryParam("agentId") @DefaultValue("default") String agentId,
            @QueryParam("tenantId") @DefaultValue("default") String tenantId,
            @QueryParam("workspaceId") @DefaultValue("default") String workspaceId) {

        if (sessionId == null || sessionId.isBlank()) {
            return badRequest("sessionId is required");
        }
        MemoryVisualQuery query = new MemoryVisualQuery(
                agentId, null, tenantId, workspaceId, sessionId, null, 50);
        MemoryVisualView view = projectionService.working(query);
        return corsResponse(Response.ok(view)).build();
    }

    // ─── Procedural Memory Patterns & Skills ───────────────────────────────────

    /**
     * Returns learned task patterns, action sequences, and skill proficiencies for a user/agent.
     */
    @GET
    @Path("/procedural/{userId}")
    public Response getProceduralMemory(
            @PathParam("userId") String userId,
            @QueryParam("agentId") @DefaultValue("default") String agentId,
            @QueryParam("tenantId") @DefaultValue("default") String tenantId,
            @QueryParam("workspaceId") @DefaultValue("default") String workspaceId) {

        if (userId == null || userId.isBlank()) {
            return badRequest("userId is required");
        }
        MemoryVisualQuery query = new MemoryVisualQuery(
                agentId, userId, tenantId, workspaceId, null, null, 100);
        MemoryVisualView view = projectionService.procedural(query);
        return corsResponse(Response.ok(view)).build();
    }

    // ─── Episodic Timeline ────────────────────────────────────────────────────

    /**
     * Returns episodic chronological memory progression & retention decay metrics.
     */
    @GET
    @Path("/timeline/{agentId}")
    public Response getTimeline(
            @PathParam("agentId") String agentId,
            @QueryParam("userId") String userId,
            @QueryParam("tenantId") @DefaultValue("default") String tenantId,
            @QueryParam("workspaceId") @DefaultValue("default") String workspaceId,
            @QueryParam("limit") @DefaultValue("50") int limit) {

        if (agentId == null || agentId.isBlank()) {
            return badRequest("agentId is required");
        }
        MemoryVisualQuery query = new MemoryVisualQuery(
                agentId, userId, tenantId, workspaceId, null, null, limit);
        MemoryVisualView view = projectionService.timeline(query);
        return corsResponse(Response.ok(view)).build();
    }

    // ─── Full Composed Memory Visualization ────────────────────────────────────

    /**
     * Returns the full composed memory visualization envelope across all tiers.
     */
    @GET
    @Path("/full/{agentId}")
    public Response getFullView(
            @PathParam("agentId") String agentId,
            @QueryParam("userId") String userId,
            @QueryParam("tenantId") @DefaultValue("default") String tenantId,
            @QueryParam("workspaceId") @DefaultValue("default") String workspaceId,
            @QueryParam("sessionId") String sessionId,
            @QueryParam("limit") @DefaultValue("100") int limit) {

        if (agentId == null || agentId.isBlank()) {
            return badRequest("agentId is required");
        }
        MemoryVisualQuery query = new MemoryVisualQuery(
                agentId, userId, tenantId, workspaceId, sessionId, null, limit);
        MemoryVisualView view = projectionService.full(query);
        return corsResponse(Response.ok(view)).build();
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
