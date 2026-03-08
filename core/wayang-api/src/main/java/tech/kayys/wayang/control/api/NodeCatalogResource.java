package tech.kayys.wayang.control.api;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;

import tech.kayys.wayang.plugin.registry.ControlPlaneNodeRegistry;
import tech.kayys.wayang.plugin.registry.ControlPlaneExecutorRegistry;
import tech.kayys.wayang.plugin.registry.ControlPlaneWidgetRegistry;
import tech.kayys.wayang.plugin.registry.node.NodeDefinition;
import tech.kayys.wayang.plugin.registry.executor.ExecutorRegistration;
import tech.kayys.wayang.plugin.registry.executor.ExecutorStatus;
import tech.kayys.wayang.plugin.registry.UIWidgetDefinition;
import tech.kayys.wayang.plugin.CommunicationProtocol;
import tech.kayys.wayang.schema.validator.ValidationResult;
import tech.kayys.wayang.control.service.ProjectManager;
import tech.kayys.wayang.control.service.WayangDefinitionService;
import tech.kayys.wayang.control.service.CompositeNodeManager;
import tech.kayys.wayang.control.dto.ProjectDTO;
import tech.kayys.wayang.control.domain.WayangDefinition;
import tech.kayys.wayang.control.dto.node.NodePort;
import tech.kayys.wayang.control.dto.node.PortDirection;
import tech.kayys.wayang.control.api.NodeCatalogResponse;
import tech.kayys.wayang.control.domain.WayangDefinition;

import java.util.*;

/**
 * Node definition catalog API for UI Designer.
 */
@Path("/api/v1/control-plane/nodes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@Tag(name = "Control Plane - Nodes", description = "Node definition catalog")
public class NodeCatalogResource {

    private static final Logger LOG = Logger.getLogger(NodeCatalogResource.class);

    @Inject
    ControlPlaneNodeRegistry nodeRegistry;

    @Inject
    ControlPlaneExecutorRegistry executorRegistry;

    @Inject
    ControlPlaneWidgetRegistry widgetRegistry;

    @Inject
    ProjectManager projectManager;

    @Inject
    WayangDefinitionService definitionService;

    @Inject
    CompositeNodeManager compositeNodeManager;

    /**
     * Get complete node catalog (for UI Designer)
     */
    @GET
    @Operation(summary = "Get complete node catalog", description = "Returns all registered nodes with UI and executor bindings")
    public Uni<NodeCatalogResponse> getNodeCatalog(
            @QueryParam("category") String category,
            @HeaderParam("X-Tenant-Id") String tenantId) {

        return Uni.combine().all().unis(
                fetchStaticNodes(category),
                fetchProjectNodes(category, tenantId),
                fetchDefinitionNodes(category, tenantId))
                .with(results -> {
                    List<NodeCatalogEntry> staticNodes = (List<NodeCatalogEntry>) results.get(0);
                    List<NodeCatalogEntry> projectNodes = (List<NodeCatalogEntry>) results.get(1);
                    List<NodeCatalogEntry> defNodes = (List<NodeCatalogEntry>) results.get(2);

                    List<NodeCatalogEntry> allEntries = new ArrayList<>();
                    allEntries.addAll(staticNodes);
                    allEntries.addAll(projectNodes);
                    allEntries.addAll(defNodes);

                    Set<String> categories = allEntries.stream()
                            .map(NodeCatalogEntry::category)
                            .filter(Objects::nonNull)
                            .collect(java.util.stream.Collectors.toSet());

                    return new NodeCatalogResponse(
                            allEntries,
                            new ArrayList<>(categories),
                            allEntries.size());
                });
    }

    private Uni<List<NodeCatalogEntry>> fetchStaticNodes(String category) {
        List<NodeDefinition> nodes = (category != null && !category.isEmpty())
                ? nodeRegistry.getByCategory(category)
                : nodeRegistry.getAll();
        return Uni.createFrom().item(nodes.stream().map(this::toNodeCatalogEntry).toList());
    }

    private Uni<List<NodeCatalogEntry>> fetchProjectNodes(String category, String tenantId) {
        if (category != null && !"Sub-Workflows".equals(category)) {
            return Uni.createFrom().item(Collections.emptyList());
        }
        return projectManager.listProjects(tenantId, null)
                .map(projects -> projects.stream()
                        .map(this::projectToNodeEntry)
                        .toList());
    }

    private Uni<List<NodeCatalogEntry>> fetchDefinitionNodes(String category, String tenantId) {
        if (category != null && !"Sub-Workflows".equals(category)) {
            return Uni.createFrom().item(Collections.emptyList());
        }
        // Listing published definitions as sub-workflows
        return definitionService.listByProject(UUID.randomUUID()) // Placeholder for proper listing
                .onFailure().recoverWithItem(Collections.emptyList())
                .map(defs -> defs.stream()
                        .map(this::definitionToNodeEntry)
                        .toList());
    }

    private NodeCatalogEntry projectToNodeEntry(ProjectDTO project) {
        return new NodeCatalogEntry(
                "wayang:project:" + project.projectId(),
                project.projectName(),
                "Sub-Workflows",
                "Project",
                project.description(),
                "1.0.0",
                null, null, null,
                null, null,
                project.metadata() != null ? project.metadata() : Map.of(),
                List.of("project", "composite"),
                Collections.emptyList());
    }

    private NodeCatalogEntry definitionToNodeEntry(WayangDefinition def) {
        return new NodeCatalogEntry(
                "wayang:definition:" + def.definitionId,
                def.name,
                "Sub-Workflows",
                def.definitionType != null ? def.definitionType.name() : "Workflow",
                def.description,
                def.version,
                null, null, null,
                null, null,
                Map.of(),
                def.tags != null ? def.tags : List.of("definition", "composite"),
                Collections.emptyList());
    }

    /**
     * Get specific node definition
     */
    @GET
    @Path("/{nodeType}")
    @Operation(summary = "Get node definition by type")
    public Uni<RestResponse<NodeCatalogEntry>> getNode(
            @PathParam("nodeType") String nodeType,
            @HeaderParam("X-Tenant-Id") String tenantId) {

        if (nodeType.startsWith("wayang:project:")) {
            UUID projectId = UUID.fromString(nodeType.substring("wayang:project:".length()));
            return projectManager.getProject(projectId, tenantId)
                    .flatMap(p -> p == null ? Uni.createFrom().item(() -> RestResponse.<NodeCatalogEntry>notFound())
                            : enrichProjectNode(p, tenantId).map(RestResponse::ok));
        } else if (nodeType.startsWith("wayang:definition:")) {
            UUID definitionId = UUID.fromString(nodeType.substring("wayang:definition:".length()));
            return definitionService.findById(definitionId)
                    .flatMap(d -> d == null ? Uni.createFrom().item(() -> RestResponse.<NodeCatalogEntry>notFound())
                            : enrichDefinitionNode(d).map(RestResponse::ok));
        }

        return Uni.createFrom().item(() -> nodeRegistry.get(nodeType))
                .map(node -> {
                    if (node == null) {
                        return RestResponse.<NodeCatalogEntry>notFound();
                    }
                    return RestResponse.ok(toNodeCatalogEntry(node));
                });
    }

    private Uni<NodeCatalogEntry> enrichProjectNode(ProjectDTO project, String tenantId) {
        // Find the "Interface" definition if it exists
        return definitionService.listByProject(project.projectId())
                .map(defs -> {
                    if (defs.isEmpty()) {
                        return projectToNodeEntry(project);
                    }
                    return definitionToNodeEntry(defs.get(0));
                });
    }

    private Uni<NodeCatalogEntry> enrichDefinitionNode(WayangDefinition definition) {
        return Uni.createFrom().item(() -> {
            NodeCatalogEntry baseEntry = definitionToNodeEntry(definition);
            List<NodePort> ports = Collections.emptyList();
            if (definition.spec != null && definition.spec.getCanvas() != null) {
                // Discover ports from canvas data
                ports = compositeNodeManager.discoverPorts(definition.spec.getCanvas());
            }
            return new NodeCatalogEntry(
                    baseEntry.type(),
                    baseEntry.label(),
                    baseEntry.category(),
                    baseEntry.subCategory(),
                    baseEntry.description(),
                    baseEntry.version(),
                    null, null, null,
                    null, null,
                    baseEntry.metadata(),
                    baseEntry.tags(),
                    ports);
        });
    }

    /**
     * Validate node configuration
     */
    @POST
    @Path("/{nodeType}/validate-config")
    @Operation(summary = "Validate node configuration against schema")
    public Uni<ValidationResultDTO> validateConfig(
            @PathParam("nodeType") String nodeType,
            Map<String, @Valid Object> config) {

        return Uni.createFrom().item(() -> {
            ValidationResult result = nodeRegistry.validateConfig(nodeType, config);
            return new ValidationResultDTO(result.isValid(), result.getMessage());
        });
    }

    /**
     * Validate node inputs
     */
    @POST
    @Path("/{nodeType}/validate-inputs")
    @Operation(summary = "Validate node inputs against schema")
    public Uni<ValidationResultDTO> validateInputs(
            @PathParam("nodeType") String nodeType,
            Map<String, @Valid Object> inputs) {

        return Uni.createFrom().item(() -> {
            ValidationResult result = nodeRegistry.validateInputs(nodeType, inputs);
            return new ValidationResultDTO(result.isValid(), result.getMessage());
        });
    }

    private NodeCatalogEntry toNodeCatalogEntry(NodeDefinition node) {
        // Get executor information
        ExecutorInfo executorInfo = null;
        if (node.executorBinding != null) {
            ExecutorRegistration executor = executorRegistry.get(
                    node.executorBinding.executorId);
            if (executor != null) {
                executorInfo = new ExecutorInfo(
                        executor.executorId,
                        executor.executorType,
                        executor.protocol,
                        executor.status,
                        executor.endpoint != null ? executor.endpoint.toString() : null);
            }
        }

        // Get widget information
        WidgetInfo widgetInfo = null;
        if (node.uiReference != null) {
            UIWidgetDefinition widget = widgetRegistry.get(node.uiReference.widgetId);
            if (widget != null) {
                widgetInfo = new WidgetInfo(
                        widget.widgetId,
                        widget.type,
                        widget.entryPoint);
            }
        }

        return new NodeCatalogEntry(
                node.type,
                node.label,
                node.category,
                node.subCategory,
                node.description,
                node.version,
                extractSchemaJson(node.configSchema),
                extractSchemaJson(node.inputSchema),
                extractSchemaJson(node.outputSchema),
                executorInfo,
                null,
                node.metadata,
                node.tags,
                Collections.emptyList() // Static nodes usually have ports defined in schemas or UI widgets
        );
    }

    private String extractSchemaJson(com.networknt.schema.JsonSchema schema) {
        if (schema == null)
            return null;
        return schema.getSchemaNode().toString();
    }
}

/**
 * Node catalog response
 */
record NodeCatalogResponse(
        List<NodeCatalogEntry> nodes,
        List<String> categories,
        int totalCount) {
}

/**
 * Node catalog entry with enriched information
 */
record NodeCatalogEntry(
        String type,
        String label,
        String category,
        String subCategory,
        String description,
        String version,
        String configSchema,
        String inputSchema,
        String outputSchema,
        ExecutorInfo executor,
        WidgetInfo widget,
        Map<String, Object> metadata,
        List<String> tags,
        List<NodePort> ports) {
}

/**
 * Executor info in catalog
 */
record ExecutorInfo(
        String executorId,
        String executorType,
        CommunicationProtocol protocol,
        ExecutorStatus status,
        String endpoint) {
}

/**
 * Widget info in catalog
 */
record WidgetInfo(
        String widgetId,
        String type,
        String entryPoint) {
}

/**
 * Validation result DTO
 */
record ValidationResultDTO(
        boolean valid,
        String message) {
}
