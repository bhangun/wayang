package tech.kayys.wayang.memory.visual;

/**
 * SPI: Projects internal memory states (Working, Episodic, Semantic, Procedural, Vector)
 * into normalized {@link MemoryVisualView} envelopes for UI rendering.
 */
public interface MemoryVisualProjectionService {

    /**
     * Projects the 4-tier memory hierarchy summary + key insights.
     */
    MemoryVisualView overview(MemoryVisualQuery query);

    /**
     * Projects the semantic knowledge concepts & facts network as a graph.
     */
    MemoryVisualView graph(MemoryVisualQuery query);

    /**
     * Projects vector memory embeddings as 2D/3D scatter points.
     */
    MemoryVisualView vectors(MemoryVisualQuery query);

    /**
     * Projects current working memory attention weights and active facts.
     */
    MemoryVisualView working(MemoryVisualQuery query);

    /**
     * Projects procedural memory task execution patterns and skill proficiencies.
     */
    MemoryVisualView procedural(MemoryVisualQuery query);

    /**
     * Projects episodic memory chronological events & decay curves.
     */
    MemoryVisualView timeline(MemoryVisualQuery query);

    /**
     * Composes all visual layers into a single comprehensive payload.
     */
    MemoryVisualView full(MemoryVisualQuery query);
}
