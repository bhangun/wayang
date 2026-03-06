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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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
            String preferredProvider = resolvePreferredProvider(request);

            // Set preferred provider if specified
            if (preferredProvider != null && !preferredProvider.isBlank()) {
                gollekClient.setPreferredProvider(preferredProvider);
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
                    .agentId(request.getAgentId())
                    .useMemory(request.getUseMemory())
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

        InferenceRequest.Builder builder = InferenceRequest.builder()
                .messages(messages)
                .model(request.getModel())
                .temperature(request.getTemperature())
                .maxTokens(request.getMaxTokens().intValue())
                .streaming(request.getStream() != null ? request.getStream() : false);

        String preferredProvider = resolvePreferredProvider(request);
        if (preferredProvider != null && !preferredProvider.isBlank()) {
            builder.preferredProvider(preferredProvider);
        }

        resolveApiKey(request, preferredProvider).ifPresent(builder::apiKey);
        Map<String, Object> metadata = buildRequestMetadata(request, preferredProvider);
        if (!metadata.isEmpty()) {
            builder.metadata(metadata);
        }

        return builder.build();
    }

    private String resolvePreferredProvider(AgentInferenceRequest request) {
        if (request.getPreferredProvider() != null && !request.getPreferredProvider().isBlank()) {
            return request.getPreferredProvider();
        }

        Map<String, Object> context = requestContextMap(request);
        if (context.isEmpty()) {
            return null;
        }

        String providerMode = stringValue(context.get("providerMode"));
        String directPreferred = firstNonBlank(
                stringValue(context.get("preferredProvider")),
                stringValue(context.get("provider")));
        if (directPreferred != null) {
            return directPreferred;
        }

        Map<String, Object> cloudProvider = mapValue(context.get("cloudProvider"));
        Map<String, Object> localProvider = mapValue(context.get("localProvider"));

        if ("cloud".equalsIgnoreCase(providerMode)) {
            return firstNonBlank(
                    stringValue(cloudProvider.get("providerId")),
                    stringValue(context.get("fallbackProvider")),
                    stringValue(localProvider.get("providerId")));
        }
        if ("local".equalsIgnoreCase(providerMode)) {
            return firstNonBlank(
                    stringValue(localProvider.get("providerId")),
                    stringValue(context.get("fallbackProvider")),
                    stringValue(cloudProvider.get("providerId")));
        }

        return firstNonBlank(
                stringValue(cloudProvider.get("providerId")),
                stringValue(localProvider.get("providerId")),
                stringValue(context.get("fallbackProvider")));
    }

    private Optional<String> resolveApiKey(AgentInferenceRequest request, String providerId) {
        Map<String, String> credentials = resolvedCredentialMap(request);
        if (credentials.isEmpty()) {
            return Optional.empty();
        }

        String providerHint = providerHint(providerId);
        if (!providerHint.isBlank()) {
            for (Map.Entry<String, String> entry : credentials.entrySet()) {
                String key = entry.getKey().toLowerCase(Locale.ROOT);
                if (key.contains(providerHint) && looksLikeSecretKeyName(key)) {
                    return Optional.of(entry.getValue());
                }
            }
        }

        for (Map.Entry<String, String> entry : credentials.entrySet()) {
            if (looksLikeSecretKeyName(entry.getKey().toLowerCase(Locale.ROOT))) {
                return Optional.of(entry.getValue());
            }
        }

        if (credentials.size() == 1) {
            return Optional.of(credentials.values().iterator().next());
        }

        return Optional.empty();
    }

    private Map<String, Object> buildRequestMetadata(AgentInferenceRequest request, String preferredProvider) {
        Map<String, Object> context = requestContextMap(request);
        if (context.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        String providerMode = stringValue(context.get("providerMode"));
        if (providerMode != null) {
            metadata.put("providerMode", providerMode);
        }
        if (preferredProvider != null && !preferredProvider.isBlank()) {
            metadata.put("preferredProvider", preferredProvider);
        }
        Map<String, String> credentials = resolvedCredentialMap(request);
        if (!credentials.isEmpty()) {
            metadata.put("resolvedCredentialNames", credentials.keySet().stream().sorted().toList());
        }
        return metadata;
    }

    private Map<String, Object> requestContextMap(AgentInferenceRequest request) {
        Map<String, Object> additional = request.getAdditionalParams();
        if (additional == null || additional.isEmpty()) {
            return Map.of();
        }
        Object context = additional.get("context");
        if (context instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((k, v) -> result.put(String.valueOf(k), v));
            return result;
        }
        return Collections.emptyMap();
    }

    private Map<String, String> resolvedCredentialMap(AgentInferenceRequest request) {
        Map<String, String> resolved = new LinkedHashMap<>();
        Map<String, Object> additional = request.getAdditionalParams();
        if (additional != null) {
            mergeCredentialMap(resolved, additional.get("_resolvedCredentials"));
        }
        Map<String, Object> context = requestContextMap(request);
        if (!context.isEmpty()) {
            mergeCredentialMap(resolved, context.get("_resolvedCredentials"));
        }
        return resolved;
    }

    @SuppressWarnings("unchecked")
    private static void mergeCredentialMap(Map<String, String> target, Object raw) {
        if (!(raw instanceof Map<?, ?> map)) {
            return;
        }
        map.forEach((k, v) -> {
            if (k == null || v == null) {
                return;
            }
            String key = String.valueOf(k).trim();
            String value = String.valueOf(v).trim();
            if (!key.isEmpty() && !value.isEmpty()) {
                target.putIfAbsent(key, value);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapValue(Object raw) {
        if (!(raw instanceof Map<?, ?> map)) {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        map.forEach((k, v) -> result.put(String.valueOf(k), v));
        return result;
    }

    private static String stringValue(Object raw) {
        if (raw == null) {
            return null;
        }
        String value = String.valueOf(raw).trim();
        return value.isEmpty() ? null : value;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static boolean looksLikeSecretKeyName(String key) {
        String normalized = key.toLowerCase(Locale.ROOT);
        return normalized.contains("apikey")
                || normalized.contains("api-key")
                || normalized.contains("api_key")
                || normalized.contains("token")
                || normalized.endsWith("key");
    }

    private static String providerHint(String providerId) {
        if (providerId == null || providerId.isBlank()) {
            return "";
        }
        String normalized = providerId.toLowerCase(Locale.ROOT);
        if (normalized.contains("openai")) return "openai";
        if (normalized.contains("anthropic")) return "anthropic";
        if (normalized.contains("gemini") || normalized.contains("google")) return "gemini";
        if (normalized.contains("mistral")) return "mistral";
        if (normalized.contains("cerebras")) return "cerebras";
        if (normalized.contains("azure")) return "azure";
        if (normalized.contains("ollama")) return "ollama";
        return normalized;
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
