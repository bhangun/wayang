package tech.kayys.wayang.execution.memory;

/**
 * The four memory layers in a durable agent runtime.
 */
public enum MemoryLayer {
    /**
     * Current execution state — tool inputs, intermediate results, running
     * totals. Cleared when the run ends.
     */
    WORKING,

    /**
     * Previous conversations and run transcripts. Retained across sessions,
     * indexed by session/user ID.
     */
    EPISODIC,

    /**
     * Long-term facts, user preferences, and domain knowledge.
     * Semantically queryable via embedding search.
     */
    SEMANTIC,

    /**
     * Learned workflows, successful strategies, and agent skills.
     * Updated through reflection and feedback loops.
     */
    PROCEDURAL
}
