package tech.kayys.wayang.knowledge.exchange.checkpoint;

public record KnowledgeAnswerResolutionCompactionPolicy(
        long safeCompactionIndex,
        boolean allowCompaction,
        String reason
) {
    public static KnowledgeAnswerResolutionCompactionPolicy allow(long index, String reason) {
        return new KnowledgeAnswerResolutionCompactionPolicy(index, true, reason);
    }

    public static KnowledgeAnswerResolutionCompactionPolicy deny(String reason) {
        return new KnowledgeAnswerResolutionCompactionPolicy(-1, false, reason);
    }
}
