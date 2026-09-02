package tech.kayys.wayang.knowledge.replay;

import tech.kayys.wayang.knowledge.decision.KnowledgeDecisionTrace;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class KnowledgeDecisionTraceComparator {

    private KnowledgeDecisionTraceComparator() {}

    public static List<String> compare(
            KnowledgeDecisionTrace original,
            KnowledgeDecisionTrace replayed) {

        List<String> divergences = new ArrayList<>();
        if (original == null || replayed == null) {
            divergences.add("trace-missing");
            return divergences;
        }

        if (!Objects.equals(original.operation(), replayed.operation())) {
            divergences.add("operation");
        }
        if (!Objects.equals(original.evidenceIds(), replayed.evidenceIds())) {
            divergences.add("evidence");
        }
        if (!Objects.equals(original.lineageIds(), replayed.lineageIds())) {
            divergences.add("lineage");
        }
        if (!Objects.equals(original.policyIds(), replayed.policyIds())) {
            divergences.add("policies");
        }
        if (!Objects.equals(original.ruleIds(), replayed.ruleIds())) {
            divergences.add("rules");
        }
        if (!Objects.equals(original.outcome(), replayed.outcome())) {
            divergences.add("outcome");
        }

        return List.copyOf(divergences);
    }
}
