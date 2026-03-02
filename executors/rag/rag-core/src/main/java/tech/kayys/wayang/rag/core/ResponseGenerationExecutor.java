package tech.kayys.wayang.rag.core;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.gamelan.engine.error.ErrorInfo;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.sdk.executor.core.SimpleNodeExecutionResult;
import tech.kayys.gamelan.sdk.executor.core.AbstractWorkflowExecutor;
import tech.kayys.gamelan.engine.protocol.CommunicationType;
import tech.kayys.gamelan.sdk.executor.core.Executor;

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
@Executor(executorType = "rag-response-generation", communicationType = CommunicationType.GRPC, maxConcurrentTasks = 15, supportedNodeTypes = {
                "TASK", "RAG_GENERATION" }, version = "1.0.0")
@ApplicationScoped
public class ResponseGenerationExecutor extends AbstractWorkflowExecutor {

        private static final Logger LOG = LoggerFactory.getLogger(ResponseGenerationExecutor.class);

        public record GenerationResult(String response, List<Citation> citations, int tokensUsed) {
        }

        public record GenerationContext(
                        String query,
                        List<String> contexts,
                        List<Map<String, Object>> contextMetadata,
                        List<ConversationTurn> conversationHistory,
                        GenerationConfig config,
                        boolean includeCitations,
                        boolean useCache,
                        String templateId,
                        String apiKey) {
        }

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

                GenerationContext genCtx = extractConfiguration(context);

                return validateConfiguration(genCtx)
                                .flatMap(valid -> {
                                        if (!valid) {
                                                return Uni.createFrom().item(SimpleNodeExecutionResult.failure(
                                                                task.runId(), task.nodeId(), task.attempt(),
                                                                new ErrorInfo(
                                                                                "CONFIG_INVALID",
                                                                                "Invalid generation configuration",
                                                                                "",
                                                                                Map.of("retryable", false)),
                                                                task.token()));
                                        }

                                        // Check cache
                                        if (genCtx.useCache()) {
                                                String cacheKey = generateCacheKey(genCtx);
                                                String cachedResponse = cacheService.get(cacheKey);

                                                if (cachedResponse != null) {
                                                        LOG.info("Cache hit for query: {}", genCtx.query());
                                                        return Uni.createFrom().item(SimpleNodeExecutionResult.success(
                                                                        task.runId(), task.nodeId(), task.attempt(),
                                                                        Map.of("response", cachedResponse, "cached",
                                                                                        true,
                                                                                        "query", genCtx.query()),
                                                                        task.token(),
                                                                        Duration.ZERO));
                                                }
                                        }

                                        return generateResponse(genCtx, task.runId().value())
                                                        .map(result -> {
                                                                long durationMs = Duration
                                                                                .between(startTime, Instant.now())
                                                                                .toMillis();

                                                                metricsCollector.recordGeneration(
                                                                                task.runId().value(),
                                                                                result.tokensUsed(), durationMs);

                                                                if (genCtx.useCache()) {
                                                                        cacheService.put(generateCacheKey(genCtx),
                                                                                        result.response());
                                                                }

                                                                return SimpleNodeExecutionResult.success(
                                                                                task.runId(), task.nodeId(),
                                                                                task.attempt(),
                                                                                Map.of(
                                                                                                "response",
                                                                                                result.response(),
                                                                                                "citations",
                                                                                                result.citations(),
                                                                                                "tokensUsed",
                                                                                                result.tokensUsed(),
                                                                                                "durationMs",
                                                                                                durationMs,
                                                                                                "model",
                                                                                                genCtx.config().model(),
                                                                                                "cached", false,
                                                                                                "query",
                                                                                                genCtx.query()),
                                                                                task.token(),
                                                                                Duration.ofMillis(durationMs));
                                                        })
                                                        .onFailure().recoverWithItem(error -> {
                                                                LOG.error("Response generation failed", error);
                                                                return SimpleNodeExecutionResult.failure(
                                                                                task.runId(), task.nodeId(),
                                                                                task.attempt(),
                                                                                new ErrorInfo(
                                                                                                "INFERENCE_REQUEST_FAILED",
                                                                                                error.getMessage(),
                                                                                                "",
                                                                                                Map.of("retryable",
                                                                                                                true)),
                                                                                task.token());
                                                        });
                                });
        }

        private Uni<GenerationResult> generateResponse(GenerationContext genCtx, String workflowRunId) {
                LOG.debug("Generating response for query: '{}' using model: {}",
                                genCtx.query(), genCtx.config().model());

                return Uni.createFrom().item(() -> {
                        ChatLanguageModel chatModel = modelFactory.createModel(
                                        genCtx.config().provider(), genCtx.config().model(), genCtx.apiKey(),
                                        genCtx.config().temperature(), genCtx.config().maxTokens());

                        List<ChatMessage> messages = buildMessages(genCtx);

                        Response<AiMessage> response = chatModel.generate(messages);
                        String responseText = response.content().text();

                        responseText = guardrailEngine.validateAndSanitize(responseText, genCtx.config());

                        List<Citation> citations = Collections.emptyList();
                        if (genCtx.includeCitations() && !genCtx.contexts().isEmpty()) {
                                citations = citationService.generateCitations(
                                                responseText, genCtx.contexts(), genCtx.contextMetadata());
                        }

                        int tokensUsed = 0;
                        if (response.tokenUsage() != null) {
                                tokensUsed = response.tokenUsage().totalTokenCount();
                        }

                        return new GenerationResult(responseText, citations, tokensUsed);
                });
        }

        private List<ChatMessage> buildMessages(GenerationContext genCtx) {
                List<ChatMessage> messages = new ArrayList<>();

                String systemPrompt = promptTemplateService.getSystemPrompt(genCtx.config());
                messages.add(new SystemMessage(systemPrompt));

                String userPrompt = promptTemplateService.buildUserPrompt(
                                genCtx.query(), genCtx.contexts(), genCtx.conversationHistory());
                messages.add(new UserMessage(userPrompt));

                return messages;
        }

        @SuppressWarnings("unchecked")
        private GenerationContext extractConfiguration(Map<String, Object> context) {
                String query = (String) context.get("query");
                List<String> contexts = (List<String>) context.getOrDefault("contexts", List.of());
                List<Map<String, Object>> contextMetadata = (List<Map<String, Object>>) context.getOrDefault("metadata",
                                List.of());
                List<ConversationTurn> history = extractConversationHistory(context);

                String provider = (String) context.getOrDefault("provider", defaultProvider);
                String model = (String) context.getOrDefault("model", defaultModel);
                String apiKey = (String) context.getOrDefault("apiKey", System.getenv("OPENAI_API_KEY"));

                double temperature = context.containsKey("temperature")
                                ? ((Number) context.get("temperature")).doubleValue()
                                : defaultTemperature;
                int maxTokens = context.containsKey("maxTokens") ? ((Number) context.get("maxTokens")).intValue()
                                : defaultMaxTokens;
                boolean includeCitations = context.containsKey("includeCitations")
                                ? (Boolean) context.get("includeCitations")
                                : defaultIncludeCitations;
                boolean useCache = context.containsKey("useCache") ? (Boolean) context.get("useCache")
                                : defaultUseCache;
                String templateId = (String) context.getOrDefault("templateId", "default");

                GenerationConfig config = new GenerationConfig(provider, model, (float) temperature, maxTokens,
                                1.0f, 0.0f, 0.0f, List.of(), "You are a helpful assistant.",
                                Map.of(), includeCitations, false, CitationStyle.INLINE_NUMBERED,
                                false, false, Map.of());

                return new GenerationContext(query, contexts, contextMetadata, history,
                                config, includeCitations, useCache, templateId, apiKey);
        }

        @SuppressWarnings("unchecked")
        private List<ConversationTurn> extractConversationHistory(Map<String, Object> context) {
                if (!context.containsKey("conversationHistory"))
                        return List.of();

                List<Map<String, Object>> historyList = (List<Map<String, Object>>) context.get("conversationHistory");

                return historyList.stream()
                                .map(turn -> new ConversationTurn(
                                                (String) turn.get("role"), (String) turn.get("content"), Instant.now()))
                                .collect(Collectors.toList());
        }

        private Uni<Boolean> validateConfiguration(GenerationContext genCtx) {
                return Uni.createFrom().item(() -> {
                        if (genCtx.query() == null || genCtx.query().isBlank()) {
                                LOG.error("No query provided");
                                return false;
                        }
                        if (genCtx.config().provider() == null || genCtx.config().provider().isBlank()) {
                                LOG.error("No provider specified");
                                return false;
                        }
                        if (genCtx.config().model() == null || genCtx.config().model().isBlank()) {
                                LOG.error("No model specified");
                                return false;
                        }
                        if (genCtx.config().temperature() < 0.0 || genCtx.config().temperature() > 2.0) {
                                LOG.error("Invalid temperature: {}", genCtx.config().temperature());
                                return false;
                        }
                        if (genCtx.config().maxTokens() <= 0 || genCtx.config().maxTokens() > 32000) {
                                LOG.error("Invalid maxTokens: {}", genCtx.config().maxTokens());
                                return false;
                        }
                        return true;
                });
        }

        private String generateCacheKey(GenerationContext genCtx) {
                String contextsHash = String.valueOf(genCtx.contexts().hashCode());
                String modelKey = genCtx.config().provider() + ":" + genCtx.config().model();
                return String.format("rag-gen:%s:%s:%s",
                                modelKey, genCtx.query().hashCode(), contextsHash);
        }

        @Override
        public boolean canHandle(NodeExecutionTask task) {
                Map<String, Object> context = task.context();
                return context.containsKey("query") &&
                                (context.containsKey("contexts") || context.containsKey("metadata"));
        }
}
