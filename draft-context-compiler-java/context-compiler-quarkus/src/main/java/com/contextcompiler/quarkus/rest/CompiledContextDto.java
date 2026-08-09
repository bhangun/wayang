package com.contextcompiler.quarkus.rest;

import com.contextcompiler.core.api.model.CompiledContext;
import com.contextcompiler.core.api.model.Tier;

import java.util.List;

/**
 * REST-facing view of CompiledContext -- paths as strings, no domain types
 * leaked over the wire. Keep this DTO in sync with CompiledContext manually;
 * it is a deliberate seam, not an oversight.
 */
public record CompiledContextDto(
        String targetFile,
        List<TierEntryDto> entries,
        int excludedCount,
        int totalRepoFiles,
        long naiveDumpTokens,
        long compiledTokens,
        double reductionPct,
        double buildMillis,
        String summary
) {

    public record TierEntryDto(String path, Tier tier, String content, long tokens, Integer hopDistance) {}

    public static CompiledContextDto from(CompiledContext c) {
        List<TierEntryDto> entries = c.entries().stream()
                .map(e -> new TierEntryDto(e.path().toString(), e.tier(), e.content(), e.tokens(), e.hopDistance()))
                .toList();
        return new CompiledContextDto(
                c.targetFile().toString(),
                entries,
                c.excludedCount(),
                c.totalRepoFiles(),
                c.naiveDumpTokens(),
                c.compiledTokens(),
                c.reductionPct(),
                c.buildDuration().toNanos() / 1_000_000.0,
                c.summary()
        );
    }
}
