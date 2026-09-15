package tech.kayys.wayang.coordination;

import io.smallrye.mutiny.Uni;

/**
 * Coordination Execution Engine interface.
 */
public interface CoordinationExecutionEngine {

    Uni<CoordinationResult> coordinate(CoordinationRequest request, CoordinationContext context);
}
