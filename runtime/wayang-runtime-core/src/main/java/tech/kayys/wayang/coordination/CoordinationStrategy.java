package tech.kayys.wayang.coordination;

import io.smallrye.mutiny.Uni;

/**
 * SPI for multi-agent coordination.
 *
 * <p>A coordination strategy determines how a set of agents should collaborate
 * to satisfy a coordination request.</p>
 *
 * <p>The strategy is deliberately independent from ModelRouter. Coordination chooses
 * <em>agents and collaboration</em>; ModelRouter chooses the inference provider/model
 * used by an agent.</p>
 */
public interface CoordinationStrategy {

    /**
     * Unique strategy identifier.
     *
     * @return strategy identifier
     */
    String id();

    /**
     * Coordination pattern implemented by this strategy.
     *
     * @return coordination pattern
     */
    CoordinationPattern pattern();

    /**
     * Returns whether this strategy can handle the request.
     *
     * @param request coordination request
     * @return true when the strategy can handle it
     */
    default boolean supports(CoordinationRequest request) {
        return request != null;
    }

    /**
     * Creates an execution plan IR.
     *
     * @param request coordination request
     * @return coordination plan
     */
    CoordinationPlan plan(CoordinationRequest request);

    /**
     * Executes the coordination plan.
     *
     * @param plan execution plan
     * @param context coordination execution context
     * @return coordination result Uni
     */
    Uni<CoordinationResult> execute(
            CoordinationPlan plan,
            CoordinationContext context
    );
}
