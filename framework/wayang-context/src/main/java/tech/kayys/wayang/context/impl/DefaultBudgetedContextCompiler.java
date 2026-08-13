package tech.kayys.wayang.context.impl;

import tech.kayys.wayang.context.api.BudgetedContextCompiler;
import tech.kayys.wayang.context.api.RelevanceScorer;
import tech.kayys.wayang.context.api.Skeletonizer;
import tech.kayys.wayang.context.api.SymbolResolver;
import tech.kayys.wayang.context.api.TokenEstimator;
import tech.kayys.wayang.context.api.model.CompiledContext;
import tech.kayys.wayang.context.api.model.ModuleIndex;
import tech.kayys.wayang.context.api.model.ReachabilityResult;
import tech.kayys.wayang.context.api.model.SkeletonResult;
import tech.kayys.wayang.context.api.model.SourceChunk;
import tech.kayys.wayang.context.api.model.Tier;
import tech.kayys.wayang.context.api.model.TierEntry;
import tech.kayys.wayang.resource.ContentPart;

import java.io.IOException;
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
    private final SourceCodeChunker sourceChunker;

    public DefaultBudgetedContextCompiler(SymbolResolver symbolResolver, Skeletonizer skeletonizer,
                                           TokenEstimator tokenEstimator, RelevanceScorer relevanceScorer) {
        this(symbolResolver, skeletonizer, tokenEstimator, relevanceScorer, new SourceCodeChunker());
    }

    public DefaultBudgetedContextCompiler(SymbolResolver symbolResolver, Skeletonizer skeletonizer,
                                           TokenEstimator tokenEstimator, RelevanceScorer relevanceScorer,
                                           SourceCodeChunker sourceChunker) {
        this.symbolResolver = symbolResolver;
        this.skeletonizer = skeletonizer;
        this.tokenEstimator = tokenEstimator;
        this.relevanceScorer = relevanceScorer;
        this.sourceChunker = sourceChunker;
    }

    @Override
    public CompiledContext compile(Path repoRoot, Path targetFile, int maxHops, long tokenBudget) {
        long start = System.nanoTime();

        Path root = repoRoot.toAbsolutePath().normalize();
        Path target = targetFile.toAbsolutePath().normalize();

        ModuleIndex index = ModuleIndexBuilder.build(root);
        ReachabilityResult reachability = symbolResolver.resolve(index, target, maxHops);

        List<TierEntry> entries = new ArrayList<>();
        long remaining = addTargetContext(entries, target, tokenBudget);

        List<Path> ranked = reachability.reachable().keySet().stream()
                .sorted(Comparator.comparingDouble(
                        (Path p) -> relevanceScorer.score(p, reachability)).reversed())
                .toList();

        for (Path dep : ranked) {
            if (remaining <= 0) break;

            List<SourceChunk> chunks;
            try {
                chunks = sourceChunker.chunk(dep);
            } catch (RuntimeException unreadable) {
                continue; // same policy as an unresolved import: quietly excluded
            }

            Integer hop = reachability.reachable().get(dep);
            Set<String> usedMembers = reachability.usedMembers().getOrDefault(dep, Set.of());

            TierEntry chosen = null;
            if (chunks.size() == 1) {
                String source = chunks.get(0).content();
                chosen = tryTier(dep, Tier.SKELETON,
                        skeletonizer.skeletonize(source).skeleton(), hop, remaining);

                if (chosen == null && !usedMembers.isEmpty()) {
                    SkeletonResult pruned = skeletonizer.skeletonizePruned(source, usedMembers);
                    chosen = tryTier(dep, Tier.SKELETON_PRUNED, pruned.skeleton(), hop, remaining);
                }

                if (chosen == null) {
                    chosen = tryTier(dep, Tier.SIGNATURE_DIGEST, skeletonizer.digest(source), hop, remaining);
                }
            }

            if (chosen != null) {
                entries.add(chosen);
                remaining -= chosen.tokens();
            } else {
                remaining = addChunks(entries, chunks, hop, remaining);
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

    private long addTargetContext(List<TierEntry> entries, Path target, long tokenBudget) {
        List<SourceChunk> chunks = sourceChunker.chunk(target);
        if (chunks.size() == 1) {
            SourceChunk chunk = chunks.get(0);
            long tokens = tokenEstimator.estimate(chunk.content());
            if (tokens <= tokenBudget) {
                entries.add(new TierEntry(target, Tier.FULL_SOURCE, java.util.List.of(new ContentPart.Text(chunk.content(), java.util.Map.of())), tokens, 0));
                return tokenBudget - tokens;
            }
        }
        return addChunks(entries, chunks, 0, tokenBudget);
    }

    private long addChunks(List<TierEntry> entries, List<SourceChunk> chunks, Integer hop, long remaining) {
        for (SourceChunk chunk : chunks) {
            if (remaining <= 0) break;
            TierEntry entry = tryTier(chunk.path(), Tier.SOURCE_CHUNK, chunk.toPromptContent(), hop, remaining);
            if (entry == null) break;
            entries.add(entry);
            remaining -= entry.tokens();
        }
        return remaining;
    }

    private TierEntry tryTier(Path path, Tier tier, String content, Integer hop, long budgetRemaining) {
        long tokens = tokenEstimator.estimate(content);
        if (tokens > budgetRemaining) return null;
        return new TierEntry(path, tier, java.util.List.of(new ContentPart.Text(content, java.util.Map.of())), tokens, hop);
    }

    private long estimateFileTokensOrZero(Path path) {
        try {
            return tokenEstimator.estimate(Files.readString(path));
        } catch (IOException e) {
            return 0L;
        }
    }

}
