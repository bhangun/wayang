package tech.kayys.wayang.execution.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.communication.api.AgentRef;
import tech.kayys.wayang.communication.api.AgentRequest;
import tech.kayys.wayang.communication.api.AgentResponse;
import tech.kayys.wayang.communication.capability.CapabilityId;
import tech.kayys.wayang.communication.message.MessagePayload;
import tech.kayys.wayang.execution.AgentInvocation;
import tech.kayys.wayang.execution.AgentInvocationChain;
import tech.kayys.wayang.execution.AgentInvocationFactory;
import tech.kayys.wayang.execution.AgentInvocationHandler;
import tech.kayys.wayang.execution.AgentInvocationInterceptor;
import tech.kayys.wayang.execution.AgentResult;
import tech.kayys.wayang.execution.ExecutionLifecycle;
import tech.kayys.wayang.execution.pipeline.ExecutionPipeline;
import tech.kayys.wayang.security.identity.Principal;
import tech.kayys.wayang.security.propagation.SecurityContextSnapshot;
import tech.kayys.wayang.security.tenant.TenantContext;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionPipelineTest {

    private AgentInvocationFactory factory;
    private InterceptorPipeline interceptorPipeline;
    private SecurityContextSnapshot security;

    @BeforeEach
    void setUp() {
        factory = new DefaultAgentInvocationFactory(
                new DefaultExecutionIdGenerator(),
                new DefaultCancellationTokenFactory()
        );
        interceptorPipeline = new InterceptorPipeline();
        security = new SecurityContextSnapshot(
                Principal.anonymous(), TenantContext.empty(), Optional.empty(), Map.of()
        );
    }

    @Test
    void executesTerminalHandlerWhenNoInterceptors() throws Exception {
        AgentInvocationHandler terminal = invocation ->
                CompletableFuture.completedFuture(
                        AgentResult.of(AgentResponse.success(MessagePayload.text("done")))
                );

        ExecutionPipeline pipeline = new DefaultExecutionPipeline(interceptorPipeline, terminal);

        AgentRequest request = AgentRequest.of(
                AgentRef.local("agent-1", "Test", "rt-1"),
                CapabilityId.of("cap.test"),
                MessagePayload.text("hello")
        );
        AgentInvocation invocation = factory.root(request, security);

        AgentResult result = pipeline.execute(invocation).toCompletableFuture().get();

        assertNotNull(result);
        assertEquals(ExecutionLifecycle.COMPLETED, invocation.context().lifecycle());
    }

    @Test
    void interceptorOrderIsRespected() throws Exception {
        AtomicInteger callOrder = new AtomicInteger(0);

        AgentInvocationInterceptor first = new AgentInvocationInterceptor() {
            @Override public String name() { return "first"; }
            @Override public int order() { return 10; }
            @Override
            public CompletionStage<AgentResult> intercept(AgentInvocation inv, AgentInvocationChain chain) {
                assertEquals(0, callOrder.getAndIncrement(), "first should be called first");
                return chain.proceed(inv);
            }
        };

        AgentInvocationInterceptor second = new AgentInvocationInterceptor() {
            @Override public String name() { return "second"; }
            @Override public int order() { return 20; }
            @Override
            public CompletionStage<AgentResult> intercept(AgentInvocation inv, AgentInvocationChain chain) {
                assertEquals(1, callOrder.getAndIncrement(), "second should be called second");
                return chain.proceed(inv);
            }
        };

        // register out of order
        interceptorPipeline.register(second);
        interceptorPipeline.register(first);

        AgentInvocationHandler terminal = invocation ->
                CompletableFuture.completedFuture(
                        AgentResult.of(AgentResponse.success(MessagePayload.text("ok")))
                );

        ExecutionPipeline pipeline = new DefaultExecutionPipeline(interceptorPipeline, terminal);

        AgentRequest request = AgentRequest.of(
                AgentRef.local("agent-1", "Test", "rt-1"),
                CapabilityId.of("cap.test"),
                MessagePayload.text("hello")
        );
        AgentInvocation invocation = factory.root(request, security);
        pipeline.execute(invocation).toCompletableFuture().get();

        assertEquals(2, callOrder.get());
    }

    @Test
    void lifecycleTransitionsToFailedOnError() throws Exception {
        AgentInvocationHandler terminal = invocation ->
                CompletableFuture.failedFuture(new RuntimeException("agent error"));

        ExecutionPipeline pipeline = new DefaultExecutionPipeline(interceptorPipeline, terminal);

        AgentRequest request = AgentRequest.of(
                AgentRef.local("agent-1", "Test", "rt-1"),
                CapabilityId.of("cap.test"),
                MessagePayload.text("hello")
        );
        AgentInvocation invocation = factory.root(request, security);

        assertThrows(Exception.class,
                () -> pipeline.execute(invocation).toCompletableFuture().get()
        );
        assertEquals(ExecutionLifecycle.FAILED, invocation.context().lifecycle());
    }
}
