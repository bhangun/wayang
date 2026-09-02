package tech.kayys.wayang.knowledge.graph;

/**
 * SPI: projects internal knowledge domain graph models into the normalized
 * {@link KnowledgeGraphView} envelope for UI visualization.
 *
 * <p>Implementations live in {@code wayang-knowledge-runtime} and are wired
 * to the live in-memory or persistent graph stores.</p>
 */
public interface KnowledgeGraphProjectionService {

    /**
     * Projects the answer-artifact graph for a given artifact ID.
     */
    KnowledgeGraphView artifactGraph(KnowledgeGraphQuery query);

    /**
     * Projects the provenance graph for a given response ID.
     */
    KnowledgeGraphView provenanceGraph(KnowledgeGraphQuery query);

    /**
     * Projects the claim-contradiction graph for a given query ID.
     */
    KnowledgeGraphView claimGraph(KnowledgeGraphQuery query);

    /**
     * Projects the evidence fusion graph for a given session ID.
     */
    KnowledgeGraphView fusionGraph(KnowledgeGraphQuery query);

    /**
     * Projects the resolution dependency chain for a given resolution ID.
     */
    KnowledgeGraphView resolutionDependencyGraph(KnowledgeGraphQuery query);

    /**
     * Projects lineage ancestors and descendants for a given knowledge node ID.
     * Respects {@link KnowledgeGraphQuery#maxDepth()}.
     */
    KnowledgeGraphView lineageGraph(KnowledgeGraphQuery query);

    /**
     * Projects the snapshot dependency graph for a given snapshot ID.
     */
    KnowledgeGraphView snapshotDependencyGraph(KnowledgeGraphQuery query);

    /**
     * Composes a full knowledge graph view scoped by tenant, workspace,
     * and optionally project/session — merging all graph types.
     */
    KnowledgeGraphView fullGraph(KnowledgeGraphQuery query);
}
