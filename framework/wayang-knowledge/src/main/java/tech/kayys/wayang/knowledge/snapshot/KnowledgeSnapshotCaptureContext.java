package tech.kayys.wayang.knowledge.snapshot;

import java.util.List;

public record KnowledgeSnapshotCaptureContext(
        List<KnowledgeSnapshotEntry> knowledge,
        KnowledgePolicySnapshot policies,
        KnowledgeRuleSnapshot rules,
        KnowledgeGovernanceSnapshot governance,
        KnowledgeRuntimeSnapshot runtime
) {

    public KnowledgeSnapshotCaptureContext {
        knowledge = knowledge == null ? List.of() : List.copyOf(knowledge);
    }
}
