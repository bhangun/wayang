package tech.kayys.wayang.knowledge.snapshot.packing;

public record KnowledgeAnswerResolutionCompressionDecision(
        boolean useCompression,
        String algorithm,
        long originalBytes,
        long compressedBytes,
        double savingsRatio,
        String reason
) {}
