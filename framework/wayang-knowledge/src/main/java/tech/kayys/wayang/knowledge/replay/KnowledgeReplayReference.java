package tech.kayys.wayang.knowledge.replay;

public record KnowledgeReplayReference(
        String snapshotId,
        String traceId,
        String fingerprint
) {}
