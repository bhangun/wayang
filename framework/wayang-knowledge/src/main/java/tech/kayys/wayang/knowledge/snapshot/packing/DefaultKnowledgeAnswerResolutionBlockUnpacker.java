package tech.kayys.wayang.knowledge.snapshot.packing;

import java.util.Objects;

public final class DefaultKnowledgeAnswerResolutionBlockUnpacker
        implements KnowledgeAnswerResolutionBlockUnpacker {

    private final KnowledgeAnswerResolutionBlockCompressor compressor;

    public DefaultKnowledgeAnswerResolutionBlockUnpacker(
            KnowledgeAnswerResolutionBlockCompressor compressor) {
        this.compressor = compressor != null ? compressor : new GzipKnowledgeAnswerResolutionBlockCompressor();
    }

    public DefaultKnowledgeAnswerResolutionBlockUnpacker() {
        this(new GzipKnowledgeAnswerResolutionBlockCompressor());
    }

    @Override
    public byte[] unpack(KnowledgeAnswerResolutionPackedStateBlock packedBlock) {
        Objects.requireNonNull(packedBlock, "packedBlock");

        if ("none".equalsIgnoreCase(packedBlock.compression())
                || packedBlock.compression().isBlank()) {
            return packedBlock.data();
        }

        if (compressor.algorithm().equalsIgnoreCase(packedBlock.compression())) {
            return compressor.decompress(packedBlock.data());
        }

        throw new IllegalArgumentException(
                "Unsupported compression algorithm: " + packedBlock.compression());
    }
}
