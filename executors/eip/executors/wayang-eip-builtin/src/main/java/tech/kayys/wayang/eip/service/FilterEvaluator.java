package tech.kayys.wayang.eip.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Service to evaluate filter expressions
 */
@ApplicationScoped
public class FilterEvaluator {

    private static final Logger LOG = LoggerFactory.getLogger(FilterEvaluator.class);

    public Uni<Boolean> evaluate(String expression, Map<String, Object> context) {
        return Uni.createFrom().item(() -> {
            try {
                // For now, simple true/false or basic script evaluation
                // In a real implementation, this would use SpEL, MVEL or JsonPath
                LOG.debug("Evaluating expression: {}", expression);

                // Simple placeholder logic: if expression is "true" or matches some context
                // value
                if ("true".equalsIgnoreCase(expression))
                    return true;
                if ("false".equalsIgnoreCase(expression))
                    return false;

                // Basic check if it's a context key
                if (context.containsKey(expression)) {
                    Object val = context.get(expression);
                    if (val instanceof Boolean)
                        return (Boolean) val;
                    return val != null;
                }

                return true; // Default to true if cannot evaluate
            } catch (Exception e) {
                LOG.error("Failed to evaluate expression: {}", expression, e);
                return false;
            }
        });
    }
}
