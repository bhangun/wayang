package tech.kayys.wayang.guard;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tech.kayys.wayang.extension.Id;


/**
 * Guardrail Result - complete result model
 */
public record GuardrailResult(
    String id,
    boolean passed,
    String rule,
    String message,
    List<Violation> violations,
    Map<String, Object> metadata,
    long validationTimeMs,
    GuardrailType type,
    double score
) {
    public static GuardrailResult passed() {
        return new GuardrailResult(
            Id.random().asString(),
            true,
            null,
            null,
            List.of(),
            Map.of(),
            0,
            GuardrailType.PROMPT_GUARD,
            1.0
        );
    }
    
    public static GuardrailResult passed(double score) {
        return new GuardrailResult(
            Id.random().asString(),
            true,
            null,
            null,
            List.of(),
            Map.of(),
            0,
            GuardrailType.PROMPT_GUARD,
            score
        );
    }
    
    public static GuardrailResult failed(String rule, String message, Violation... violations) {
        return new GuardrailResult(
            Id.random().asString(),
            false,
            rule,
            message,
            List.of(violations),
            Map.of(),
            0,
            GuardrailType.PROMPT_GUARD,
            0.0
        );
    }
    
    public GuardrailResult withViolation(Violation violation) {
        List<Violation> newViolations = new ArrayList<>(violations);
        newViolations.add(violation);
        return new GuardrailResult(id, passed, rule, message, newViolations, 
            metadata, validationTimeMs, type, score);
    }
}
