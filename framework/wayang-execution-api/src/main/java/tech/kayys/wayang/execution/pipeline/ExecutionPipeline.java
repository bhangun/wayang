package tech.kayys.wayang.execution.pipeline;

import tech.kayys.wayang.execution.AgentInvocation;
import tech.kayys.wayang.execution.AgentResult;

import java.util.concurrent.CompletionStage;

/**
 * The central execution pipeline for agent invocations.
 *
 * <p>Runs an {@link AgentInvocation} through the full interceptor chain
 * (trace → authorization → obligations → timeout → retry → terminal handler).
 *
 * <p>This is the single execution path regardless of protocol origin
 * (local, A2A, ANP, workflow).
 */
public interface ExecutionPipeline {

    /**
     * Executes the given invocation through the full pipeline.
     *
     * @param invocation the invocation to execute
     * @return a future that completes with the result or fails with an exception
     */
    CompletionStage<AgentResult> execute(AgentInvocation invocation);
}
