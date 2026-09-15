package tech.kayys.wayang.execution;

import java.util.concurrent.CompletionStage;

/**
 * Represents the remaining portion of an invocation chain.
 *
 * <p>Each {@link AgentInvocationInterceptor} receives a chain that, when invoked,
 * will call the next interceptor or the terminal {@link AgentInvocationHandler}.
 */
public interface AgentInvocationChain {

    /** Proceed to the next interceptor or terminal handler. */
    CompletionStage<AgentResult> proceed(AgentInvocation invocation);
}
