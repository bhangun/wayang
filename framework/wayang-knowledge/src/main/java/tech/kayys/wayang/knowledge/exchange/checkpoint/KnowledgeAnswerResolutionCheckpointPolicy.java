package tech.kayys.wayang.knowledge.exchange.checkpoint;

public record KnowledgeAnswerResolutionCheckpointPolicy(
        long checkpointIntervalEntries,
        long minimumCompactionEntries,
        int retainedCheckpoints,
        boolean requireVerification,
        boolean requireAtomicInstall
) {
    public static KnowledgeAnswerResolutionCheckpointPolicy defaults() {
        return new KnowledgeAnswerResolutionCheckpointPolicy(
                10_000,
                20_000,
                2,
                true,
                true
        );
    }

    public KnowledgeAnswerResolutionCheckpointPolicy {
        if (checkpointIntervalEntries <= 0) {
            throw new IllegalArgumentException("checkpointIntervalEntries must be > 0");
        }
        if (minimumCompactionEntries < 0) {
            throw new IllegalArgumentException("minimumCompactionEntries must be >= 0");
        }
        if (retainedCheckpoints < 1) {
            throw new IllegalArgumentException("retainedCheckpoints must be >= 1");
        }
    }
}
