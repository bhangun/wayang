package tech.kayys.wayang.node.resource;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import tech.kayys.wayang.exception.ConflictException;
import tech.kayys.wayang.exception.NotFoundException;
import tech.kayys.wayang.node.service.NodeTypeService;
import tech.kayys.wayang.node.dto.*;

import java.util.List;

/**
 * REST API for managing and discovering node types.
 * 
 * This resource provides:
 * - Complete catalog of built-in node types
 * - Node type discovery and search
 * - Detailed node schemas for UI rendering
 * - Plugin node type registration
 * - Custom node type management
 * 
 * Frontend Integration:
 * - Node palette for workflow designer
 * - Dynamic form generation for node properties
 * - Port compatibility checking
 * - Category-based filtering
 * 
 * @since 1.0.0
 */
@Path("/api/v1/node-types")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Node Types", description = "Node type management and discovery")
public class NodeTypeResource {

        private static final Logger LOG = Logger.getLogger(NodeTypeResource.class);

        @Inject
        NodeTypeService nodeTypeService;

        @Context
        SecurityContext securityContext;

        /**
         * Get complete catalog of all available node types.
         * 
         * Returns organized catalog with:
         * - Built-in nodes (Agent, Integration, Control Flow, Data, Human, Utility,
         * Error)
         * - Plugin nodes (registered by tenant)
         * - Custom nodes (tenant-specific)
         * 
         * Response includes full node schemas for UI rendering.
         * 
         * @return Complete node type catalog
         */
        @GET
        @Path("/catalog")
        @Operation(summary = "Get node types catalog", description = "Returns complete catalog of all available node types organized by category")
        public Uni<Response> getNodeTypeCatalog() {
                String tenantId = getTenantId();
                LOG.infof("Fetching node type catalog for tenant: %s", tenantId);

                return nodeTypeService.getNodeTypeCatalog(tenantId)
                                .map(catalog -> Response.ok(catalog).build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to fetch node type catalog", th);
                                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        /**
         * Get detailed descriptor for specific node type.
         * 
         * Returns complete node specification including:
         * - Input/output port schemas
         * - Configuration properties with validation
         * - Error handling specifications
         * - UI metadata (icon, color, form layout)
         * - Documentation and examples
         * 
         * @param nodeTypeId Node type identifier (e.g., "builtin.agent.llm-completion")
         * @return Node type descriptor
         */
        @GET
        @Path("/{nodeTypeId}")
        @Operation(summary = "Get node type details", description = "Returns detailed schema and configuration for specific node type")
        public Uni<Response> getNodeTypeDescriptor(
                        @PathParam("nodeTypeId") String nodeTypeId) {

                String tenantId = getTenantId();
                LOG.infof("Fetching node type: %s for tenant: %s", nodeTypeId, tenantId);

                return nodeTypeService.getNodeTypeDescriptor(nodeTypeId, tenantId)
                                .map(descriptor -> Response.ok(descriptor).build())
                                .onFailure().recoverWithItem(th -> {
                                        if (th instanceof NotFoundException) {
                                                return Response.status(Response.Status.NOT_FOUND)
                                                                .entity(ErrorResponse.notFound(
                                                                                "Node type not found: " + nodeTypeId))
                                                                .build();
                                        }
                                        LOG.error("Failed to fetch node type descriptor", th);
                                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        /**
         * Search node types by query and category.
         * 
         * Supports:
         * - Full-text search across name, description, tags
         * - Category filtering (Agent, Integration, etc.)
         * - Capability filtering (network, llm_access, etc.)
         * - Fuzzy matching for better UX
         * 
         * @param query        Search query string
         * @param category     Filter by category (optional)
         * @param capabilities Filter by capabilities (optional)
         * @return List of matching node types
         */
        @GET
        @Path("/search")
        @Operation(summary = "Search node types", description = "Search and filter node types by query, category, or capabilities")
        public Uni<Response> searchNodeTypes(
                        @QueryParam("q") String query,
                        @QueryParam("category") String category,
                        @QueryParam("capabilities") List<String> capabilities) {

                String tenantId = getTenantId();
                LOG.infof("Searching node types: query=%s, category=%s, tenant=%s",
                                query, category, tenantId);

                return nodeTypeService.searchNodeTypes(query, category, tenantId)
                                .map(results -> Response.ok(results).build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to search node types", th);
                                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        /**
         * Get node types by category.
         * 
         * Categories:
         * - Agent: AI-powered reasoning and decision making
         * - Integration: System connectors and data flow
         * - Control Flow: Workflow logic and routing
         * - Data: Data manipulation and validation
         * - Human: Human-in-the-loop operations
         * - Utility: Common utility operations
         * - Error: Error management and recovery
         * 
         * @param category Category name
         * @return List of node types in category
         */
        @GET
        @Path("/category/{category}")
        @Operation(summary = "Get nodes by category", description = "Returns all node types in specified category")
        public Uni<Response> getNodeTypesByCategory(
                        @PathParam("category") String category) {

                String tenantId = getTenantId();
                LOG.infof("Fetching node types for category: %s, tenant: %s",
                                category, tenantId);

                return nodeTypeService.getNodeTypesByCategory(category, tenantId)
                                .map(nodes -> Response.ok(nodes).build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to fetch node types by category", th);
                                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        /**
         * Validate node configuration.
         * 
         * Validates:
         * - Required properties present
         * - Property types correct
         * - Property values within constraints
         * - Input/output port compatibility
         * 
         * @param nodeTypeId Node type identifier
         * @param config     Node configuration to validate
         * @return Validation result with errors if any
         */
        @POST
        @Path("/{nodeTypeId}/validate")
        @Operation(summary = "Validate node configuration", description = "Validates node configuration against node type schema")
        public Uni<Response> validateNodeConfig(
                        @PathParam("nodeTypeId") String nodeTypeId,
                        NodeConfigRequest config) {

                String tenantId = getTenantId();
                LOG.infof("Validating config for node type: %s", nodeTypeId);

                return nodeTypeService.validateNodeConfig(nodeTypeId, config, tenantId)
                                .map(result -> Response.ok(result).build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to validate node config", th);
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        /**
         * Get node type suggestions based on context.
         * 
         * Provides intelligent suggestions for:
         * - Next node after current selection
         * - Compatible nodes based on output ports
         * - Common patterns and templates
         * 
         * @param context Suggestion context (current node, output port)
         * @return List of suggested node types
         */
        @POST
        @Path("/suggestions")
        @Operation(summary = "Get node suggestions", description = "Returns intelligent node type suggestions based on context")
        public Uni<Response> getNodeSuggestions(NodeSuggestionRequest context) {
                String tenantId = getTenantId();
                LOG.infof("Fetching node suggestions for tenant: %s", tenantId);

                return nodeTypeService.getNodeSuggestions(context, tenantId)
                                .map(suggestions -> Response.ok(suggestions).build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to fetch node suggestions", th);
                                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        /**
         * Register custom/plugin node type.
         * 
         * Allows tenants to:
         * - Register custom node implementations
         * - Add plugin-based nodes
         * - Define private node types
         * 
         * Requires plugin validation and security scanning.
         * 
         * @param descriptor Custom node type descriptor
         * @return Registered node type
         */
        @POST
        @Path("/custom")
        @Operation(summary = "Register custom node type", description = "Registers custom or plugin-based node type for tenant")
        public Uni<Response> registerCustomNodeType(CustomNodeTypeDescriptor descriptor) {
                String tenantId = getTenantId();
                String userId = getUserId();

                LOG.infof("Registering custom node type: %s by user: %s",
                                descriptor.getId(), userId);

                return nodeTypeService.registerCustomNodeType(descriptor, tenantId, userId)
                                .map(registered -> Response
                                                .status(Response.Status.CREATED)
                                                .entity(registered)
                                                .build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to register custom node type", th);
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        /**
         * Update custom node type.
         * 
         * @param nodeTypeId Node type identifier
         * @param descriptor Updated descriptor
         * @return Updated node type
         */
        @PUT
        @Path("/custom/{nodeTypeId}")
        @Operation(summary = "Update custom node type")
        public Uni<Response> updateCustomNodeType(
                        @PathParam("nodeTypeId") String nodeTypeId,
                        CustomNodeTypeDescriptor descriptor) {

                String tenantId = getTenantId();
                String userId = getUserId();

                LOG.infof("Updating custom node type: %s by user: %s", nodeTypeId, userId);

                return nodeTypeService.updateCustomNodeType(
                                nodeTypeId, descriptor, tenantId, userId)
                                .map(updated -> Response.ok(updated).build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to update custom node type", th);
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        /**
         * Delete custom node type.
         * 
         * Only allowed if node type is not used in any workflow.
         * 
         * @param nodeTypeId Node type identifier
         * @return No content on success
         */
        @DELETE
        @Path("/custom/{nodeTypeId}")
        @Operation(summary = "Delete custom node type")
        public Uni<Response> deleteCustomNodeType(
                        @PathParam("nodeTypeId") String nodeTypeId) {

                String tenantId = getTenantId();
                String userId = getUserId();

                LOG.infof("Deleting custom node type: %s by user: %s", nodeTypeId, userId);

                return nodeTypeService.deleteCustomNodeType(nodeTypeId, tenantId, userId)
                                .map(v -> Response.noContent().build())
                                .onFailure().recoverWithItem(th -> {
                                        if (th instanceof ConflictException) {
                                                return Response.status(Response.Status.CONFLICT)
                                                                .entity(ErrorResponse.conflict(
                                                                                "Node type is in use and cannot be deleted"))
                                                                .build();
                                        }
                                        LOG.error("Failed to delete custom node type", th);
                                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        /**
         * Get node type usage statistics.
         * 
         * Shows:
         * - Number of workflows using this node type
         * - Total executions
         * - Success/failure rates
         * - Average execution time
         * 
         * @param nodeTypeId Node type identifier
         * @return Usage statistics
         */
        @GET
        @Path("/{nodeTypeId}/stats")
        @Operation(summary = "Get node type usage statistics")
        public Uni<Response> getNodeTypeStats(
                        @PathParam("nodeTypeId") String nodeTypeId,
                        @QueryParam("from") String fromDate,
                        @QueryParam("to") String toDate) {

                String tenantId = getTenantId();
                LOG.infof("Fetching stats for node type: %s", nodeTypeId);

                return nodeTypeService.getNodeTypeStats(nodeTypeId, tenantId, fromDate, toDate)
                                .map(stats -> Response.ok(stats).build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to fetch node type stats", th);
                                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        /**
         * Get node type documentation.
         * 
         * Returns:
         * - Usage guide
         * - Configuration examples
         * - Best practices
         * - Common patterns
         * 
         * @param nodeTypeId Node type identifier
         * @return Documentation content
         */
        @GET
        @Path("/{nodeTypeId}/docs")
        @Produces(MediaType.TEXT_HTML)
        @Operation(summary = "Get node type documentation")
        public Uni<Response> getNodeTypeDocumentation(
                        @PathParam("nodeTypeId") String nodeTypeId) {

                String tenantId = getTenantId();
                LOG.infof("Fetching documentation for node type: %s", nodeTypeId);

                return nodeTypeService.getNodeTypeDocumentation(nodeTypeId, tenantId)
                                .map(docs -> Response.ok(docs).build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to fetch node type documentation", th);
                                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        // ========================================================================
        // HELPER METHODS
        // ========================================================================

        private String getTenantId() {
                // Extract from JWT or security context
                // For now, return default tenant
                return "default-tenant";
        }

        private String getUserId() {
                if (securityContext.getUserPrincipal() != null) {
                        return securityContext.getUserPrincipal().getName();
                }
                return "anonymous";
        }
}
