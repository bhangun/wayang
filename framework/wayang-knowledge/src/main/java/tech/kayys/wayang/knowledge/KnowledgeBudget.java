package tech.kayys.wayang.knowledge;

/**
 * Budget controlling how much knowledge may enter model context.
 */
public record KnowledgeBudget(
        int maxItems,
        int maxTokens,
        int maxCharacters,
        int maxPerSource,
        int maxPerAuthority,
        boolean requireProvenance,
        boolean allowLowConfidence,
        double minimumScore
) {

    public KnowledgeBudget {
        maxItems = normalize(maxItems, 8);
        maxTokens = normalize(maxTokens, 2048);
        maxCharacters = normalize(maxCharacters, maxTokens * 4);
        maxPerSource = normalize(maxPerSource, maxItems);
        maxPerAuthority = normalize(maxPerAuthority, maxItems);
        minimumScore = Math.max(0.0, Math.min(1.0, minimumScore));
    }

    private static int normalize(int value, int defaultValue) {
        return value > 0 ? value : defaultValue;
    }

    public static KnowledgeBudget defaults() {
        return new KnowledgeBudget(8, 2048, 8192, 4, 4, false, true, 0.2);
    }

    public static KnowledgeBudget slm() {
        return new KnowledgeBudget(4, 768, 3072, 2, 2, true, false, 0.4);
    }

    public static KnowledgeBudget strict() {
        return new KnowledgeBudget(5, 1024, 4096, 2, 2, true, false, 0.5);
    }
}
