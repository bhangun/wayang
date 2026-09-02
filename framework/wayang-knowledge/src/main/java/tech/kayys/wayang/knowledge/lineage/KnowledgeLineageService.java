package tech.kayys.wayang.knowledge.lineage;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Service managing lineage inspection and explainability queries.
 */
public interface KnowledgeLineageService {

    CompletionStage<List<KnowledgeLineageEdge>> traceProvenance(String knowledgeItemId);

    CompletionStage<EvidenceBundle> createBundle(String query, List<tech.kayys.wayang.knowledge.KnowledgeEvidence> evidence);
}
