package tech.kayys.wayang.knowledge.graph;

import tech.kayys.wayang.knowledge.exchange.compact.InMemoryKnowledgeAnswerArtifactGraphStore;
import tech.kayys.wayang.knowledge.exchange.compact.KnowledgeAnswerArtifactRelation;
import tech.kayys.wayang.knowledge.exchange.selection.KnowledgeAnswerResolutionDependency;
import tech.kayys.wayang.knowledge.exchange.selection.InMemoryKnowledgeAnswerResolutionDependencyGraph;
import tech.kayys.wayang.knowledge.lineage.InMemoryKnowledgeLineageStore;
import tech.kayys.wayang.knowledge.lineage.KnowledgeLineageEdge;
import tech.kayys.wayang.knowledge.snapshot.dependency.InMemoryKnowledgeSnapshotDependencyGraph;
import tech.kayys.wayang.knowledge.snapshot.dependency.KnowledgeSnapshotDependency;
import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Default runtime implementation of {@link KnowledgeGraphProjectionService}.
 *
 * <p>Wired to in-memory runtime stores from {@code wayang-knowledge-runtime}.
 * Can be replaced with a persistent-backed implementation for production.</p>
 */
public class DefaultKnowledgeGraphProjectionService implements KnowledgeGraphProjectionService {

    private final InMemoryKnowledgeAnswerArtifactGraphStore artifactGraphStore;
    private final InMemoryKnowledgeAnswerResolutionDependencyGraph resolutionDependencyGraph;
    private final InMemoryKnowledgeLineageStore lineageStore;
    private final InMemoryKnowledgeSnapshotDependencyGraph snapshotDependencyGraph;

    public DefaultKnowledgeGraphProjectionService(
            InMemoryKnowledgeAnswerArtifactGraphStore artifactGraphStore,
            InMemoryKnowledgeAnswerResolutionDependencyGraph resolutionDependencyGraph,
            InMemoryKnowledgeLineageStore lineageStore,
            InMemoryKnowledgeSnapshotDependencyGraph snapshotDependencyGraph) {
        this.artifactGraphStore = artifactGraphStore;
        this.resolutionDependencyGraph = resolutionDependencyGraph;
        this.lineageStore = lineageStore;
        this.snapshotDependencyGraph = snapshotDependencyGraph;
    }

    @Override
    public KnowledgeGraphView artifactGraph(KnowledgeGraphQuery query) {
        String artifactId = query.entityId();
        List<KnowledgeGraphNode> nodes = new ArrayList<>();
        List<KnowledgeGraphEdge> edges = new ArrayList<>();

        nodes.add(KnowledgeGraphNode.of(artifactId, "ARTIFACT", artifactId, 1.0,
                query.tenantId(), query.workspaceId(), query.projectId()));

        List<KnowledgeAnswerArtifactRelation> fromRelations = artifactGraphStore.relationsFrom(artifactId);
        List<KnowledgeAnswerArtifactRelation> toRelations = artifactGraphStore.relationsTo(artifactId);

        for (KnowledgeAnswerArtifactRelation rel : fromRelations) {
            nodes.add(KnowledgeGraphNode.of(rel.targetArtifactId(), "ARTIFACT",
                    rel.targetArtifactId(), rel.confidence()));
            edges.add(KnowledgeGraphEdge.of(rel.sourceArtifactId(), rel.targetArtifactId(),
                    rel.type().name(), rel.confidence()));
        }
        for (KnowledgeAnswerArtifactRelation rel : toRelations) {
            if (!rel.sourceArtifactId().equals(artifactId)) {
                nodes.add(KnowledgeGraphNode.of(rel.sourceArtifactId(), "ARTIFACT",
                        rel.sourceArtifactId(), rel.confidence()));
                edges.add(KnowledgeGraphEdge.of(rel.sourceArtifactId(), rel.targetArtifactId(),
                        rel.type().name(), rel.confidence()));
            }
        }

        return KnowledgeGraphView.builder(KnowledgeGraphView.KnowledgeGraphType.ARTIFACT)
                .nodes(deduplicateNodes(nodes)).edges(edges)
                .tenantId(query.tenantId()).workspaceId(query.workspaceId())
                .projectId(query.projectId()).sessionId(query.sessionId())
                .build();
    }

    @Override
    public KnowledgeGraphView provenanceGraph(KnowledgeGraphQuery query) {
        // Provenance graphs are assembled at response-generation time and stored
        // by a ProvenanceGraphStore (not yet wired). Return root node as anchor.
        return KnowledgeGraphView.builder(KnowledgeGraphView.KnowledgeGraphType.PROVENANCE)
                .graphId("provenance-" + query.entityId())
                .nodes(List.of(KnowledgeGraphNode.of(
                        query.entityId(), "RESPONSE", "Response: " + query.entityId(), 1.0,
                        query.tenantId(), query.workspaceId(), query.projectId())))
                .edges(List.of())
                .tenantId(query.tenantId()).workspaceId(query.workspaceId())
                .projectId(query.projectId()).sessionId(query.sessionId())
                .build();
    }

    @Override
    public KnowledgeGraphView claimGraph(KnowledgeGraphQuery query) {
        return KnowledgeGraphView.builder(KnowledgeGraphView.KnowledgeGraphType.CLAIM)
                .graphId("claim-" + query.entityId())
                .nodes(List.of()).edges(List.of())
                .tenantId(query.tenantId()).workspaceId(query.workspaceId())
                .projectId(query.projectId()).sessionId(query.sessionId())
                .build();
    }

    @Override
    public KnowledgeGraphView fusionGraph(KnowledgeGraphQuery query) {
        return KnowledgeGraphView.builder(KnowledgeGraphView.KnowledgeGraphType.FUSION)
                .graphId("fusion-" + query.entityId())
                .nodes(List.of()).edges(List.of())
                .tenantId(query.tenantId()).workspaceId(query.workspaceId())
                .projectId(query.projectId()).sessionId(query.sessionId())
                .build();
    }

    @Override
    public KnowledgeGraphView resolutionDependencyGraph(KnowledgeGraphQuery query) {
        String resolutionId = query.entityId();
        List<KnowledgeGraphNode> nodes = new ArrayList<>();
        List<KnowledgeGraphEdge> edges = new ArrayList<>();

        nodes.add(KnowledgeGraphNode.of(resolutionId, "RESOLUTION", "Resolution: " + resolutionId, 1.0));

        List<KnowledgeAnswerResolutionDependency> deps =
                resolutionDependencyGraph.dependenciesOf(resolutionId);
        for (KnowledgeAnswerResolutionDependency dep : deps) {
            double weight = dep.required() ? 1.0 : 0.5;
            nodes.add(KnowledgeGraphNode.of(dep.targetId(), dep.type().name(), dep.targetId(), weight));
            edges.add(KnowledgeGraphEdge.of(resolutionId, dep.targetId(), dep.type().name(), weight));
        }

        return KnowledgeGraphView.builder(KnowledgeGraphView.KnowledgeGraphType.RESOLUTION_DEPENDENCY)
                .nodes(deduplicateNodes(nodes)).edges(edges)
                .tenantId(query.tenantId()).workspaceId(query.workspaceId())
                .projectId(query.projectId()).sessionId(query.sessionId())
                .build();
    }

    @Override
    public KnowledgeGraphView lineageGraph(KnowledgeGraphQuery query) {
        String nodeId = query.entityId();
        List<KnowledgeGraphNode> nodes = new ArrayList<>();
        List<KnowledgeGraphEdge> edges = new ArrayList<>();

        nodes.add(KnowledgeGraphNode.of(nodeId, "KNOWLEDGE_ITEM", nodeId, 1.0));

        try {
            List<KnowledgeLineageEdge> lineageEdges =
                    lineageStore.getAncestors(nodeId, query.maxDepth()).toCompletableFuture().get();
            for (KnowledgeLineageEdge edge : lineageEdges) {
                nodes.add(KnowledgeGraphNode.of(
                        edge.sourceId(), "KNOWLEDGE_ITEM", edge.sourceId(), edge.confidence()));
                nodes.add(KnowledgeGraphNode.of(
                        edge.targetId(), "KNOWLEDGE_ITEM", edge.targetId(), edge.confidence()));
                edges.add(KnowledgeGraphEdge.of(
                        edge.sourceId(), edge.targetId(), edge.relation().name(), edge.confidence()));
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
        }

        return KnowledgeGraphView.builder(KnowledgeGraphView.KnowledgeGraphType.LINEAGE)
                .nodes(deduplicateNodes(nodes)).edges(edges)
                .tenantId(query.tenantId()).workspaceId(query.workspaceId())
                .projectId(query.projectId()).sessionId(query.sessionId())
                .build();
    }

    @Override
    public KnowledgeGraphView snapshotDependencyGraph(KnowledgeGraphQuery query) {
        String snapshotId = query.entityId();
        List<KnowledgeGraphNode> nodes = new ArrayList<>();
        List<KnowledgeGraphEdge> edges = new ArrayList<>();

        nodes.add(KnowledgeGraphNode.of(snapshotId, "SNAPSHOT", "Snapshot: " + snapshotId, 1.0));

        KnowledgeSnapshotId snapshotKey = KnowledgeSnapshotId.of(snapshotId);
        List<KnowledgeSnapshotDependency> deps =
                snapshotDependencyGraph.transitiveDependencies(snapshotKey);

        for (KnowledgeSnapshotDependency dep : deps) {
            String depId = dep.targetId();
            nodes.add(KnowledgeGraphNode.of(depId, "SNAPSHOT", "Snapshot: " + depId, 1.0));
            edges.add(KnowledgeGraphEdge.of(snapshotId, depId, dep.type().name(), 1.0));
        }

        return KnowledgeGraphView.builder(KnowledgeGraphView.KnowledgeGraphType.SNAPSHOT_DEPENDENCY)
                .nodes(deduplicateNodes(nodes)).edges(edges)
                .tenantId(query.tenantId()).workspaceId(query.workspaceId())
                .projectId(query.projectId()).sessionId(query.sessionId())
                .build();
    }

    @Override
    public KnowledgeGraphView fullGraph(KnowledgeGraphQuery query) {
        List<KnowledgeGraphNode> allNodes = new ArrayList<>();
        List<KnowledgeGraphEdge> allEdges = new ArrayList<>();

        // Lineage anchored at workspace as root
        KnowledgeGraphQuery lineageQuery = new KnowledgeGraphQuery(
                query.workspaceId(), query.tenantId(), query.workspaceId(),
                query.projectId(), query.sessionId(), query.maxDepth());
        KnowledgeGraphView lineage = lineageGraph(lineageQuery);
        allNodes.addAll(lineage.nodes());
        allEdges.addAll(lineage.edges());

        return KnowledgeGraphView.builder(KnowledgeGraphView.KnowledgeGraphType.FULL)
                .graphId("full-" + query.workspaceId())
                .nodes(deduplicateNodes(allNodes)).edges(allEdges)
                .tenantId(query.tenantId()).workspaceId(query.workspaceId())
                .projectId(query.projectId()).sessionId(query.sessionId())
                .build();
    }

    private List<KnowledgeGraphNode> deduplicateNodes(List<KnowledgeGraphNode> nodes) {
        LinkedHashMap<String, KnowledgeGraphNode> seen = new LinkedHashMap<>();
        for (KnowledgeGraphNode n : nodes) seen.putIfAbsent(n.id(), n);
        return List.copyOf(seen.values());
    }
}
