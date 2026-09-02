package tech.kayys.wayang.knowledge;

import java.util.List;
import java.util.Map;

/**
 * Result of budget-bounded evidence selection.
 */
public record KnowledgeSelectionResult(
        List<KnowledgeEvidence> selected,
        List<KnowledgeEvidence> excluded,
        List<KnowledgeConflict> conflicts,
        long estimatedTokens,
        Map<String, Object> diagnostics
) {

    public KnowledgeSelectionResult {
        selected = selected == null ? List.of() : List.copyOf(selected);
        excluded = excluded == null ? List.of() : List.copyOf(excluded);
        conflicts = conflicts == null ? List.of() : List.copyOf(conflicts);
        diagnostics = diagnostics == null ? Map.of() : Map.copyOf(diagnostics);
    }

    public static KnowledgeSelectionResult empty() {
        return new KnowledgeSelectionResult(List.of(), List.of(), List.of(), 0, Map.of());
    }
}
