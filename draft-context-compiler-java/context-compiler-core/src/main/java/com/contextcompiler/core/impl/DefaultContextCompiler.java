package com.contextcompiler.core.impl;

import com.contextcompiler.core.api.ContextCompiler;
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
import java.util.List;
import java.util.Map;

/**
 * Orchestrates the three-tier context strategy, composed over the abstraction
 * layer (SymbolResolver / Skeletonizer / TokenEstimator) so any of the three
 * can be swapped -- e.g. HeuristicSymbolResolver for a plain, dependency-free
 * default, or TypeAwareSymbolResolver when a source root is available -- with
 * no change to this class, matching the repository-abstraction pattern used
 * elsewhere in this codebase.
 */
public final class DefaultContextCompiler implements ContextCompiler {

    private final SymbolResolver symbolResolver;
    private final Skeletonizer skeletonizer;
    private final TokenEstimator tokenEstimator;

    public DefaultContextCompiler(SymbolResolver symbolResolver, Skeletonizer skeletonizer,
                                   TokenEstimator tokenEstimator) {
        this.symbolResolver = symbolResolver;
        this.skeletonizer = skeletonizer;
        this.tokenEstimator = tokenEstimator;
    }

    @Override
    public CompiledContext compile(Path repoRoot, Path targetFile, int maxHops) {
        long start = System.nanoTime();

        Path root = repoRoot.toAbsolutePath().normalize();
        Path target = targetFile.toAbsolutePath().normalize();

        ModuleIndex index = ModuleIndexBuilder.build(root);
        ReachabilityResult reachability = symbolResolver.resolve(index, target, maxHops);

        List<TierEntry> entries = new ArrayList<>();
        String targetSource = readOrThrow(target);
        entries.add(new TierEntry(target, Tier.FULL_SOURCE, targetSource,
                tokenEstimator.estimate(targetSource), 0));

        reachability.reachable().entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(dep -> {
                    try {
                        String source = Files.readString(dep.getKey());
                        SkeletonResult skeleton = skeletonizer.skeletonize(source);
                        entries.add(new TierEntry(dep.getKey(), Tier.SKELETON, skeleton.skeleton(),
                                tokenEstimator.estimate(skeleton.skeleton()), dep.getValue()));
                    } catch (Exception unreadableOrUnparsable) {
                        // Treated as a tier-2 miss, same policy as an unresolved import.
                    }
                });

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
