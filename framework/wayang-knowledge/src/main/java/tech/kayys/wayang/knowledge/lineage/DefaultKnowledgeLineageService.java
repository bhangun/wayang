package tech.kayys.wayang.knowledge.lineage;

import tech.kayys.wayang.knowledge.KnowledgeEvidence;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class DefaultKnowledgeLineageService implements KnowledgeLineageService {

    private final KnowledgeLineageStore store;

    public DefaultKnowledgeLineageService(KnowledgeLineageStore store) {
        this.store = store != null ? store : new InMemoryKnowledgeLineageStore();
    }

    @Override
    public CompletionStage<List<KnowledgeLineageEdge>> traceProvenance(String knowledgeItemId) {
        return store.getAncestors(knowledgeItemId, 5);
    }

    @Override
    public CompletionStage<EvidenceBundle> createBundle(String query, List<KnowledgeEvidence> evidence) {
        EvidenceBundle bundle = new EvidenceBundle(null, query, evidence, List.of(), java.time.Instant.now(), java.util.Map.of());
        return store.saveBundle(bundle);
    }
}
