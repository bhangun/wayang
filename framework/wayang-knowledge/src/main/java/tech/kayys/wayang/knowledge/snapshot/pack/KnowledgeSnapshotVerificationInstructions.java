package tech.kayys.wayang.knowledge.snapshot.pack;

import java.util.List;
import java.util.Map;

public record KnowledgeSnapshotVerificationInstructions(
        String protocolVersion,
        List<String> requiredAlgorithms,
        List<String> requiredResources,
        boolean requiresOriginalRuntime,
        boolean requiresKnowledgeStore,
        boolean requiresNetwork,
        String verificationProcedure,
        Map<String, String> metadata
) {
    public KnowledgeSnapshotVerificationInstructions {
        requiredAlgorithms = requiredAlgorithms == null ? List.of() : List.copyOf(requiredAlgorithms);
        requiredResources = requiredResources == null ? List.of() : List.copyOf(requiredResources);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
