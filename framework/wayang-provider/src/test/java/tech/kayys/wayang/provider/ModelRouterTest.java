package tech.kayys.wayang.provider;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.core.AgentDefinition;
import tech.kayys.wayang.core.ContextRequirements;
import tech.kayys.wayang.resource.Modality;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class ModelRouterTest {

    private static class MockProvider implements Provider {
        private final String id;
        private final Set<Modality> modalities;

        public MockProvider(String id, Set<Modality> modalities) {
            this.id = id;
            this.modalities = modalities;
        }

        @Override
        public String id() { return id; }

        @Override
        public void streamChat(List<ChatMessage> messages, String systemPrompt, List<ToolSpec> tools, double temperature, int maxTokens, Consumer<StreamEvent> onEvent) throws IOException, InterruptedException { }

        @Override
        public Set<Modality> supportedModalities() { return modalities; }
    }

    private static class TestRouter implements ModelRouter {
        @Override
        public Provider route(AgentRequest request, AgentDefinition agentDefinition, List<Provider> availableProviders) {
            return null; // Not testing the old overload
        }
    }

    @Test
    void testRouteSelectsProviderWithRequiredModalities() {
        Provider textOnly = new MockProvider("text-provider", Set.of(Modality.TEXT));
        Provider visionCapable = new MockProvider("vision-provider", Set.of(Modality.TEXT, Modality.IMAGE));
        
        TestRouter router = new TestRouter();

        ContextRequirements reqs = new ContextRequirements(Set.of(Modality.TEXT, Modality.IMAGE), false, 1000);
        Provider selected = router.route(reqs, List.of(textOnly, visionCapable));
        
        assertEquals("vision-provider", selected.id());
    }

    @Test
    void testRouteThrowsIfNoCapableProvider() {
        Provider textOnly = new MockProvider("text-provider", Set.of(Modality.TEXT));
        
        TestRouter router = new TestRouter();

        ContextRequirements reqs = new ContextRequirements(Set.of(Modality.TEXT, Modality.AUDIO), false, 1000);
        
        assertThrows(IllegalStateException.class, () -> {
            router.route(reqs, List.of(textOnly));
        });
    }
}
