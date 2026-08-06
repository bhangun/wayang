package tech.kayys.wayang.execution;

import java.util.Map;
import java.util.Optional;

/**
 * Expression condition - evaluates a boolean expression.
 */
public final class ExpressionCondition implements EdgeCondition {

    private final String expression;
    private final Map<String, Object> parameters;

    public ExpressionCondition(String expression) {
        this.expression = expression;
        this.parameters = Map.of("expression", expression);
    }

    public ExpressionCondition(String expression, Map<String, Object> params) {
        this.expression = expression;
        Map<String, Object> combined = new java.util.HashMap<>();
        combined.put("expression", expression);
        if (params != null) {
            combined.putAll(params);
        }
        this.parameters = Map.copyOf(combined);
    }

    @Override
    public ConditionType type() {
        return ConditionType.EXPRESSION;
    }

    @Override
    public boolean evaluate(ExecutionContext context, NodeResult sourceResult) {
        // Simple expression evaluation - can be extended with a proper expression
        // engine
        if (expression == null || expression.isEmpty()) {
            return true;
        }

        // Check for common patterns
        if (expression.equals("true")) {
            return true;
        }
        if (expression.equals("false")) {
            return false;
        }

        // Check for result checks
        if (expression.startsWith("result.")) {
            String field = expression.substring(7);
            if (sourceResult != null) {
                switch (field) {
                    case "success":
                        return sourceResult.isSuccess();
                    case "failure":
                        return !sourceResult.isSuccess();
                    default:
                        // Check if result has the field
                        Object output = sourceResult.getOutput();
                        if (output != null) {
                            // Simple field access
                            return true;
                        }
                }
            }
        }

        // Check for variable checks
        if (expression.startsWith("variable.")) {
            String varName = expression.substring(9);
            if (context != null && context.variables() != null) {
                return context.variables().has(new VariableKey<>(varName, Object.class));
            }
        }

        // Default to true for unknown expressions
        return true;
    }

    @Override
    public Optional<String> expression() {
        return Optional.ofNullable(expression);
    }

    @Override
    public Map<String, Object> parameters() {
        return parameters;
    }
}