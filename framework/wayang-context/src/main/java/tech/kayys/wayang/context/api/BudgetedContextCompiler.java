package tech.kayys.wayang.context.api;

import tech.kayys.wayang.context.api.model.CompiledContext;

import java.nio.file.Path;

/**
 * Like {@link ContextCompiler}, but instead of a fixed hop-count cutoff,
 * ranks every reachable file by relevance and greedily assigns each the
 * richest representation that still fits a remaining token budget --
 * SKELETON, then SKELETON_PRUNED, then SIGNATURE_DIGEST -- before moving to
 * the next file. Sized, for example, to leave room in an open-weight model's
 * small context window after the system prompt and expected generation.
 *
 * The budget only ever decides how many files earn space and how much of
 * each is shown; every representation stays exact source. Nothing here
 * summarizes or paraphrases -- that trade is deliberately not on the table
 * (see Skeletonizer's javadoc for why).
 */
public interface BudgetedContextCompiler {
    CompiledContext compile(Path repoRoot, Path targetFile, int maxHops, long tokenBudget);
}
