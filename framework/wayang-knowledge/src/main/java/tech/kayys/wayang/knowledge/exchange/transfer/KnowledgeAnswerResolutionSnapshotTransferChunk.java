package tech.kayys.wayang.knowledge.exchange.transfer;

import java.util.Arrays;

public record KnowledgeAnswerResolutionSnapshotTransferChunk(
        String transferId,
        String snapshotId,
        long offset,
        byte[] data,
        String chunkFingerprint,
        String merkleProof
) {
    public KnowledgeAnswerResolutionSnapshotTransferChunk {
        data = data == null ? new byte[0] : Arrays.copyOf(data, data.length);
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be >= 0");
        }
    }

    @Override
    public byte[] data() {
        return Arrays.copyOf(data, data.length);
    }
}
