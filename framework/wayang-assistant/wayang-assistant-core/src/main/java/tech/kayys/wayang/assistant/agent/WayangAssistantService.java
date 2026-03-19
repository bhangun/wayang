package tech.kayys.wayang.assistant.agent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import tech.kayys.gollek.spi.inference.Message;
import tech.kayys.gollek.spi.stream.StreamChunk;
import tech.kayys.wayang.agent.core.inference.AgentInferenceRequest;
import tech.kayys.wayang.agent.core.inference.AgentInferenceResponse;
import tech.kayys.wayang.agent.core.inference.GollekInferenceService;
import tech.kayys.wayang.assistant.agent.ConversationSession.ConversationMessage;
import tech.kayys.wayang.assistant.agent.intent.AssistantIntentClassifier;
import tech.kayys.wayang.assistant.agent.project.ProjectGeneratorService;
import tech.kayys.wayang.assistant.agent.session.AssistantSessionManager;
import tech.kayys.wayang.assistant.agent.tool.AssistantSideEffectExecutor;
import tech.kayys.wayang.assistant.agent.troubleshoot.TroubleshootingService;
import tech.kayys.wayang.assistant.knowledge.KnowledgeSearchService;
import tech.kayys.wayang.project.api.ProjectDescriptor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Lean facade for the Wayang Internal Assistant.
 * Orchestrates specialized services for session management, intent
 * classification,
 * documentation search, and project generation.
 */
@ApplicationScoped
public class WayangAssistantService {

    private static final Logger LOG = Logger.getLogger(WayangAssistantService.class);

    @Inject
    AssistantSessionManager sessionManager;

    @Inject
    AssistantIntentClassifier intentClassifier;

    @Inject
    AssistantSideEffectExecutor sideEffectExecutor;

    @Inject
    KnowledgeSearchService searchService;

    @Inject
    ProjectGeneratorService projectGenerator;

    @Inject
    TroubleshootingService troubleshootingService;

    @Inject
    Instance<GollekInferenceService> inferenceService;

    /**
     * Multi-turn conversational chat (non-streaming).
     */
    public ChatResult chat(String sessionId, String message) {
        LOG.infof("Chat request – sessionId=%s", sessionId);

        ConversationSession session = sessionManager.getOrCreateSession(sessionId);
        session.addUserMessage(message);

        String enhancedQuery = buildEnhancedQuery(message, session);
        List<DocSearchResult> docs = searchService.searchDocumentation(enhancedQuery);

        String reply = synthesiseReply(message, docs, session);
        session.addAssistantMessage(reply);

        return new ChatResult(sessionId, reply, docs, session.getHistory());
    }

    /**
     * Multi-turn conversational chat with streaming and autonomous tool execution.
     *
     * <p>
     * All blocking work (intent classification, doc search, prompt building)
     * is deferred to the default Mutiny worker pool so the Vert.x event loop
     * is never blocked. Only the final {@code inferStream()} Multi is subscribed
     * on the caller's (event-loop) thread.
     */
    public io.smallrye.mutiny.Multi<StreamChunk> chatStream(String sessionId, String message) {
        LOG.infof("Chat stream request – sessionId=%s", sessionId);

        // Perform all blocking preparation on a worker thread, then switch to streaming
        return io.smallrye.mutiny.Uni.createFrom().item(() -> {
            ConversationSession session = sessionManager.getOrCreateSession(sessionId);
            session.addUserMessage(message);

            // 1. Intent Detection (may call LLM synchronously)
            String intent;
            try {
                intent = intentClassifier.detectIntent(message, session);
            } catch (Exception e) {
                LOG.warnf("Intent detection failed, defaulting to CHAT: %s", e.getMessage());
                intent = "CHAT";
            }
            LOG.infof("Detected Intent: %s", intent);

            // 2. Side-Effect Execution
            if ("BUG".equals(intent)) {
                try {
                    sideEffectExecutor.executeSlackReport(message, session);
                } catch (Exception e) {
                    LOG.warnf("Slack report side-effect failed: %s", e.getMessage());
                }
            }

            // 3. Contextual Search (file I/O)
            String enhancedQuery = buildEnhancedQuery(message, session);
            List<DocSearchResult> docs;
            try {
                docs = searchService.searchDocumentation(enhancedQuery);
            } catch (Exception e) {
                LOG.warnf("Doc search failed: %s", e.getMessage());
                docs = List.of();
            }

            String docContext = buildDocContextString(docs);

            String sysPrompt = """
                    You are Wayang Assistant, an expert AI for the Wayang multi-agent framework.

                    DIRECTIONS:
                    1. PERSONA: Helpful, professional, and culture-aware (premium shadow-puppet aesthetic).
                    2. CONTEXT: Use the provided documentation to answer the user.
                    3. INTENT STATUS: User intent detected as [%s].
                       - If BUG: Mention that you have autonomously reported this to the Slack #wayang-dev channel.
                    4. FORMAT: Use clean markdown for code.

                    Current Context:
                    %s
                    """.formatted(intent, docContext.isBlank() ? "No specific context found." : docContext);

            List<Message> history = session.getHistory().stream()
                    .limit(Math.max(0, session.getHistory().size() - 1))
                    .map(m -> m.role() == ConversationMessage.Role.USER
                            ? Message.user(m.content())
                            : Message.assistant(m.content()))
                    .collect(Collectors.toList());

            AgentInferenceRequest req = AgentInferenceRequest.builder()
                    .systemPrompt(sysPrompt)
                    .userPrompt(message)
                    .conversationHistory(history)
                    .temperature(0.3)
                    .stream(true)
                    .build();

            return req;
        })
                .runSubscriptionOn(io.smallrye.mutiny.infrastructure.Infrastructure.getDefaultWorkerPool())
                .onItem().transformToMulti(req -> {
                    if (inferenceService != null && inferenceService.isResolvable()) {
                        return inferenceService.get().inferStream(req);
                    } else {
                        ConversationSession session = sessionManager.getOrCreateSession(sessionId);
                        List<DocSearchResult> docs;
                        try {
                            docs = searchService.searchDocumentation(message);
                        } catch (Exception e) {
                            docs = List.of();
                        }
                        String ruleReply = buildRuleBasedReply(message, docs, session);
                        session.addAssistantMessage(ruleReply);
                        return io.smallrye.mutiny.Multi.createFrom().item(
                                tech.kayys.gollek.spi.stream.StreamChunk.finalChunk(
                                        "fallback-" + session.getSessionId(), 0, ruleReply));
                    }
                });
    }

    public List<DocSearchResult> searchDocumentation(String query) {
        return searchService.searchDocumentation(query);
    }

    public ProjectDescriptor generateProject(String intent) {
        return projectGenerator.generateProject(intent);
    }

    public ErrorTroubleshootingResult troubleshootError(String errorMessage) {
        return troubleshootingService.troubleshootError(errorMessage);
    }

    public List<ConversationMessage> getSessionHistory(String sessionId) {
        return sessionManager.getSessionHistory(sessionId);
    }

    public boolean deleteSession(String sessionId) {
        return sessionManager.deleteSession(sessionId);
    }

    public int activeSessionCount() {
        return sessionManager.activeSessionCount();
    }

    // ── Internal Logic ───────────────────────────────────────────

    private String buildEnhancedQuery(String message, ConversationSession session) {
        List<ConversationMessage> history = session.getHistory();
        if (history.size() <= 1)
            return message;

        Optional<String> lastAssistantReply = history.stream()
                .filter(m -> m.role() == ConversationMessage.Role.ASSISTANT)
                .reduce((a, b) -> b)
                .map(ConversationMessage::content);

        return lastAssistantReply
                .map(prev -> message + " " + extractKeywords(prev, 5))
                .orElse(message);
    }

    private String extractKeywords(String text, int max) {
        return Arrays.stream(text.replaceAll("[^a-zA-Z0-9 ]", " ").split("\\s+"))
                .filter(w -> w.length() > 4)
                .distinct()
                .limit(max)
                .collect(Collectors.joining(" "));
    }

    private String buildDocContextString(List<DocSearchResult> docs) {
        StringBuilder sb = new StringBuilder();
        List<DocSearchResult> validDocs = docs.stream()
                .filter(d -> !"No results found".equals(d.getTitle()))
                .limit(5)
                .collect(Collectors.toList());

        for (DocSearchResult doc : validDocs) {
            sb.append("Source: ").append(doc.getUrl()).append("\n")
                    .append("Title: ").append(doc.getTitle()).append("\n")
                    .append(doc.getSnippet()).append("\n\n");
        }
        return sb.toString();
    }

    private String synthesiseReply(String question, List<DocSearchResult> docs, ConversationSession session) {
        String docContext = buildDocContextString(docs);

        if (inferenceService != null && inferenceService.isResolvable()) {
            return generateLlmReply(question, docContext, docs, session);
        }

        return buildRuleBasedReply(question, docs, session);
    }

    private String generateLlmReply(String question, String docContext, List<DocSearchResult> docs,
            ConversationSession session) {
        String sysPrompt = """
                You are Wayang Assistant, a helpful and expert AI for the Wayang multi-agent framework.
                Answer the user's question clearly and accurately based ON THE PROVIDED CONTEXT ONLY.
                If the context does not contain the answer, say you don't know and provide general advice.
                For code snippets, format them neatly in markdown.
                At the end of your response, append the list of sources you used as clickable markdown links.

                Context:
                %s
                """.formatted(docContext.isBlank() ? "No specific context found." : docContext);

        List<Message> history = session.getHistory().stream()
                .limit(Math.max(0, session.getHistory().size() - 1))
                .map(m -> m.role() == ConversationMessage.Role.USER
                        ? Message.user(m.content())
                        : Message.assistant(m.content()))
                .collect(Collectors.toList());

        AgentInferenceRequest req = AgentInferenceRequest.builder()
                .systemPrompt(sysPrompt)
                .userPrompt(question)
                .conversationHistory(history)
                .temperature(0.3)
                .build();

        try {
            AgentInferenceResponse res = inferenceService.get().infer(req);
            if (res.isError()) {
                return buildRuleBasedReply(question, docs, session);
            }

            String reply = res.getContent();
            List<DocSearchResult> validDocs = docs.stream()
                    .filter(d -> !"No results found".equals(d.getTitle()))
                    .collect(Collectors.toList());

            if (!validDocs.isEmpty() && !reply.contains("Sources:") && !reply.contains("Sources used:")) {
                reply += "\n\n---\n*Sources: " +
                        validDocs.stream().map(d -> "`" + d.getUrl() + "`").collect(Collectors.joining(", ")) +
                        "*";
            }
            return reply;

        } catch (Exception e) {
            LOG.warnf("LLM inference exception: %s", e.getMessage());
            return buildRuleBasedReply(question, docs, session);
        }
    }

    private String buildRuleBasedReply(String question, List<DocSearchResult> docs, ConversationSession session) {
        StringBuilder reply = new StringBuilder();
        List<DocSearchResult> validDocs = docs.stream()
                .filter(d -> !"No results found".equals(d.getTitle()))
                .limit(5)
                .collect(Collectors.toList());

        if (validDocs.isEmpty()) {
            reply.append("I couldn't find relevant documentation for your question. ")
                    .append("Here are some suggestions:\n")
                    .append("- Try rephrasing with specific Wayang terms (e.g. RAG, orchestrator, HITL, guardrails)\n")
                    .append("- Check the full docs at https://wayang.github.io\n")
                    .append("- Use `/troubleshoot` if you are seeing an error message");
        } else {
            reply.append("Based on the Wayang documentation:\n\n");
            for (DocSearchResult doc : validDocs) {
                reply.append("**").append(doc.getTitle()).append("**\n");
                reply.append(doc.getSnippet()).append("\n\n");
            }
            reply.append("---\n");
            reply.append("*Sources: ").append(
                    validDocs.stream().map(d -> "`" + d.getUrl() + "`").collect(Collectors.joining(", "))).append("*");
        }

        long userTurns = session.getHistory().stream()
                .filter(m -> m.role() == ConversationMessage.Role.USER).count();
        if (userTurns > 1) {
            reply.append("\n\n_Continuing from our conversation — let me know if you need more detail._");
        }

        return reply.toString();
    }

    // ── Public Result Types ─────────────────────────────────────

    public static class DocSearchResult {
        private final String title, url, snippet, filePath;
        private final int score;

        public DocSearchResult(String title, String url, String snippet) {
            this(title, url, snippet, 0, null);
        }

        public DocSearchResult(String title, String url, String snippet, int score, String filePath) {
            this.title = title;
            this.url = url;
            this.snippet = snippet;
            this.score = score;
            this.filePath = filePath;
        }

        public String getTitle() {
            return title;
        }

        public String getUrl() {
            return url;
        }

        public String getSnippet() {
            return snippet;
        }

        public int getScore() {
            return score;
        }

        public String getFilePath() {
            return filePath;
        }
    }

    public static class ErrorTroubleshootingResult {
        private final String errorMessage, advice;
        private final List<DocSearchResult> documentationResults;

        public ErrorTroubleshootingResult(String errorMessage, String advice,
                List<DocSearchResult> documentationResults) {
            this.errorMessage = errorMessage;
            this.advice = advice;
            this.documentationResults = documentationResults;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getAdvice() {
            return advice;
        }

        public List<DocSearchResult> getDocumentationResults() {
            return documentationResults;
        }
    }

    public static class ChatResult {
        private final String sessionId, reply;
        private final List<DocSearchResult> relevantDocs;
        private final List<ConversationMessage> history;

        public ChatResult(String sessionId, String reply, List<DocSearchResult> relevantDocs,
                List<ConversationMessage> history) {
            this.sessionId = sessionId;
            this.reply = reply;
            this.relevantDocs = relevantDocs;
            this.history = history;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getReply() {
            return reply;
        }

        public List<DocSearchResult> getRelevantDocs() {
            return relevantDocs;
        }

        public List<ConversationMessage> getHistory() {
            return history;
        }
    }
}
