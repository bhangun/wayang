package tech.kayys.wayang.knowledge.snapshot.packing;

public final class KnowledgeAnswerResolutionCompressionPlanner {

    private final KnowledgeAnswerResolutionBlockPackingPolicy policy;

    public KnowledgeAnswerResolutionCompressionPlanner(
            KnowledgeAnswerResolutionBlockPackingPolicy policy) {
        this.policy = policy != null ? policy : KnowledgeAnswerResolutionBlockPackingPolicy.defaults();
    }

    public KnowledgeAnswerResolutionCompressionPlanner() {
        this(KnowledgeAnswerResolutionBlockPackingPolicy.defaults());
    }

    public KnowledgeAnswerResolutionCompressionDecision decide(
            long originalBytes,
            long compressedBytes,
            String algorithm) {

        if (!policy.enableCompression()) {
            return new KnowledgeAnswerResolutionCompressionDecision(
                    false,
                    "none",
                    originalBytes,
                    compressedBytes,
                    0,
                    "Compression disabled"
            );
        }

        if (originalBytes <= 0) {
            return new KnowledgeAnswerResolutionCompressionDecision(
                    false,
                    "none",
                    originalBytes,
                    compressedBytes,
                    0,
                    "Empty block"
            );
        }

        double savings = (double) (originalBytes - compressedBytes) / originalBytes;

        if (savings >= policy.minimumCompressionSavingsRatio()) {
            return new KnowledgeAnswerResolutionCompressionDecision(
                    true,
                    algorithm,
                    originalBytes,
                    compressedBytes,
                    savings,
                    "Compression provides sufficient savings"
            );
        }

        return new KnowledgeAnswerResolutionCompressionDecision(
                false,
                "none",
                originalBytes,
                originalBytes,
                0,
                "Compression savings below threshold"
        );
    }
}
