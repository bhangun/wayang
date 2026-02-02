package main.java.tech.kayys.wayang.rag;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.output.Response;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.gamelan.core.domain.ErrorInfo;
import tech.kayys.gamelan.core.engine.NodeExecutionResult;
import tech.kayys.gamelan.core.engine.NodeExecutionTask;
import tech.kayys.gamelan.executor.Executor;
import tech.kayys.gamelan.executor.AbstractWorkflowExecutor;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * RAG RESPONSE GENERATION EXECUTOR - FULL IMPLEMENTATION
 */
@Executor(executorType = "rag-response-generation", communicationType = tech.kayys.gamelan.core.scheduler.CommunicationType.GRPC, maxConcurrentTasks = 15, supportedNodeTypes = {
        "TASK", "RAG_GENERATION" }, version = "1.0.0")
@ApplicationScoped
public class ResponseGenerationExecutor extends AbstractWorkflowExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseGenerationExecutor.class);

    @Inject
    ChatModelFactory modelFactory;
    @Inject
    PromptTemplateService promptTemplateService;
    @Inject
    CitationService citationService;
    @Inject
    ResponseGuardrailEngine guardrailEngine;
    @Inject
    ResponseCacheService cacheService;
    @Inject
    GenerationMetricsCollector metricsCollector;

    @ConfigProperty(name = "gamelan.rag.generation.provider", defaultValue = "openai")
    String defaultProvider;

    @ConfigProperty(name = "gamelan.rag.generation.model", defaultValue = "gpt-4-turbo")
    String defaultModel;

    @ConfigProperty(name = "gamelan.rag.generation.temperature", defaultValue = "0.7")
    double defaultTemperature;

    @ConfigProperty(name = "gamelan.rag.generation.max-tokens", defaultValue = "1000")
    int defaultMaxTokens;

    @ConfigProperty(name = "gamelan.rag.generation.include-citations", defaultValue = "true")
    boolean defaultIncludeCitations;

    @ConfigProperty(name = "gamelan.rag.generation.use-cache", defaultValue = "true")
    boolean defaultUseCache;

    @ConfigProperty(name = "gamelan.rag.generation.timeout", defaultValue = "60")
    int timeoutSeconds;

    @Override
    public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
        LOG.info("Starting response generation for run: {}, node: {}",
                task.runId().value(), task.nodeId().value());

        Instant startTime = Instant.now();
        Map<String, Object> context = task.context();

        GenerationConfig config = extractConfiguration(context);

        return validateConfiguration(config)
                .flatMap(valid -> {
                    if (!valid) {
                        return Uni.createFrom().item(NodeExecutionResult.failure(
                                task.runId(), task.nodeId(), task.attempt(),
                                new ErrorInfo("INVALID_CONFIGURATION",
                                        "Invalid generation configuration", "", Map.of()),
                                task.token()));
                    }

                    // Check cache
                    if (config.useCache()) {
                        String cacheKey = generateCacheKey(config);
                        String cachedResponse = cacheService.get(cacheKey);

                        if (cachedResponse != null) {
                            LOG.info("Cache hit for query: {}", config.query());
                            return Uni.createFrom().item(NodeExecutionResult.success(
                                    task.runId(), task.nodeId(), task.attempt(),
                                    Map.of("response", cachedResponse, "cached", true,
                                            "query", config.query()),
                                    task.token()));
                        }
                    }

                    return generateResponse(config, task.runId().value())
                            .map(result -> {
                                long durationMs = Duration.between(startTime, Instant.now()).toMillis();

                                metricsCollector.recordGeneration(
                                        task.runId().value(), result.tokensUsed(), durationMs);

                                if (config.useCache()) {
                                    cacheService.put(generateCacheKey(config), result.response());
                                }

                                return NodeExecutionResult.success(
                                        task.runId(), task.nodeId(), task.attempt(),
                                        Map.of(
                                                "response", result.response(),
                                                "citations", result.citations(),
                                                "tokensUsed", result.tokensUsed(),
                                                "durationMs", durationMs,
                                                "model", config.model(),
                                                "cached", false,
                                                "query", config.query()),
                                        task.token());
                            })
                            .onFailure().recoverWithItem(error -> {
                                LOG.error("Response generation failed", error);
                                return NodeExecutionResult.failure(
                                        task.runId(), task.nodeId(), task.attempt(),
                                        ErrorInfo.of(error), task.token());
                            });
                });
    }

    private Uni<GenerationResult> generateResponse(GenerationConfig config, String workflowRunId) {
        LOG.debug("Generating response for query: '{}' using model: {}",
                config.query(), config.model());

        return Uni.createFrom().item(() -> {
            ChatLanguageModel chatModel = modelFactory.createModel(
                    config.provider(), config.model(), config.apiKey(),
                    config.temperature(), config.maxTokens());

            List<ChatMessage> messages = buildMessages(config);

            Response<AiMessage> response = chatModel.generate(messages);
            String responseText = response.content().text();

            responseText = guardrailEngine.validateAndSanitize(responseText, config);

            List<Citation> citations = Collections.emptyList();
            if (config.includeCitations() && !config.contexts().isEmpty()) {
                citations = citationService.generateCitations(
                        responseText, config.contexts(), config.contextMetadata());
            }

            int tokensUsed = 0;
            if (response.tokenUsage() != null) {
                tokensUsed = response.tokenUsage().totalTokenCount();
            }

            return new GenerationResult(responseText, citations, tokensUsed);
        });
    }

    private List<ChatMessage> buildMessages(GenerationConfig config) {
        List<ChatMessage> messages = new ArrayList<>();

        String systemPrompt = promptTemplateService.getSystemPrompt(config);
        messages.add(new SystemMessage(systemPrompt));

        String userPrompt = promptTemplateService.buildUserPrompt(
                config.query(), config.contexts(), config.conversationHistory());
        messages.add(new UserMessage(userPrompt));

        return messages;
    }

    @SuppressWarnings("unchecked")
    private GenerationConfig extractConfiguration(Map<String, Object> context) {
        String query = (String) context.get("query");
        List<String> contexts = (List<String>) context.getOrDefault("contexts", List.of());
        List<Map<String, Object>> contextMetadata = (List<Map<String, Object>>) context.getOrDefault("metadata",
                List.of());
        List<ConversationTurn> history = extractConversationHistory(context);

        String provider = (String) context.getOrDefault("provider", defaultProvider);
        String model = (String) context.getOrDefault("model", defaultModel);
        String apiKey = (String) context.getOrDefault("apiKey", System.getenv("OPENAI_API_KEY"));

        double temperature = context.containsKey("temperature") ? ((Number) context.get("temperature")).doubleValue()
                : defaultTemperature;
        int maxTokens = context.containsKey("maxTokens") ? ((Number) context.get("maxTokens")).intValue()
                : defaultMaxTokens;
        boolean includeCitations = context.containsKey("includeCitations") ? (Boolean) context.get("includeCitations")
                : defaultIncludeCitations;
        boolean useCache = context.containsKey("useCache") ? (Boolean) context.get("useCache") : defaultUseCache;
        String templateId = (String) context.getOrDefault("templateId", "default");

        return new GenerationConfig(query, contexts, contextMetadata, history,
                provider, model, apiKey, temperature, maxTokens,
                includeCitations, useCache, templateId);
    }

    @SuppressWarnings("unchecked")
    private List<ConversationTurn> extractConversationHistory(Map<String, Object> context) {
        if (!context.containsKey("conversationHistory"))
            return List.of();

        List<Map<String, Object>> historyList = (List<Map<String, Object>>) context.get("conversationHistory");

        return historyList.stream()
                .map(turn -> new ConversationTurn(
                        (String) turn.get("role"), (String) turn.get("content")))
                .collect(Collectors.toList());
    }

    private Uni<Boolean> validateConfiguration(GenerationConfig config) {
        return Uni.createFrom().item(() -> {
            if (config.query() == null || config.query().isBlank()) {
                LOG.error("No query provided");
                return false;
            }
            if (config.provider() == null || config.provider().isBlank()) {
                LOG.error("No provider specified");
                return false;
            }
            if (config.model() == null || config.model().isBlank()) {
                LOG.error("No model specified");
                return false;
            }
            if (config.temperature() < 0.0 || config.temperature() > 2.0) {
                LOG.error("Invalid temperature: {}", config.temperature());
                return false;
            }
            if (config.maxTokens() <= 0 || config.maxTokens() > 32000) {
                LOG.error("Invalid maxTokens: {}", config.maxTokens());
                return false;
            }
            return true;
        });
    }

    private String generateCacheKey(GenerationConfig config) {
        String contextsHash = String.valueOf(config.contexts().hashCode());
        String modelKey = config.provider() + ":" + config.model();
        return String.format("rag-gen:%s:%s:%s",
                modelKey, config.query().hashCode(), contextsHash);
    }

    @Override
    public boolean canHandle(NodeExecutionTask task) {
        Map<String, Object> context = task.context();
        return context.containsKey("query") &&
                (context.containsKey("contexts") || context.containsKey("metadata"));
    }
}