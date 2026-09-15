package tech.kayys.wayang.execution.core;

import tech.kayys.wayang.execution.AgentInvocationInterceptor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Registry that manages the ordered list of {@link AgentInvocationInterceptor}s.
 *
 * <p>Interceptors are sorted by {@link AgentInvocationInterceptor#order()} (ascending).
 */
public final class InterceptorPipeline {

    private final List<AgentInvocationInterceptor> interceptors = new ArrayList<>();

    /** Registers an interceptor. Can be called multiple times. */
    public synchronized void register(AgentInvocationInterceptor interceptor) {
        interceptors.add(interceptor);
    }

    /** Returns a stable, sorted snapshot of registered interceptors. */
    public synchronized List<AgentInvocationInterceptor> sorted() {
        return interceptors.stream()
                .sorted(Comparator.comparingInt(AgentInvocationInterceptor::order))
                .toList();
    }
}
