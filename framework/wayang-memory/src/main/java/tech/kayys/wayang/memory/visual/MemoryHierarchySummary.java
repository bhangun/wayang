package tech.kayys.wayang.memory.visual;

import java.util.Map;

/**
 * Summary metrics across the 4 hierarchical memory tiers and long-term storage.
 */
public record MemoryHierarchySummary(
        int workingMemoryActiveItems,
        int episodicMemoryRecentCount,
        double episodicCoherenceScore,
        int semanticConceptCount,
        int semanticFactCount,
        int proceduralPatternCount,
        double proceduralAvgProficiency,
        long longTermTotalRecords,
        long longTermSizeBytes,
        Map<String, Long> categoryDistribution
) {
    public MemoryHierarchySummary {
        categoryDistribution = categoryDistribution == null ? Map.of() : Map.copyOf(categoryDistribution);
    }

    public static MemoryHierarchySummary empty() {
        return new MemoryHierarchySummary(0, 0, 1.0, 0, 0, 0, 0.0, 0L, 0L, Map.of());
    }
}
