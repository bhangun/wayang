package tech.kayys.wayang.execution.context;

/**
 * Token budget allocation across each context layer in a model prompt.
 *
 * <p>All values are in tokens. The allocator ensures the total does not exceed
 * {@link #totalTokens()}.</p>
 */
public record ContextBudget(
    int systemTokens,
    int conversationTokens,
    int memoryTokens,
    int ragTokens,
    int artifactTokens,
    int toolStateTokens,
    int reserveTokens
) {
    /**
     * Total tokens allocated. Does not include the reserve.
     */
    public int totalTokens() {
        return systemTokens + conversationTokens + memoryTokens
            + ragTokens + artifactTokens + toolStateTokens + reserveTokens;
    }

    /**
     * A sensible default for a 128k context window model.
     */
    public static ContextBudget forWindow128k() {
        return new ContextBudget(8_000, 20_000, 15_000, 40_000, 15_000, 10_000, 20_000);
    }

    /**
     * A lean budget for a 32k context window model.
     */
    public static ContextBudget forWindow32k() {
        return new ContextBudget(2_000, 8_000, 4_000, 10_000, 4_000, 2_000, 2_000);
    }
}
