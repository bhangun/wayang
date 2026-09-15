package tech.kayys.wayang.execution.core;

import tech.kayys.wayang.execution.*;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Default {@link AgentInvocationChain} implementation.
 *
 * <p>Advances through the ordered list of interceptors and finally invokes
 * the terminal {@link AgentInvocationHandler}.
 */
public final class DefaultAgentInvocationChain implements AgentInvocationChain {

    private final List<AgentInvocationInterceptor> interceptors;
    private final AgentInvocationHandler terminal;
    private final int index;

    public DefaultAgentInvocationChain(
            List<AgentInvocationInterceptor> interceptors,
            AgentInvocationHandler terminal) {
        this(interceptors, terminal, 0);
    }

    private DefaultAgentInvocationChain(
            List<AgentInvocationInterceptor> interceptors,
            AgentInvocationHandler terminal,
            int index) {
        this.interceptors = interceptors;
        this.terminal = terminal;
        this.index = index;
    }

    @Override
    public CompletionStage<AgentResult> proceed(AgentInvocation invocation) {
        if (index < interceptors.size()) {
            AgentInvocationInterceptor interceptor = interceptors.get(index);
            AgentInvocationChain next = new DefaultAgentInvocationChain(
                    interceptors, terminal, index + 1
            );
            return interceptor.intercept(invocation, next);
        }
        return terminal.handle(invocation);
    }
}
