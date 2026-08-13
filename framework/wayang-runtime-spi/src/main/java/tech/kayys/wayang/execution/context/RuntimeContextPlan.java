package tech.kayys.wayang.execution.context;

import tech.kayys.wayang.context.ContextData;
import java.util.List;
import java.util.Optional;

/**
 * The orchestrated execution context plan for a single step.
 * Contains the merged contextual state, tracking tokens and sources.
 */
public interface RuntimeContextPlan {
    
    /**
     * @return The aggregated, ordered context data ready for the model request.
     */
    ContextData getContextData();

    /**
     * @return The total estimated tokens consumed by this compiled context.
     */
    long getTokenUsage();

    /**
     * @return Identifiers of the providers that successfully contributed context.
     */
    List<String> getContributingProviders();
    
    /**
     * @return Optional rationale or trace for how the context was assembled (for auditing/debugging).
     */
    Optional<String> getAssemblyRationale();
}
