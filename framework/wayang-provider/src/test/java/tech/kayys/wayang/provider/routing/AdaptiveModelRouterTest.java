package tech.kayys.wayang.provider.routing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.provider.*;
import tech.kayys.wayang.resource.Modality;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class AdaptiveModelRouterTest {

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

    private AdaptiveModelRouter router;
    private DefaultModelRegistry registry;
    private ModelRoutingTelemetry telemetry;

    private Provider openaiProvider;
    private Provider anthropicProvider;
    private Provider googleProvider;
    private Provider gollekProvider;
    private List<Provider> availableProviders;

    @BeforeEach
    void setUp() {
        registry = new DefaultModelRegistry();
        telemetry = ModelRoutingTelemetry.getInstance();
        telemetry.reset();
        router = new AdaptiveModelRouter(registry, new DefaultModelScorer(), telemetry);

        openaiProvider = new MockProvider("openai", Set.of(Modality.TEXT, Modality.IMAGE));
        anthropicProvider = new MockProvider("anthropic", Set.of(Modality.TEXT, Modality.IMAGE));
        googleProvider = new MockProvider("google", Set.of(Modality.TEXT, Modality.IMAGE, Modality.AUDIO));
        gollekProvider = new MockProvider("gollek", Set.of(Modality.TEXT));

        availableProviders = List.of(openaiProvider, anthropicProvider, googleProvider, gollekProvider);
    }

    @Test
    void testQualityFirstObjectiveSelectsFlagshipModel() {
        InferenceRequirements reqs = InferenceRequirements.defaults();
        InferencePolicy policy = InferencePolicy.thorough(); // QUALITY_FIRST

        InferencePlan plan = router.plan(null, null, reqs, policy, availableProviders);

        assertNotNull(plan);
        assertNotNull(plan.selectedModel());
        assertTrue(plan.selectedModel().contains("claude-3-5-sonnet") || plan.selectedModel().contains("gpt-4o") || plan.selectedModel().contains("o1"));
        assertNotNull(plan.selectedProvider());
        assertFalse(plan.fallbackTargets().isEmpty());
        assertTrue(plan.decisionReason().contains("QUALITY_FIRST") || plan.decisionReason().contains("Selected"));
    }

    @Test
    void testCostMinimizingObjectiveSelectsBudgetModel() {
        InferenceRequirements reqs = InferenceRequirements.defaults();
        InferencePolicy policy = new InferencePolicy(
                InferencePolicy.RoutingObjective.COST_MINIMIZING,
                null,
                Set.of(), Set.of(), Set.of(), Set.of(),
                InferencePolicy.FallbackStrategy.ORDERED_FALLBACK
        );

        InferencePlan plan = router.plan(null, null, reqs, policy, availableProviders);

        assertNotNull(plan);
        // Under cost minimizing, should pick cheap model like gemini-1.5-flash or gpt-4o-mini or local gollek
        assertTrue(plan.selectedModel().contains("flash") || plan.selectedModel().contains("mini") || plan.selectedModel().contains("local"));
        assertTrue(plan.estimatedCost() <= 0.05);
    }

    @Test
    void testLocalFirstObjectivePrefersLocalGollekModel() {
        InferenceRequirements reqs = InferenceRequirements.defaults();
        InferencePolicy policy = InferencePolicy.debug(); // LOCAL_FIRST

        InferencePlan plan = router.plan(null, null, reqs, policy, availableProviders);

        assertNotNull(plan);
        assertTrue(plan.selectedModel().contains("local") || plan.selectedModel().contains("gollek"));
        assertEquals("gollek", plan.selectedProvider().id());
        assertEquals(0.0, plan.estimatedCost());
    }

    @Test
    void testToolCallingRequirementFiltersOutNonToolCallingModels() {
        // Register a dummy model without tool calling
        registry.registerModel(new ModelSpec(
                "no-tools-cheap-model", "test-prov", 32_000, 4096,
                false, false, Set.of(Modality.TEXT),
                0.01, 0.01, 100, 0.99, false
        ));

        InferenceRequirements reqs = InferenceRequirements.of(Set.of(Modality.TEXT), true, 4096);
        InferencePlan plan = router.plan(null, null, reqs, InferencePolicy.balanced(), availableProviders);

        assertNotEquals("no-tools-cheap-model", plan.selectedModel());
        assertTrue(plan.rejectedCandidates().containsKey("no-tools-cheap-model"));
        assertTrue(plan.rejectedCandidates().get("no-tools-cheap-model").toLowerCase().contains("tool"));
    }

    @Test
    void testReasoningRequirementFiltersOutNonReasoningModels() {
        InferenceRequirements reqs = InferenceRequirements.of(Set.of(Modality.TEXT), false, true, 8192);
        InferencePlan plan = router.plan(null, null, reqs, InferencePolicy.thorough(), availableProviders);

        assertNotNull(plan.selectedModel());
        assertTrue(plan.selectedModel().contains("o1") || plan.selectedModel().contains("deepseek-r1") || plan.selectedModel().contains("o3-mini") || plan.selectedModel().contains("gemini-2.0-flash") || plan.selectedModel().contains("local"));
    }

    @Test
    void testContextWindowRequirementFiltersOutSmallContextModels() {
        InferenceRequirements reqs = InferenceRequirements.of(Set.of(Modality.TEXT), false, false, 500_000); // 500k context needed
        InferencePlan plan = router.plan(null, null, reqs, InferencePolicy.balanced(), availableProviders);

        // Only Gemini 1.5 Pro / Flash support >= 1M context
        assertTrue(plan.selectedModel().contains("gemini"));
        assertEquals("google", plan.selectedProvider().id());
        assertTrue(plan.rejectedCandidates().containsKey("gpt-4o"));
    }

    @Test
    void testTelemetryHealthPenaltyAdaptsRouting() {
        // Under BALANCED objective, gpt-4o or claude is usually top tier.
        // Let's degrade gpt-4o heavily in telemetry:
        for (int i = 0; i < 10; i++) {
            telemetry.recordFailure("gpt-4o", 5000);
        }

        InferenceRequirements reqs = InferenceRequirements.defaults();
        InferencePlan plan = router.plan(null, null, reqs, InferencePolicy.thorough(), availableProviders);

        // gpt-4o should be penalized and not selected as primary
        assertNotEquals("gpt-4o", plan.selectedModel());
    }
}
