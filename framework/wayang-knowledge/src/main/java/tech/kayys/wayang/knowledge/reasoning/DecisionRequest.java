package tech.kayys.wayang.knowledge.reasoning;

import java.util.Map;

/**
 * Statement or objective requiring a structured reasoning decision.
 */
public record DecisionRequest(
        String statement,
        String objective,
        Map<String, Object> facts,
        Map<String, Object> metadata
) {

    public DecisionRequest {
        statement = statement == null ? "" : statement;
        objective = objective == null ? "" : objective;
        facts = facts == null ? Map.of() : Map.copyOf(facts);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static DecisionRequest of(String statement) {
        return new DecisionRequest(statement, "", Map.of(), Map.of());
    }
}
