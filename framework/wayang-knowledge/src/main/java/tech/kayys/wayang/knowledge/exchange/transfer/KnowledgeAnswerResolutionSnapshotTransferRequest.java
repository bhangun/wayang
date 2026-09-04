package tech.kayys.wayang.knowledge.exchange.transfer;

public record KnowledgeAnswerResolutionSnapshotTransferRequest(
        String transferId,
        String snapshotId,
        String sourceRuntimeId,
        String targetRuntimeId,
        String tenantId,
        String epochId,
        long offset,
        long length,
        boolean requireMerkleProof,
        String expectedFingerprint
) {
    public KnowledgeAnswerResolutionSnapshotTransferRequest {
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be >= 0");
        }
        if (length <= 0) {
            throw new IllegalArgumentException("length must be > 0");
        }
    }
}
