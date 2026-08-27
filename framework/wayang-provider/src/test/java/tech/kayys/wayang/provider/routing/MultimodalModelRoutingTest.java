package tech.kayys.wayang.provider.routing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.provider.*;
import tech.kayys.wayang.resource.Modality;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class MultimodalModelRoutingTest {

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
    private DefaultModelRegistry modelRegistry;
    private DefaultModelDeploymentRegistry deploymentRegistry;
    private ModelRoutingTelemetry telemetry;

    private Provider openaiProvider;
    private Provider anthropicProvider;
    private Provider googleProvider;
    private Provider groqProvider;
    private Provider gollekProvider;
    private Provider tripoProvider;
    private Provider elevenLabsProvider;
    private List<Provider> availableProviders;

    @BeforeEach
    void setUp() {
        modelRegistry = new DefaultModelRegistry();
        deploymentRegistry = new DefaultModelDeploymentRegistry();
        telemetry = ModelRoutingTelemetry.getInstance();
        telemetry.reset();

        router = new AdaptiveModelRouter(
                modelRegistry,
                deploymentRegistry,
                new ConstraintEvaluator(),
                new DefaultModelScorer(),
                telemetry,
                new FallbackPlanner()
        );

        openaiProvider = new MockProvider("openai", Set.of(Modality.TEXT, Modality.IMAGE));
        anthropicProvider = new MockProvider("anthropic", Set.of(Modality.TEXT, Modality.IMAGE));
        googleProvider = new MockProvider("google", Set.of(Modality.TEXT, Modality.IMAGE, Modality.AUDIO, Modality.PDF));
        groqProvider = new MockProvider("groq", Set.of(Modality.AUDIO, Modality.TEXT));
        gollekProvider = new MockProvider("gollek", Set.of(Modality.TEXT, Modality.IMAGE, Modality.AUDIO, Modality.THREE_D, Modality.PDF));
        tripoProvider = new MockProvider("tripo", Set.of(Modality.TEXT, Modality.IMAGE, Modality.THREE_D));
        elevenLabsProvider = new MockProvider("elevenlabs", Set.of(Modality.TEXT, Modality.AUDIO));

        availableProviders = List.of(
                openaiProvider, anthropicProvider, googleProvider,
                groqProvider, gollekProvider, tripoProvider, elevenLabsProvider
        );
    }

    @Test
    void test3DGenerationRequirementSelects3DModel() {
        InferenceRequirements reqs = InferenceRequirements.multimodal(
                Set.of(Modality.IMAGE),
                Set.of(Modality.THREE_D),
                Set.of(ModelCapability.THREE_D_GENERATION)
        );

        InferencePlan plan = router.plan(null, null, reqs, InferencePolicy.balanced(), availableProviders);

        assertNotNull(plan);
        assertTrue(plan.selectedModel().contains("trellis") || plan.selectedModel().contains("shap-e") || plan.selectedModel().contains("tripo"));
        assertTrue(plan.requirements().requires3D());
        assertFalse(plan.rejectedCandidates().isEmpty());
        // Text-only LLMs should be rejected
        assertTrue(plan.rejectedCandidates().containsKey("gpt-4o"));
    }

    @Test
    void testSpeechToTextRequirementSelectsWhisper() {
        InferenceRequirements reqs = InferenceRequirements.multimodal(
                Set.of(Modality.AUDIO),
                Set.of(Modality.TEXT),
                Set.of(ModelCapability.SPEECH_TO_TEXT)
        );

        InferencePlan plan = router.plan(null, null, reqs, InferencePolicy.balanced(), availableProviders);

        assertNotNull(plan);
        assertTrue(plan.selectedModel().contains("whisper"));
        assertTrue(plan.rejectedCandidates().containsKey("claude-3-5-sonnet-20241022"));
    }

    @Test
    void testTextToSpeechRequirementSelectsTTSModel() {
        InferenceRequirements reqs = InferenceRequirements.multimodal(
                Set.of(Modality.TEXT),
                Set.of(Modality.AUDIO),
                Set.of(ModelCapability.TEXT_TO_SPEECH)
        );

        InferencePlan plan = router.plan(null, null, reqs, InferencePolicy.balanced(), availableProviders);

        assertNotNull(plan);
        assertEquals("elevenlabs-tts", plan.selectedModel());
    }

    @Test
    void testOCRRequirementSelectsOCRModel() {
        InferenceRequirements reqs = InferenceRequirements.multimodal(
                Set.of(Modality.PDF),
                Set.of(Modality.TEXT),
                Set.of(ModelCapability.OCR)
        );

        InferencePlan plan = router.plan(null, null, reqs, InferencePolicy.balanced(), availableProviders);

        assertNotNull(plan);
        assertTrue(plan.selectedModel().contains("nougat") || plan.selectedModel().contains("florence"));
    }

    @Test
    void testImageGenerationRequirementSelectsDiffusionModel() {
        InferenceRequirements reqs = InferenceRequirements.multimodal(
                Set.of(Modality.TEXT),
                Set.of(Modality.IMAGE),
                Set.of(ModelCapability.IMAGE_GENERATION)
        );

        InferencePlan plan = router.plan(null, null, reqs, InferencePolicy.balanced(), availableProviders);

        assertNotNull(plan);
        assertTrue(plan.selectedModel().contains("flux") || plan.selectedModel().contains("dall-e"));
    }

    @Test
    void testDeploymentRegistryOverridesProviderResolution() {
        // Register an explicit local deployment for gpt-4o via a mock provider
        Provider customLocalProvider = new MockProvider("custom-local", Set.of(Modality.TEXT, Modality.IMAGE));
        deploymentRegistry.register(new ModelDeployment(
                "dep-gpt-4o-custom",
                "gpt-4o",
                "custom-local",
                customLocalProvider,
                DeploymentType.LOCAL,
                "http://localhost:8080",
                ModelStatus.AVAILABLE,
                java.util.Map.of(),
                java.time.Instant.now()
        ));

        InferenceRequirements reqs = InferenceRequirements.of(Set.of(Modality.TEXT), true, 8192);
        InferencePolicy policy = InferencePolicy.thorough(); // QUALITY_FIRST (selects gpt-4o or claude)

        InferencePlan plan = router.plan(null, null, reqs, policy, availableProviders);

        assertNotNull(plan);
        if ("gpt-4o".equals(plan.selectedModel())) {
            assertEquals("custom-local", plan.selectedProvider().id());
        }
    }
}
