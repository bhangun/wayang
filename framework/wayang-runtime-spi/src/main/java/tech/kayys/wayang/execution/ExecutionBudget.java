package tech.kayys.wayang.execution;

import java.time.Duration;

/**
 * Defines the resource limits and cache policy for an agent execution loop.
 *
 * <p>Four preset profiles map to the CLI flags {@code FAST / BALANCED / THOROUGH / DEBUG}:
 *
 * <ul>
 *   <li>{@link #fast()}      – lightweight, tool cache ON with short TTL, minimal retries</li>
 *   <li>{@link #balanced()}  – default; tool + retrieval cache ON with moderate TTL</li>
 *   <li>{@link #thorough()}  – higher limits, longer cache TTL, more retries</li>
 *   <li>{@link #debug()}     – low limits, all caching OFF for full transparency</li>
 * </ul>
 */
public record ExecutionBudget(
        // ── Time / step limits ─────────────────────────────────────────────────
        Duration maxDuration,
        int maxSteps,
        long maxToolCalls,
        long maxInputTokens,
        long maxOutputTokens,
        // ── Resilience ─────────────────────────────────────────────────────────
        int maxRetries,
        int maxConcurrentTools,
        // ── Tool cache ─────────────────────────────────────────────────────────
        boolean toolCacheEnabled,
        Duration toolCacheTtl,
        // ── Retrieval / RAG cache ──────────────────────────────────────────────
        boolean retrievalCacheEnabled,
        Duration retrievalCacheTtl,
        // ── Research cache ─────────────────────────────────────────────────────
        boolean researchCacheEnabled,
        Duration researchCacheTtl,
        // ── Multi-agent limits ─────────────────────────────────────────────────
        long maxA2ARequests,
        long maxResearchSources
) {

    // ── Legacy / default ──────────────────────────────────────────────────────

    /**
     * Backwards-compatible default — same behaviour as the old {@code defaults()}.
     */
    public static ExecutionBudget defaults() { return balanced(); }

    // ── Preset profiles ───────────────────────────────────────────────────────

    /**
     * Fast profile — lightweight execution, tool cache ON with a 5-minute TTL.
     * Suitable for quick queries and chat-style interactions.
     */
    public static ExecutionBudget fast() {
        return new ExecutionBudget(
                Duration.ofMinutes(2), 15, 30,
                100_000, 20_000,
                1, 1,
                true,  Duration.ofMinutes(5),
                true,  Duration.ofMinutes(5),
                false, null,
                3, 5
        );
    }

    /**
     * Balanced profile (default) — tool + retrieval cache ON with a 30-minute TTL.
     */
    public static ExecutionBudget balanced() {
        return new ExecutionBudget(
                Duration.ofMinutes(5), 25, 50,
                200_000, 50_000,
                3, 2,
                true,  Duration.ofMinutes(30),
                true,  Duration.ofMinutes(30),
                false, null,
                10, 20
        );
    }

    /**
     * Thorough profile — extended limits, longer cache TTL, more retries.
     * Suitable for research, code review, and deep analysis tasks.
     */
    public static ExecutionBudget thorough() {
        return new ExecutionBudget(
                Duration.ofMinutes(20), 50, 100,
                400_000, 100_000,
                5, 4,
                true,  Duration.ofHours(2),
                true,  Duration.ofHours(2),
                true,  Duration.ofHours(4),
                20, 50
        );
    }

    /**
     * Debug profile — minimal limits, all caching OFF for full transparency.
     * Every tool call is executed fresh; no cached results are reused.
     */
    public static ExecutionBudget debug() {
        return new ExecutionBudget(
                Duration.ofMinutes(5), 25, 50,
                200_000, 50_000,
                1, 1,
                false, null,
                false, null,
                false, null,
                10, 20
        );
    }

    // ── Convenience ───────────────────────────────────────────────────────────

    /** Returns {@code true} if any cache tier is enabled. */
    public boolean isCachingEnabled() {
        return toolCacheEnabled || retrievalCacheEnabled || researchCacheEnabled;
    }
}

