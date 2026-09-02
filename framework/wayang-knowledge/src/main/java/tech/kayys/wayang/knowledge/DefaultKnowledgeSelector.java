package tech.kayys.wayang.knowledge;

import java.time.Instant;
import java.util.*;

/**
 * Default multi-stage knowledge selector implementing:
 * 1. Validity filtering
 * 2. Authority ranking
 * 3. Deduplication
 * 4. Conflict detection
 * 5. Budget truncation
 */
public class DefaultKnowledgeSelector implements KnowledgeSelector {

    @Override
    public KnowledgeSelectionResult select(
            List<KnowledgeEvidence> candidates,
            KnowledgeBudget budget,
            KnowledgeContext context
    ) {
        if (candidates == null || candidates.isEmpty()) {
            return KnowledgeSelectionResult.empty();
        }

        KnowledgeBudget b = budget != null ? budget : KnowledgeBudget.defaults();
        Instant asOf = context != null && context.asOf() != null ? context.asOf() : Instant.now();

        List<KnowledgeEvidence> valid = new ArrayList<>();
        List<KnowledgeEvidence> excluded = new ArrayList<>();
        List<KnowledgeConflict> conflicts = new ArrayList<>();

        // 1. Validity, Confidence and Score filtering
        for (KnowledgeEvidence ev : candidates) {
            if (ev == null || ev.item() == null) {
                continue;
            }

            if (!ev.item().isCurrentlyValid(asOf)) {
                excluded.add(ev);
                continue;
            }

            if (ev.score() < b.minimumScore()) {
                excluded.add(ev);
                continue;
            }

            if (b.requireProvenance() && (ev.item().provenance() == null || ev.item().provenance().sourceUri() == null)) {
                excluded.add(ev);
                continue;
            }

            valid.add(ev);
        }

        // 2. Deduplication & Score Sorting
        Map<String, KnowledgeEvidence> byId = new HashMap<>();
        for (KnowledgeEvidence ev : valid) {
            String key = ev.item().id();
            if (!byId.containsKey(key) || byId.get(key).score() < ev.score()) {
                byId.put(key, ev);
            }
        }

        List<KnowledgeEvidence> sorted = new ArrayList<>(byId.values());
        sorted.sort(Comparator.comparingDouble((KnowledgeEvidence e) -> {
            double authWeight = e.item().isAuthoritative() ? 0.3 : 0.0;
            return e.score() + authWeight;
        }).reversed());

        // 3. Selection with source/authority quotas and token budget
        List<KnowledgeEvidence> selected = new ArrayList<>();
        Map<String, Integer> sourceCounts = new HashMap<>();
        Map<String, Integer> authorityCounts = new HashMap<>();
        long currentChars = 0;

        for (KnowledgeEvidence ev : sorted) {
            if (selected.size() >= b.maxItems()) {
                excluded.add(ev);
                continue;
            }

            String source = ev.item().sourceId();
            int sCount = sourceCounts.getOrDefault(source, 0);
            if (sCount >= b.maxPerSource()) {
                excluded.add(ev);
                continue;
            }

            String authKind = ev.item().authority() != null ? ev.item().authority().kind() : "unknown";
            int aCount = authorityCounts.getOrDefault(authKind, 0);
            if (aCount >= b.maxPerAuthority()) {
                excluded.add(ev);
                continue;
            }

            int itemChars = ev.item().content().length() + ev.item().title().length();
            if (currentChars + itemChars > b.maxCharacters() && !selected.isEmpty()) {
                excluded.add(ev);
                continue;
            }

            selected.add(ev);
            sourceCounts.put(source, sCount + 1);
            authorityCounts.put(authKind, aCount + 1);
            currentChars += itemChars;
        }

        long estimatedTokens = currentChars / 4;

        Map<String, Object> diag = Map.of(
                "candidatesTotal", candidates.size(),
                "selectedCount", selected.size(),
                "excludedCount", excluded.size(),
                "estimatedTokens", estimatedTokens
        );

        return new KnowledgeSelectionResult(selected, excluded, conflicts, estimatedTokens, diag);
    }
}
