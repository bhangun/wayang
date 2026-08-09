package com.contextcompiler.core.impl;

import com.contextcompiler.core.api.BudgetedContextCompiler;
import com.contextcompiler.core.api.RelevanceScorer;
import com.contextcompiler.core.api.Skeletonizer;
import com.contextcompiler.core.api.SymbolResolver;
import com.contextcompiler.core.api.TokenEstimator;
import com.contextcompiler.core.api.model.CompiledContext;
import com.contextcompiler.core.api.model.ModuleIndex;
import com.contextcompiler.core.api.model.ReachabilityResult;
import com.contextcompiler.core.api.model.SkeletonResult;
import com.contextcompiler.core.api.model.Tier;
import com.contextcompiler.core.api.model.TierEntry;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Greedy knapsack assembly: rank reachable files by relevance, then for each
 * one (richest first in the ranking, not in the representation) try the
 * fullest representation that still fits the remaining budget before falling
 * back to a leaner one. A file that doesn't fit even at digest level is
 * excluded -- the same outcome as never having reached it at all, just
 * budget-driven instead of hop-driven.
 */
public final class DefaultBudgetedContextCompiler implements BudgetedContextCompiler {

    private final SymbolResolver symbolResolver;
    private final Skeletonizer skeletonizer;
    private final TokenEstimator tokenEstimator;
    private final RelevanceScorer relevanceScorer;

    public DefaultBudgetedContextCompiler(SymbolResolver symbolResolver, Skeletonizer skeletonizer,
                                           TokenEstimator tokenEstimator, RelevanceScorer relevanceScorer) {
        this.symbolResolver = symbolResolver;
        this.skeletonizer = skeletonizer;
        this.tokenEstimator = tokenEstimator;
        this.relevanceScorer = relevanceScorer;
    }

    @Override
    public CompiledContext compile(Path repoRoot, Path targetFile, int maxHops, long tokenBudget) {
        long start = System.nanoTime();

        Path root = repoRoot.toAbsolutePath().normalize();
        Path target = targetFile.toAbsolutePath().normalize();

        ModuleIndex index = ModuleIndexBuilder.build(root);
        ReachabilityResult reachability = symbolResolver.resolve(index, target, maxHops);

        List<TierEntry> entries = new ArrayList<>();
        String targetSource = readOrThrow(target);
        long targetTokens = tokenEstimator.estimate(targetSource);
        entries.add(new TierEntry(target, Tier.FULL_SOURCE, targetSource, targetTokens, 0));
        long remaining = tokenBudget - targetTokens;

        List<Path> ranked = reachability.reachable().keySet().stream()
                .sorted(Comparator.comparingDouble(
                        (Path p) -> relevanceScorer.score(p, reachability)).reversed())
                .toList();

        for (Path dep : ranked) {
            if (remaining <= 0) break;

            String source;
            try {
                source = Files.readString(dep);
            } catch (IOException unreadable) {
                continue; // same policy as an unresolved import: quietly excluded
            }

            Integer hop = reachability.reachable().get(dep);
            Set<String> usedMembers = reachability.usedMembers().getOrDefault(dep, Set.of());

            TierEntry chosen = tryTier(dep, Tier.SKELETON,
                    skeletonizer.skeletonize(source).skeleton(), hop, remaining);

            if (chosen == null && !usedMembers.isEmpty()) {
                SkeletonResult pruned = skeletonizer.skeletonizePruned(source, usedMembers);
                chosen = tryTier(dep, Tier.SKELETON_PRUNED, pruned.skeleton(), hop, remaining);
            }

            if (chosen == null) {
                chosen = tryTier(dep, Tier.SIGNATURE_DIGEST, skeletonizer.digest(source), hop, remaining);
            }

            if (chosen != null) {
                entries.add(chosen);
                remaining -= chosen.tokens();
            }
        }

        int totalRepoFiles = index.pathToType().size();
        int excludedCount = totalRepoFiles - entries.size();

        long naiveDumpTokens = index.pathToType().keySet().stream()
                .mapToLong(this::estimateFileTokensOrZero)
                .sum();
        long compiledTokens = entries.stream().mapToLong(TierEntry::tokens).sum();

        return new CompiledContext(
                target, entries, excludedCount, totalRepoFiles,
                naiveDumpTokens, compiledTokens,
                Duration.ofNanos(System.nanoTime() - start),
                reachability
        );
    }

    private TierEntry tryTier(Path path, Tier tier, String content, Integer hop, long budgetRemaining) {
        long tokens = tokenEstimator.estimate(content);
        if (tokens > budgetRemaining) return null;
        return new TierEntry(path, tier, content, tokens, hop);
    }

    private long estimateFileTokensOrZero(Path path) {
        try {
            return tokenEstimator.estimate(Files.readString(path));
        } catch (IOException e) {
            return 0L;
        }
    }

    private String readOrThrow(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new UncheckedIOException("could not read target file " + file, e);
        }
    }
}
