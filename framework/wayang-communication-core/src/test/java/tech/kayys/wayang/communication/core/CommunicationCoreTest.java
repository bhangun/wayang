package tech.kayys.wayang.communication.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.communication.api.*;
import tech.kayys.wayang.communication.capability.CapabilityId;
import tech.kayys.wayang.communication.capability.ProtocolCapability;
import tech.kayys.wayang.communication.core.protocol.*;
import tech.kayys.wayang.communication.endpoint.AgentEndpoint;
import tech.kayys.wayang.communication.endpoint.LocalAgentEndpoint;
import tech.kayys.wayang.communication.endpoint.RemoteAgentEndpoint;
import tech.kayys.wayang.communication.exception.NoCompatibleProtocolException;
import tech.kayys.wayang.communication.message.MessagePayload;
import tech.kayys.wayang.communication.protocol.*;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

import static org.junit.jupiter.api.Assertions.*;

class CommunicationCoreTest {

    private DefaultProtocolRegistry registry;
    private DefaultProtocolSelectionPolicy policy;
    private DefaultProtocolRouter router;
    private DefaultAgentCommunicator communicator;

    @BeforeEach
    void setUp() {
        registry = new DefaultProtocolRegistry();
        policy = new DefaultProtocolSelectionPolicy();
        router = new DefaultProtocolRouter(registry, policy);
        communicator = new DefaultAgentCommunicator(router);
    }

    // -------------------------------------------------------------------------
    // 1. Registry Tests
    // -------------------------------------------------------------------------

    @Test
    void testProtocolRegistry() {
        AgentProtocol dummyProtocol = new DummyProtocol(WellKnownProtocols.IN_PROCESS, Set.of(ProtocolCapability.REQUEST_RESPONSE), true);

        assertFalse(registry.contains(WellKnownProtocols.IN_PROCESS));
        assertTrue(registry.find(WellKnownProtocols.IN_PROCESS).isEmpty());

        registry.register(dummyProtocol);
        assertTrue(registry.contains(WellKnownProtocols.IN_PROCESS));
        assertTrue(registry.find(WellKnownProtocols.IN_PROCESS).isPresent());
        assertEquals(1, registry.protocols().size());

        registry.unregister(WellKnownProtocols.IN_PROCESS);
        assertFalse(registry.contains(WellKnownProtocols.IN_PROCESS));
    }

    // -------------------------------------------------------------------------
    // 2. Selection Policy & Router Tests
    // -------------------------------------------------------------------------

    @Test
    void testSelectionPolicyRanking() {
        AgentProtocol localProto = new DummyProtocol(WellKnownProtocols.IN_PROCESS, Set.of(ProtocolCapability.REQUEST_RESPONSE, ProtocolCapability.ASYNC_TASK), true);
        AgentProtocol remoteProto = new DummyProtocol(WellKnownProtocols.A2A, Set.of(ProtocolCapability.REQUEST_RESPONSE), true);

        registry.register(localProto);
        registry.register(remoteProto);

        AgentRef localTarget = new AgentRef("agent-1", "Agent 1", LocalAgentEndpoint.of("rt-1"));
        AgentRequest localRequest = AgentRequest.of(localTarget, CapabilityId.of("cap.test"), MessagePayload.text("hello"));

        List<ProtocolCandidate> localCandidates = router.candidates(localRequest, ProtocolContext.empty());
        assertFalse(localCandidates.isEmpty());
        // Local in-process protocol gets score 100 + 10 (auto request-response) = 110
        assertEquals(WellKnownProtocols.IN_PROCESS, localCandidates.get(0).protocol().id());

        // Preferred protocols override default score
        AgentRequest preferredRequest = new AgentRequest(
                localTarget,
                CapabilityId.of("cap.test"),
                MessagePayload.text("hello"),
                RequestMode.AUTO,
                null,
                CommunicationOptions.prefer(WellKnownProtocols.A2A),
                null
        );

        List<ProtocolCandidate> prefCandidates = router.candidates(preferredRequest, ProtocolContext.empty());
        assertEquals(WellKnownProtocols.A2A, prefCandidates.get(0).protocol().id());
    }

    @Test
    void testStrictOptionsDisallowsFallback() {
        AgentProtocol localProto = new DummyProtocol(WellKnownProtocols.IN_PROCESS, Set.of(ProtocolCapability.REQUEST_RESPONSE), true);
        registry.register(localProto);

        AgentRef localTarget = new AgentRef("agent-1", "Agent 1", LocalAgentEndpoint.of("rt-1"));
        AgentRequest strictRequest = new AgentRequest(
                localTarget,
                CapabilityId.of("cap.test"),
                MessagePayload.text("hello"),
                RequestMode.AUTO,
                null,
                CommunicationOptions.strict(WellKnownProtocols.ANP),
                null
        );

        List<ProtocolCandidate> candidates = router.candidates(strictRequest, ProtocolContext.empty());
        assertTrue(candidates.isEmpty(), "ANP not registered and fallback disallowed; candidates should be empty");
    }

    @Test
    void testRouterThrowsWhenNoProtocolMatches() {
        AgentRef target = new AgentRef("agent-1", "Agent 1", RemoteAgentEndpoint.of(URI.create("https://example.com")));
        AgentRequest request = AgentRequest.of(target, CapabilityId.of("cap.test"), MessagePayload.text("hello"));

        assertThrows(NoCompatibleProtocolException.class, () -> router.route(request, ProtocolContext.empty()));
    }

    // -------------------------------------------------------------------------
    // Dummy Protocol for testing
    // -------------------------------------------------------------------------

    private static class DummyProtocol implements AgentProtocol {
        private final ProtocolId id;
        private final Set<ProtocolCapability> capabilities;
        private final boolean supportsAll;

        public DummyProtocol(ProtocolId id, Set<ProtocolCapability> capabilities, boolean supportsAll) {
            this.id = id;
            this.capabilities = capabilities;
            this.supportsAll = supportsAll;
        }

        @Override
        public ProtocolId id() { return id; }

        @Override
        public ProtocolVersion version() { return new ProtocolVersion(1, 0); }

        @Override
        public Set<ProtocolCapability> capabilities() { return capabilities; }

        @Override
        public boolean supports(AgentEndpoint endpoint, AgentRequest request) { return supportsAll; }

        @Override
        public ProtocolClient client(ProtocolContext context) {
            return new ProtocolClient() {
                @Override
                public CompletionStage<AgentResponse> send(AgentRequest request, ProtocolContext context) {
                    return CompletableFuture.completedFuture(AgentResponse.success(MessagePayload.text("dummy-ok")));
                }

                @Override
                public AgentTask submit(AgentRequest request, ProtocolContext context) {
                    return null;
                }

                @Override
                public Flow.Publisher<AgentEvent> stream(AgentRequest request, ProtocolContext context) {
                    return null;
                }
            };
        }
    }
}
