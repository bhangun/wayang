package tech.kayys.wayang.knowledge;

import java.util.List;

/**
 * Evaluates candidate evidence against a budget, filtering and ranking units.
 */
public interface KnowledgeSelector {

    KnowledgeSelectionResult select(
            List<KnowledgeEvidence> candidates,
            KnowledgeBudget budget,
            KnowledgeContext context
    );
}
