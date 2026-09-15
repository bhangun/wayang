package tech.kayys.wayang.execution;

import java.util.concurrent.CompletionStage;

/**
 * Terminal handler that executes the actual agent logic.
 *
 * <p>Implementations of this interface are the "last mile" in the
 * {@link AgentInvocationChain} — they call the real agent, skill, or tool.
 */
public interface AgentInvocationHandler {

    /** Handles the invocation and returns the result asynchronously. */
    CompletionStage<AgentResult> handle(AgentInvocation invocation);
}
