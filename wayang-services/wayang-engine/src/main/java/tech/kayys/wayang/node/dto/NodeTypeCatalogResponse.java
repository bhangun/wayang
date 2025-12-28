package tech.kayys.wayang.node.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Node Type Catalog Response.
 * 
 * Organizes node types by category with metadata.
 * 
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodeTypeCatalogResponse {

    private String version;
    private String lastUpdated;
    private Map<String, CategoryInfo> categories;
    private int totalNodes;

    public NodeTypeCatalogResponse() {
        this.categories = new HashMap<>();
    }

    /**
     * Add a category with its nodes.
     * 
     * @param name        Category name
     * @param description Category description
     * @param nodes       List of node types in category
     */
    public void addCategory(String name, String description, List<NodeTypeDescriptor> nodes) {
        CategoryInfo info = new CategoryInfo();
        info.setName(name);
        info.setDescription(description);
        info.setNodes(nodes);
        info.setCount(nodes.size());
        this.categories.put(name, info);
    }

    /**
     * Find node type by ID.
     */
    public Optional<NodeTypeDescriptor> findNodeType(String nodeTypeId) {
        return categories.values().stream()
                .flatMap(cat -> cat.getNodes().stream())
                .filter(node -> node.getId().equals(nodeTypeId))
                .findFirst();
    }

    /**
     * Search node types by query and category.
     */
    public List<NodeTypeDescriptor> search(String query, String category) {
        return categories.values().stream()
                .filter(cat -> category == null || cat.getName().equalsIgnoreCase(category))
                .flatMap(cat -> cat.getNodes().stream())
                .filter(node -> query == null ||
                        node.getName().toLowerCase().contains(query.toLowerCase()) ||
                        node.getDescription().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Get nodes by category.
     */
    public List<NodeTypeDescriptor> getNodesByCategory(String category) {
        if (categories.containsKey(category)) {
            return categories.get(category).getNodes();
        }
        return new ArrayList<>();
    }

    /**
     * Get node suggestions based on context.
     */
    public List<NodeTypeDescriptor> getSuggestions(NodeSuggestionRequest context) {
        // Basic suggestion logic: find nodes that match output type
        if (context.getOutputType() == null)
            return new ArrayList<>();

        return categories.values().stream()
                .flatMap(cat -> cat.getNodes().stream())
                .filter(node -> node.getInputs().stream()
                        .anyMatch(input -> input.getType().equalsIgnoreCase(context.getOutputType())))
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Calculate total number of nodes across all categories.
     * 
     * @return Total node count
     */
    public int calculateTotalNodes() {
        return categories.values().stream()
                .mapToInt(CategoryInfo::getCount)
                .sum();
    }

    /**
     * Category information.
     */
    public static class CategoryInfo {
        private String name;
        private String description;
        private List<NodeTypeDescriptor> nodes;
        private int count;

        public CategoryInfo() {
            this.nodes = new ArrayList<>();
        }

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<NodeTypeDescriptor> getNodes() {
            return nodes;
        }

        public void setNodes(List<NodeTypeDescriptor> nodes) {
            this.nodes = nodes;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    // Getters and setters
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Map<String, CategoryInfo> getCategories() {
        return categories;
    }

    public void setCategories(Map<String, CategoryInfo> categories) {
        this.categories = categories;
    }

    public int getTotalNodes() {
        return totalNodes;
    }

    public void setTotalNodes(int totalNodes) {
        this.totalNodes = totalNodes;
    }
}
