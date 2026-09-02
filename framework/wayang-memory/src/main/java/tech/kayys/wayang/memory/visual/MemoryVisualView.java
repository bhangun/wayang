package tech.kayys.wayang.memory.visual;

import java.time.Instant;
import java.util.List;

/**
 * Normalized UI envelope for Memory Visualization.
 * All memory visualization endpoints return this shape or a focused subset of it.
 */
public record MemoryVisualView(
        String viewId,
        MemoryVisualViewType viewType,
        String agentId,
        String userId,
        String tenantId,
        String workspaceId,
        String sessionId,
        MemoryHierarchySummary hierarchy,
        List<MemoryVisualNode> nodes,
        List<MemoryVisualEdge> edges,
        List<MemoryVectorPoint> vectorPoints,
        List<MemoryVisualInsight> insights,
        MemoryWorkingState workingState,
        Instant generatedAt
) {
    public enum MemoryVisualViewType {
        OVERVIEW,
        GRAPH,
        VECTORS,
        WORKING,
        PROCEDURAL,
        TIMELINE,
        FULL
    }

    public MemoryVisualView {
        nodes = nodes == null ? List.of() : List.copyOf(nodes);
        edges = edges == null ? List.of() : List.copyOf(edges);
        vectorPoints = vectorPoints == null ? List.of() : List.copyOf(vectorPoints);
        insights = insights == null ? List.of() : List.copyOf(insights);
        hierarchy = hierarchy == null ? MemoryHierarchySummary.empty() : hierarchy;
        workingState = workingState == null ? MemoryWorkingState.empty() : workingState;
        generatedAt = generatedAt == null ? Instant.now() : generatedAt;
    }

    public static Builder builder(MemoryVisualViewType viewType) {
        return new Builder(viewType);
    }

    public static final class Builder {
        private final MemoryVisualViewType viewType;
        private String viewId = "mview-" + java.util.UUID.randomUUID();
        private String agentId = "default";
        private String userId;
        private String tenantId = "default";
        private String workspaceId = "default";
        private String sessionId;
        private MemoryHierarchySummary hierarchy;
        private List<MemoryVisualNode> nodes = List.of();
        private List<MemoryVisualEdge> edges = List.of();
        private List<MemoryVectorPoint> vectorPoints = List.of();
        private List<MemoryVisualInsight> insights = List.of();
        private MemoryWorkingState workingState;

        private Builder(MemoryVisualViewType viewType) {
            this.viewType = viewType;
        }

        public Builder viewId(String viewId) { this.viewId = viewId; return this; }
        public Builder agentId(String agentId) { this.agentId = agentId; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public Builder workspaceId(String workspaceId) { this.workspaceId = workspaceId; return this; }
        public Builder sessionId(String sessionId) { this.sessionId = sessionId; return this; }
        public Builder hierarchy(MemoryHierarchySummary hierarchy) { this.hierarchy = hierarchy; return this; }
        public Builder nodes(List<MemoryVisualNode> nodes) { this.nodes = nodes; return this; }
        public Builder edges(List<MemoryVisualEdge> edges) { this.edges = edges; return this; }
        public Builder vectorPoints(List<MemoryVectorPoint> points) { this.vectorPoints = points; return this; }
        public Builder insights(List<MemoryVisualInsight> insights) { this.insights = insights; return this; }
        public Builder workingState(MemoryWorkingState workingState) { this.workingState = workingState; return this; }

        public MemoryVisualView build() {
            return new MemoryVisualView(
                    viewId, viewType, agentId, userId, tenantId, workspaceId, sessionId,
                    hierarchy, nodes, edges, vectorPoints, insights, workingState, Instant.now()
            );
        }
    }
}
