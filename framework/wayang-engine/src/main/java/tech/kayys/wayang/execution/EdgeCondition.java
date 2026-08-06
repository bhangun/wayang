package tech.kayys.wayang.execution;

import java.util.Map;
import java.util.Optional;

/**
 * A condition that determines whether an edge should be traversed.
 * 
 * <p>
 * Built-in conditions:
 * <ul>
 * <li>ALWAYS - Always traverse</li>
 * <li>SUCCESS - Traverse if source node succeeded</li>
 * <li>FAILURE - Traverse if source node failed</li>
 * <li>EXPRESSION - Traverse if expression evaluates to true</li>
 * <li>TOOL_RESULT - Traverse based on tool result</li>
 * <li>VARIABLE - Traverse based on variable value</li>
 * <li>TIMEOUT - Traverse on timeout</li>
 * <li>HUMAN_APPROVAL - Traverse after human approval</li>
 * <li>EVENT - Traverse on event occurrence</li>
 * <li>PROBABILITY - Traverse with probability</li>
 * </ul>
 */
public interface EdgeCondition {

    /**
     * Returns the type of condition.
     */
    ConditionType type();

    /**
     * Checks if the condition is satisfied.
     * 
     * @param context      The execution context
     * @param sourceResult The result from the source node
     * @return true if the condition is satisfied
     */
    boolean evaluate(ExecutionContext context, NodeResult sourceResult);

    /**
     * Returns the expression (if applicable).
     */
    default Optional<String> expression() {
        return Optional.empty();
    }

    /**
     * Returns the parameters for the condition.
     */
    Map<String, Object> parameters();

    /**
     * Creates an ALWAYS condition.
     */
    static EdgeCondition always() {
        return new AlwaysCondition();
    }

    /**
     * Creates a SUCCESS condition.
     */
    static EdgeCondition onSuccess() {
        return new SuccessCondition();
    }

    /**
     * Creates a FAILURE condition.
     */
    static EdgeCondition onFailure() {
        return new FailureCondition();
    }

    /**
     * Creates an EXPRESSION condition.
     */
    static EdgeCondition expression(String expression) {
        return new ExpressionCondition(expression);
    }

    /**
     * Creates a PROBABILITY condition.
     */
    static EdgeCondition probability(double probability) {
        return new ProbabilityCondition(probability);
    }
}