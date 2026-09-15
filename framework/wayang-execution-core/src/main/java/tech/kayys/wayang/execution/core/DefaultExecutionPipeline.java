package tech.kayys.wayang.execution.core;

import tech.kayys.wayang.execution.*;
import tech.kayys.wayang.execution.pipeline.ExecutionPipeline;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Default {@link ExecutionPipeline} that runs invocations through the
 * ordered interceptor chain and then the terminal handler.
 */
public final class DefaultExecutionPipeline implements ExecutionPipeline {

    private final InterceptorPipeline interceptorPipeline;
    private final AgentInvocationHandler terminalHandler;

    public DefaultExecutionPipeline(
            InterceptorPipeline interceptorPipeline,
            AgentInvocationHandler terminalHandler) {
        this.interceptorPipeline = Objects.requireNonNull(interceptorPipeline, "interceptorPipeline");
        this.terminalHandler = Objects.requireNonNull(terminalHandler, "terminalHandler");
    }

    @Override
    public CompletionStage<AgentResult> execute(AgentInvocation invocation) {
        Objects.requireNonNull(invocation, "invocation");

        // Transition to RUNNING state
        invocation.context().transition(ExecutionLifecycle.RUNNING);

        DefaultAgentInvocationChain chain = new DefaultAgentInvocationChain(
                interceptorPipeline.sorted(),
                terminalHandler
        );

        return chain.proceed(invocation)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        invocation.context().transition(ExecutionLifecycle.FAILED);
                    } else {
                        invocation.context().transition(ExecutionLifecycle.COMPLETED);
                    }
                });
    }
}
