package tech.kayys.wayang.communication.local;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.communication.api.*;
import tech.kayys.wayang.communication.capability.CapabilityId;
import tech.kayys.wayang.communication.core.DefaultAgentCommunicator;
import tech.kayys.wayang.communication.core.protocol.DefaultProtocolRegistry;
import tech.kayys.wayang.communication.core.protocol.DefaultProtocolRouter;
import tech.kayys.wayang.communication.core.protocol.DefaultProtocolSelectionPolicy;
import tech.kayys.wayang.communication.exception.AgentUnavailableException;
import tech.kayys.wayang.communication.message.MessagePayload;
import tech.kayys.wayang.communication.protocol.ProtocolContext;
import tech.kayys.wayang.communication.task.TaskStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

import static org.junit.jupiter.api.Assertions.*;

class LocalProtocolEndToEndTest {

    private LocalAgentRegistry localRegistry;
    private LocalProtocol localProtocol;
    private DefaultProtocolRegistry protocolRegistry;
    private DefaultAgentCommunicator communicator;

    @BeforeEach
    void setUp() {
        localRegistry = new DefaultLocalAgentRegistry("wayang-test-runtime");
        localProtocol = new LocalProtocol(localRegistry);

        protocolRegistry = new DefaultProtocolRegistry();
        protocolRegistry.register(localProtocol);

        var router = new DefaultProtocolRouter(protocolRegistry, new DefaultProtocolSelectionPolicy());
        communicator = new DefaultAgentCommunicator(router);
    }

    @Test
    void testEndToEndSynchronousSend() {
        // Register local agent handler
        AgentRef agentRef = localRegistry.register("procurement-agent", "Procurement Agent", request -> {
            assertEquals("procurement-agent", request.target().id());
            assertEquals("supplier.search", request.capability().value());
            assertEquals("laptop", request.payload().value());

            return CompletableFuture.completedFuture(
                    AgentResponse.success(MessagePayload.text("Found 3 suppliers"))
            );
        });

        // Caller issues AgentRequest through AgentCommunicator
        AgentRequest request = AgentRequest.of(
                agentRef,
                CapabilityId.of("supplier.search"),
                MessagePayload.text("laptop")
        );

        AgentResponse response = communicator.send(request, ProtocolContext.of("tenant-abc"))
                .toCompletableFuture()
                .join();

        assertNotNull(response);
        assertTrue(response.success());
        assertNotNull(response.payload());
        assertEquals("Found 3 suppliers", response.payload().value());
    }

    @Test
    void testEndToEndAsynchronousTaskSubmission() {
        AgentRef agentRef = localRegistry.register("analysis-agent", "Analysis Agent", request ->
                CompletableFuture.completedFuture(
                        AgentResponse.success(MessagePayload.text("Analysis complete"))
                )
        );

        AgentRequest request = new AgentRequest(
                agentRef,
                CapabilityId.of("data.analyze"),
                MessagePayload.text("data-set-1"),
                RequestMode.ASYNC,
                null,
                CommunicationOptions.automatic(),
                null
        );

        AgentTask task = communicator.submit(request, ProtocolContext.empty());
        assertNotNull(task);
        assertNotNull(task.id());

        // Listen to events
        List<AgentEvent> receivedEvents = new ArrayList<>();
        task.events().subscribe(new Flow.Subscriber<>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(AgentEvent item) {
                receivedEvents.add(item);
            }

            @Override
            public void onError(Throwable throwable) {}

            @Override
            public void onComplete() {}
        });

        AgentResult result = task.result().toCompletableFuture().join();
        assertNotNull(result);
        assertTrue(result.success());
        assertEquals("Analysis complete", result.payload().value());
        assertEquals(TaskStatus.COMPLETED, task.status());
    }

    @Test
    void testAgentUnavailableWhenNotRegistered() {
        AgentRef unregistered = AgentRef.local("ghost-agent", "Ghost", "wayang-test-runtime");
        AgentRequest request = AgentRequest.of(unregistered, CapabilityId.of("ping"), MessagePayload.text("ping"));

        CompletableFuture<AgentResponse> future = communicator.send(request, ProtocolContext.empty())
                .toCompletableFuture();

        Exception ex = assertThrows(Exception.class, future::join);
        assertTrue(ex.getCause() instanceof AgentUnavailableException || ex instanceof AgentUnavailableException);
    }

    @Test
    void testAgentHandlerErrorPropagation() {
        AgentRef errorAgent = localRegistry.register("failing-agent", request ->
                CompletableFuture.completedFuture(
                        AgentResponse.failure("BAD_REQUEST", "Invalid parameters provided")
                )
        );

        AgentRequest request = AgentRequest.of(errorAgent, CapabilityId.of("fail"), MessagePayload.text("test"));
        AgentResponse response = communicator.send(request, ProtocolContext.empty())
                .toCompletableFuture()
                .join();

        assertNotNull(response);
        assertFalse(response.success());
        assertNotNull(response.error());
        assertEquals("BAD_REQUEST", response.error().code());
        assertEquals("Invalid parameters provided", response.error().message());
    }
}
