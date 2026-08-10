package tech.kayys.wayang.context.api.model;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/**
 * The assembled prompt-ready output of one compile() call, plus token-count
 * diagnostics comparing this strategy against a naive full-repo dump, plus
 * the raw ReachabilityResult so callers can see -- and disclose -- what the
 * resolver missed rather than pretending it's exhaustive.
 */
public final class CompiledContext {

    private final Path targetFile;
    private final List<TierEntry> entries;
    private final int excludedCount;
    private final int totalRepoFiles;
    private final long naiveDumpTokens;
    private final long compiledTokens;
    private final Duration buildDuration;
    private final ReachabilityResult diagnostics;

    public CompiledContext(Path targetFile, List<TierEntry> entries, int excludedCount,
                            int totalRepoFiles, long naiveDumpTokens, long compiledTokens,
                            Duration buildDuration, ReachabilityResult diagnostics) {
        this.targetFile = targetFile;
        this.entries = List.copyOf(entries);
        this.excludedCount = excludedCount;
        this.totalRepoFiles = totalRepoFiles;
        this.naiveDumpTokens = naiveDumpTokens;
        this.compiledTokens = compiledTokens;
        this.buildDuration = buildDuration;
        this.diagnostics = diagnostics;
    }

    public Path targetFile() {
        return targetFile;
    }

    public List<TierEntry> entries() {
        return entries;
    }

    public int excludedCount() {
        return excludedCount;
    }

    public int totalRepoFiles() {
        return totalRepoFiles;
    }

    public long naiveDumpTokens() {
        return naiveDumpTokens;
    }

    public long compiledTokens() {
        return compiledTokens;
    }

    public Duration buildDuration() {
        return buildDuration;
    }

    public ReachabilityResult diagnostics() {
        return diagnostics;
    }

    public double reductionPct() {
        if (naiveDumpTokens == 0) return 0.0;
        return 100.0 * (1 - ((double) compiledTokens / naiveDumpTokens));
    }

    public String toPromptString() {
        StringBuilder sb = new StringBuilder();
        for (TierEntry entry : entries) {
            sb.append("// ---- [").append(tierLabel(entry.tier())).append("] ").append(entry.path()).append(" ----\n");
            sb.append(entry.content());
            sb.append("\n\n");
        }
        return sb.toString().stripTrailing();
    }

    private String tierLabel(Tier tier) {
        return switch (tier) {
            case FULL_SOURCE -> "FULL SOURCE";
            case SOURCE_CHUNK -> "SOURCE CHUNK";
            case SKELETON -> "SKELETON";
            case SKELETON_PRUNED -> "SKELETON, PRUNED TO USED MEMBERS";
            case SIGNATURE_DIGEST -> "SIGNATURE DIGEST";
            case EXCLUDED -> "EXCLUDED"; // never actually materialized as an entry
        };
    }

    public String summary() {
        long tier1 = entries.stream().filter(e -> e.tier() == Tier.FULL_SOURCE).count();
        long chunked = entries.stream().filter(e -> e.tier() == Tier.SOURCE_CHUNK).count();
        long tier2 = entries.stream().filter(e -> e.tier() == Tier.SKELETON).count();
        long tier2Pruned = entries.stream().filter(e -> e.tier() == Tier.SKELETON_PRUNED).count();
        long tier3 = entries.stream().filter(e -> e.tier() == Tier.SIGNATURE_DIGEST).count();
        StringBuilder sb = new StringBuilder();
        sb.append("Target file: ").append(targetFile).append('\n');
        sb.append("Repo files scanned: ").append(totalRepoFiles).append('\n');
        sb.append("Tier 1 (full source): ").append(tier1).append(" file(s)\n");
        sb.append("Tier 1b (source chunks): ").append(chunked).append(" chunk(s)\n");
        sb.append("Tier 2 (full skeleton): ").append(tier2).append(" files\n");
        sb.append("Tier 2b (pruned skeleton): ").append(tier2Pruned).append(" files\n");
        sb.append("Tier 3 (signature digest): ").append(tier3).append(" files\n");
        sb.append("Tier 4 (excluded): ").append(excludedCount).append(" files\n");
        sb.append("Naive full-dump estimate: ").append(naiveDumpTokens).append(" tokens\n");
        sb.append("Compiled context: ").append(compiledTokens).append(" tokens\n");
        sb.append(String.format("Reduction: %.1f%%%n", reductionPct()));
        sb.append(String.format("Build time: %.2f ms%n", buildDuration.toNanos() / 1_000_000.0));

        if (diagnostics != null) {
            if (!diagnostics.dynamicDispatchFiles().isEmpty()) {
                sb.append("Warning: ").append(diagnostics.dynamicDispatchFiles().size())
                  .append(" file(s) use reflection-based dynamic dispatch")
                  .append(" \u2014 targets may be missing from tier 2.\n");
            }
            if (!diagnostics.eventAnnotationHints().isEmpty()) {
                sb.append("Warning: ").append(diagnostics.eventAnnotationHints().size())
                  .append(" file(s) use event-style annotations (e.g. @Observes, @ConsumeEvent)")
                  .append(" \u2014 handlers may be missing from tier 2.\n");
            }
            if (!diagnostics.nameCollisions().isEmpty()) {
                sb.append("Note: ").append(diagnostics.nameCollisions().size())
                  .append(" call name(s) resolved to more than one file (name-only resolution)")
                  .append(" \u2014 tier 2 may include false positives.\n");
            }
        }
        return sb.toString().stripTrailing();
    }
}
