package tech.kayys.wayang.agent.core.inference;

import io.smallrye.mutiny.Uni;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.kayys.gollek.sdk.local.GollekLocalClient;
import tech.kayys.gollek.sdk.local.GollekLocalClientException;
import tech.kayys.gollek.spi.Message;
import tech.kayys.gollek.spi.inference.InferenceRequest;
import tech.kayys.gollek.spi.inference.InferenceResponse;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared inference service for all Wayang agents.
 * Provides a simplified interface to the Gollek SDK for inference requests.
 * 
 * <p>
 * This service is injected into agent executors via CDI and handles:
 * <ul>
 * <li>Provider selection and fallback</li>
 * <li>Request/response mapping</li>
 * <li>Error handling and retry logic</li>
 * <li>Performance tracking</li>
 * </ul>
 */
public class GollekInferenceService {
    private static final Logger log = LoggerFactory.getLogger(GollekInferenceService.class);

    @Inject
    GollekLocalClient gollekClient;

    @Inject
    tech.kayys.wayang.agent.core.memory.AgentMemoryService memoryService;

    @Inject
    tech.kayys.wayang.agent.core.tool.ToolRegistry toolRegistry;

    /**
     * Perform synchronous inference.
     *
     * @param request Agent inference request
     * @return Agent inference response
     */
    public AgentInferenceResponse infer(AgentInferenceRequest request) {
        Instant start = Instant.now();

        try {
            // Set preferred provider if specified
            if (request.getPreferredProvider() != null) {
                gollekClient.setPreferredProvider(request.getPreferredProvider());
            }

            // 1. Inject Memory Context if enabled
            String systemPromptWithMemory = request.getSystemPrompt();
            if (Boolean.TRUE.equals(request.getUseMemory()) && request.getAgentId() != null) {
                String memoryContext = memoryService.retrieveContext(request.getAgentId(), request.getUserPrompt(), 5)
                        .await().indefinitely();
                if (memoryContext != null && !memoryContext.isBlank()) {
                    systemPromptWithMemory = (systemPromptWithMemory != null ? systemPromptWithMemory : "")
                            + "\n\nRelevant Context:\n" + memoryContext;
                    log.debug("Injected memory context for agent {}", request.getAgentId());
                }
            }

            // 2. Prepare Tools
            // TODO: If SDK supports native tools, pass them here.
            // For now, we assume simple inference or "ReAct" prompting handled by the
            // agent.
            // Future: map request.getTools() to Gollek tool definitions.

            // 3. Build Gollek inference request
            InferenceRequest gollekRequest = buildGollekRequest(request, systemPromptWithMemory);

            // 4. Execut Inference
            InferenceResponse gollekResponse = gollekClient.createCompletion(gollekRequest);

            // 5. Calculate latency
            Duration latency = Duration.between(start, Instant.now());

            // 6. Map to agent response
            AgentInferenceResponse response = mapToAgentResponse(gollekResponse, latency);

            // 7. Store interaction in memory if enabled
            if (Boolean.TRUE.equals(request.getUseMemory()) && request.getAgentId() != null) {
                memoryService.storeMemory(request.getAgentId(),
                        "User: " + request.getUserPrompt() + "\nAssistant: " + gollekResponse.getContent(),
                        null).subscribe().with(id -> log.debug("Stored interaction memory: {}", id));
            }

            return response;

        } catch (GollekLocalClientException e) {
            log.error("Inference failed: {}", e.getMessage(), e);
            Duration latency = Duration.between(start, Instant.now());

            return AgentInferenceResponse.builder()
                    .error(e.getMessage())
                    .latency(latency)
                    .build();
        }
    }

    /**
     * Perform asynchronous inference using Mutiny Uni.
     *
     * @param request Agent inference request
     * @return Uni containing agent inference response
     */
    public Uni<AgentInferenceResponse> inferAsync(AgentInferenceRequest request) {
        return Uni.createFrom().item(() -> infer(request));
    }

    /**
     * Perform inference with automatic fallback to alternate provider.
     *
     * @param request          Agent inference request
     * @param fallbackProvider Fallback provider ID if primary fails
     * @return Agent inference response
     */
    public AgentInferenceResponse inferWithFallback(
            AgentInferenceRequest request,
            String fallbackProvider) {

        // Try primary provider
        AgentInferenceResponse response = infer(request);

        // If failed and fallback is specified, try fallback
        if (response.isError() && fallbackProvider != null) {
            log.warn("Primary inference failed, trying fallback provider: {}", fallbackProvider);

            AgentInferenceRequest fallbackRequest = AgentInferenceRequest.builder()
                    .systemPrompt(request.getSystemPrompt())
                    .userPrompt(request.getUserPrompt())
                    .preferredProvider(fallbackProvider)
                    .temperature(request.getTemperature())
                    .maxTokens(request.getMaxTokens())
                    .model(request.getModel())
                    .additionalParams(request.getAdditionalParams())
                    .stream(request.getStream() != null ? request.getStream() : false)
                    .build();

            response = infer(fallbackRequest);
        }

        return response;
    }

    /**
     * Build Gollek inference request from agent request.
     */
    private InferenceRequest buildGollekRequest(AgentInferenceRequest request, String overrideSystemPrompt) {
        List<Message> messages = new ArrayList<>();

        // Use override prompt if available, otherwise original
        String finalSystemPrompt = overrideSystemPrompt != null ? overrideSystemPrompt : request.getSystemPrompt();

        // Add system message if provided
        if (finalSystemPrompt != null && !finalSystemPrompt.isBlank()) {
            messages.add(Message.system(finalSystemPrompt));
        }

        // Add user message
        messages.add(Message.user(request.getUserPrompt()));

        return InferenceRequest.builder()
                .messages(messages)
                .model(request.getModel())
                .temperature(request.getTemperature())
                .maxTokens(request.getMaxTokens().intValue())
                .streaming(request.getStream() != null ? request.getStream() : false)
                .build();
    }

    /**
     * Map Gollek inference response to agent response.
     */
    private AgentInferenceResponse mapToAgentResponse(
            tech.kayys.gollek.spi.inference.InferenceResponse gollekResponse,
            Duration latency) {

        return AgentInferenceResponse.builder()
                .content(gollekResponse.getContent())
                .providerUsed(null) // Provider not in SPI response
                .modelUsed(gollekResponse.getModel())
                .promptTokens(gollekResponse.getInputTokens())
                .completionTokens(gollekResponse.getOutputTokens())
                .totalTokens(gollekResponse.getTokensUsed())
                .latency(latency)
                .cached(false) // Cached status not in SPI response
                .build();
    }

    /**
     * List all available providers.
     *
     * @return List of provider IDs
     */
    public List<String> listAvailableProviders() {
        try {
            return gollekClient.listAvailableProviders().stream()
                    .map(provider -> provider.id())
                    .toList();
        } catch (GollekLocalClientException e) {
            log.error("Failed to list providers: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get information about a specific provider.
     *
     * @param providerId Provider ID
     * @return Provider information, or null if not found
     */
    public tech.kayys.gollek.spi.provider.ProviderInfo getProviderInfo(String providerId) {
        try {
            return gollekClient.getProviderInfo(providerId);
        } catch (GollekLocalClientException e) {
            log.error("Failed to get provider info for {}: {}", providerId, e.getMessage(), e);
            return null;
        }
    }
}
