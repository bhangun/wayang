package tech.kayys.wayang.execution;

import java.util.concurrent.CompletionStage;

/**
 * An interceptor that wraps around agent invocation execution.
 *
 * <p>Interceptors are used for cross-cutting concerns such as:
 * <ul>
 *   <li>Tracing / distributed context propagation</li>
 *   <li>Authorization checks</li>
 *   <li>Obligation enforcement (audit, rate-limit, HITL)</li>
 *   <li>Timeout handling</li>
 *   <li>Retry logic</li>
 * </ul>
 *
 * <p>The {@code chain} parameter allows the interceptor to delegate to the next
 * interceptor or the terminal handler.
 */
public interface AgentInvocationInterceptor {

    /**
     * A short, human-readable name for this interceptor (for logging/ordering).
     */
    String name();

    /**
     * Processing order — lower values execute first (before the terminal handler).
     * Default ordering: Trace(0), Authorization(100), Obligations(200), Timeout(500), Retry(600).
     */
    int order();

    /** Intercept the invocation and optionally delegate to the chain. */
    CompletionStage<AgentResult> intercept(
            AgentInvocation invocation,
            AgentInvocationChain chain
    );
}
