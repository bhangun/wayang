package tech.kayys.wayang.knowledge;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Default multi-source knowledge resolver.
 *
 * <p>Queries all applicable sources concurrently, combines evidence,
 * filters validity, removes duplicate items, and sorts by score descending.</p>
 */
public class DefaultKnowledgeResolver implements KnowledgeResolver {

    private final KnowledgeRegistry registry;

    public DefaultKnowledgeResolver(KnowledgeRegistry registry) {
        this.registry = registry != null ? registry : new DefaultKnowledgeRegistry();
    }

    @Override
    public CompletionStage<KnowledgeResult> resolve(
            KnowledgeQuery query,
            KnowledgeContext context
    ) {
        KnowledgeContext effectiveContext = context == null ? KnowledgeContext.empty() : context;
        List<KnowledgeSource> sources = registry.resolve(effectiveContext);

        if (sources.isEmpty()) {
            return CompletableFuture.completedFuture(KnowledgeResult.empty(query));
        }

        List<CompletableFuture<KnowledgeResult>> futures = sources.stream()
                .filter(KnowledgeSource::isHealthy)
                .map(source -> source.query(query, effectiveContext).toCompletableFuture())
                .toList();

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(ignored -> {
                    List<KnowledgeEvidence> combined = new ArrayList<>();
                    for (CompletableFuture<KnowledgeResult> future : futures) {
                        try {
                            KnowledgeResult res = future.join();
                            if (res != null && res.evidence() != null) {
                                combined.addAll(res.evidence());
                            }
                        } catch (Exception ignoredFailure) {
                            // Failure in one source does not crash entire resolution
                        }
                    }

                    List<KnowledgeEvidence> ranked = deduplicateAndRank(
                            combined,
                            query.topK(),
                            query.minScore(),
                            effectiveContext.asOf()
                    );

                    return new KnowledgeResult(
                            query,
                            ranked,
                            Map.of(
                                    "sourcesQueried", sources.size(),
                                    "evidenceCandidates", combined.size(),
                                    "evidenceReturned", ranked.size()
                            ),
                            Instant.now()
                    );
                });
    }

    private List<KnowledgeEvidence> deduplicateAndRank(
            List<KnowledgeEvidence> evidence,
            int topK,
            double minScore,
            Instant asOf
    ) {
        return evidence.stream()
                .filter(e -> e != null && e.item() != null)
                .filter(e -> e.score() >= minScore)
                .filter(e -> e.item().isCurrentlyValid(asOf))
                .collect(Collectors.toMap(
                        e -> e.item().id(),
                        e -> e,
                        (left, right) -> left.score() >= right.score() ? left : right
                ))
                .values()
                .stream()
                .sorted(Comparator.comparingDouble(KnowledgeEvidence::score).reversed())
                .limit(topK)
                .toList();
    }
}
