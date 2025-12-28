package tech.kayys.wayang.node.service;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.node.dto.CustomNodeTypeDescriptor;
import tech.kayys.wayang.node.dto.NodeConfigRequest;
import tech.kayys.wayang.node.dto.NodeSuggestionRequest;
import tech.kayys.wayang.node.dto.NodeTypeCatalogResponse;
import tech.kayys.wayang.node.dto.NodeTypeDescriptor;
import tech.kayys.wayang.node.model.ValidationResult;
import tech.kayys.wayang.node.dto.NodeTypeStats;

import java.util.List;

/**
 * Service interface for node type management and discovery.
 * 
 * Provides operations for:
 * - Node type catalog retrieval
 * - Node type search and filtering
 * - Custom node type registration
 * - Node configuration validation
 * - Intelligent node suggestions
 * 
 * @since 1.0.0
 */
public interface NodeTypeService {

        /**
         * Get complete catalog of all available node types.
         * 
         * @param tenantId Tenant identifier
         * @return Complete node type catalog
         */
        Uni<NodeTypeCatalogResponse> getNodeTypeCatalog(String tenantId);

        /**
         * Get detailed descriptor for specific node type.
         * 
         * @param nodeTypeId Node type identifier
         * @param tenantId   Tenant identifier
         * @return Node type descriptor
         */
        Uni<NodeTypeDescriptor> getNodeTypeDescriptor(String nodeTypeId, String tenantId);

        /**
         * Search node types by query and category.
         * 
         * @param query    Search query string
         * @param category Filter by category (optional)
         * @param tenantId Tenant identifier
         * @return List of matching node types
         */
        Uni<List<NodeTypeDescriptor>> searchNodeTypes(String query, String category, String tenantId);

        /**
         * Get node types by category.
         * 
         * @param category Category name
         * @param tenantId Tenant identifier
         * @return List of node types in category
         */
        Uni<List<NodeTypeDescriptor>> getNodeTypesByCategory(String category, String tenantId);

        /**
         * Validate node configuration.
         * 
         * @param nodeTypeId Node type identifier
         * @param config     Node configuration to validate
         * @param tenantId   Tenant identifier
         * @return Validation result
         */
        Uni<ValidationResult> validateNodeConfig(String nodeTypeId, NodeConfigRequest config, String tenantId);

        /**
         * Get node type suggestions based on context.
         * 
         * @param context  Suggestion context
         * @param tenantId Tenant identifier
         * @return List of suggested node types
         */
        Uni<List<NodeTypeDescriptor>> getNodeSuggestions(NodeSuggestionRequest context, String tenantId);

        /**
         * Register custom/plugin node type.
         * 
         * @param descriptor Custom node type descriptor
         * @param tenantId   Tenant identifier
         * @param userId     User identifier
         * @return Registered node type
         */
        Uni<CustomNodeTypeDescriptor> registerCustomNodeType(
                        CustomNodeTypeDescriptor descriptor, String tenantId, String userId);

        /**
         * Update custom node type.
         * 
         * @param nodeTypeId Node type identifier
         * @param descriptor Updated descriptor
         * @param tenantId   Tenant identifier
         * @param userId     User identifier
         * @return Updated node type
         */
        Uni<CustomNodeTypeDescriptor> updateCustomNodeType(
                        String nodeTypeId, CustomNodeTypeDescriptor descriptor, String tenantId, String userId);

        /**
         * Delete custom node type.
         * 
         * @param nodeTypeId Node type identifier
         * @param tenantId   Tenant identifier
         * @param userId     User identifier
         * @return Void on success
         */
        Uni<Void> deleteCustomNodeType(String nodeTypeId, String tenantId, String userId);

        /**
         * Get node type usage statistics.
         * 
         * @param nodeTypeId Node type identifier
         * @param tenantId   Tenant identifier
         * @param fromDate   Start date (optional)
         * @param toDate     End date (optional)
         * @return Usage statistics
         */
        Uni<NodeTypeStats> getNodeTypeStats(String nodeTypeId, String tenantId, String fromDate, String toDate);

        /**
         * Get node type documentation.
         * 
         * @param nodeTypeId Node type identifier
         * @param tenantId   Tenant identifier
         * @return Documentation content
         */
        Uni<String> getNodeTypeDocumentation(String nodeTypeId, String tenantId);
}
