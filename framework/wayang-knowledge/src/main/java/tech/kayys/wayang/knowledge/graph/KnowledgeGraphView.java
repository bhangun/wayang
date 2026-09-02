package tech.kayys.wayang.knowledge.graph;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Normalized UI-facing envelope for knowledge graph visualization.
 * All graph endpoints return this shape regardless of the underlying domain model,
 * allowing the UI to use a single component for all graph views.
 */
public record KnowledgeGraphView(
        String graphId,
        KnowledgeGraphType graphType,
        List<KnowledgeGraphNode> nodes,
        List<KnowledgeGraphEdge> edges,
        KnowledgeGraphStats stats,
        String tenantId,
        String workspaceId,
        String projectId,
        String sessionId,
        Instant generatedAt
) {
    public enum KnowledgeGraphType {
        ARTIFACT,
        PROVENANCE,
        CLAIM,
        FUSION,
        RESOLUTION_DEPENDENCY,
        LINEAGE,
        SNAPSHOT_DEPENDENCY,
        FULL
    }

    public record KnowledgeGraphStats(int nodeCount, int edgeCount) {}

    public KnowledgeGraphView {
        nodes = nodes == null ? List.of() : List.copyOf(nodes);
        edges = edges == null ? List.of() : List.copyOf(edges);
        stats = stats != null ? stats : new KnowledgeGraphStats(nodes.size(), edges.size());
        generatedAt = generatedAt == null ? Instant.now() : generatedAt;
    }

    public static Builder builder(KnowledgeGraphType graphType) {
        return new Builder(graphType);
    }

    public static final class Builder {
        private final KnowledgeGraphType graphType;
        private String graphId = "graph-" + java.util.UUID.randomUUID();
        private List<KnowledgeGraphNode> nodes = List.of();
        private List<KnowledgeGraphEdge> edges = List.of();
        private String tenantId;
        private String workspaceId;
        private String projectId;
        private String sessionId;

        private Builder(KnowledgeGraphType graphType) {
            this.graphType = graphType;
        }

        public Builder graphId(String graphId) { this.graphId = graphId; return this; }
        public Builder nodes(List<KnowledgeGraphNode> nodes) { this.nodes = nodes; return this; }
        public Builder edges(List<KnowledgeGraphEdge> edges) { this.edges = edges; return this; }
        public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public Builder workspaceId(String workspaceId) { this.workspaceId = workspaceId; return this; }
        public Builder projectId(String projectId) { this.projectId = projectId; return this; }
        public Builder sessionId(String sessionId) { this.sessionId = sessionId; return this; }

        public KnowledgeGraphView build() {
            return new KnowledgeGraphView(
                    graphId, graphType, nodes, edges,
                    new KnowledgeGraphStats(nodes.size(), edges.size()),
                    tenantId, workspaceId, projectId, sessionId, Instant.now()
            );
        }
    }
}
