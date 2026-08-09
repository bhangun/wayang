package tech.kayys.wayang.context.impl;

import tech.kayys.wayang.context.api.TokenEstimator;

/**
 * Rough heuristic: ~4 characters per token. Good enough to compare compiled
 * vs. naive payload size; treat the reduction percentage, not the absolute
 * count, as the meaningful figure -- same caveat as the Python original.
 */
public final class DefaultTokenEstimator implements TokenEstimator {

    private static final int CHARS_PER_TOKEN = 4;

    @Override
    public long estimate(String text) {
        if (text == null || text.isEmpty()) return 1;
        return Math.max(1, text.length() / CHARS_PER_TOKEN);
    }
}
