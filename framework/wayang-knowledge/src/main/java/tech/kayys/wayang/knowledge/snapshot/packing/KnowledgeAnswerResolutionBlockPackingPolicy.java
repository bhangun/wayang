package tech.kayys.wayang.knowledge.snapshot.packing;

public record KnowledgeAnswerResolutionBlockPackingPolicy(
        long targetBlockBytes,
        long minimumBlockBytes,
        long maximumBlockBytes,
        long maximumPackEntries,
        boolean enablePacking,
        boolean enableSplitting,
        boolean enableCompression,
        double minimumCompressionSavingsRatio
) {
    public static KnowledgeAnswerResolutionBlockPackingPolicy defaults() {
        return new KnowledgeAnswerResolutionBlockPackingPolicy(
                4 * 1024 * 1024,
                256 * 1024,
                8 * 1024 * 1024,
                4096,
                true,
                true,
                true,
                0.10
        );
    }
}
