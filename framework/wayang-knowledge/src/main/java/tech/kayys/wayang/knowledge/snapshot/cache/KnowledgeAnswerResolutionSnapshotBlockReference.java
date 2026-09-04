package tech.kayys.wayang.knowledge.snapshot.cache;

public record KnowledgeAnswerResolutionSnapshotBlockReference(
        String snapshotId,
        String tenantId,
        String workspaceId,
        String projectId,
        KnowledgeAnswerResolutionStateBlockId blockId,
        long ordinal,
        long createdAtEpochMillis
) {}
