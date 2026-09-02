package tech.kayys.wayang.knowledge.snapshot;

import java.util.Map;

public record KnowledgeRuntimeSnapshot(
        String runtimeVersion,
        String knowledgeEngineVersion,
        String rankingVersion,
        String compressionVersion,
        String budgetPolicyVersion,
        String modelProviderId,
        String modelId,
        String modelVersion,
        Map<String, Object> configuration
) {

    public KnowledgeRuntimeSnapshot {
        configuration = configuration == null ? Map.of() : Map.copyOf(configuration);
    }
}
