package tech.kayys.wayang.assistant.api;

import jakarta.inject.Inject;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import tech.kayys.wayang.project.api.AssistantHelper;
import tech.kayys.wayang.assistant.agent.ConversationSession.ConversationMessage;
import tech.kayys.wayang.assistant.agent.WayangAssistantService;
import tech.kayys.wayang.assistant.agent.WayangAssistantService.*;
import tech.kayys.wayang.assistant.knowledge.KnowledgeSourceRegistry;
import tech.kayys.wayang.project.api.ProjectDescriptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST API for the Wayang Internal Assistant.
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li>{@code POST /api/v1/assistant/ask}               – single Q&amp;A</li>
 *   <li>{@code POST /api/v1/assistant/chat}              – multi-turn chat (sends/receives a sessionId)</li>
 *   <li>{@code GET  /api/v1/assistant/chat/{sid}/history} – conversation history</li>
 *   <li>{@code DELETE /api/v1/assistant/chat/{sid}}      – clear a session</li>
 *   <li>{@code POST /api/v1/assistant/generate-project}  – project generation from intent</li>
 *   <li>{@code POST /api/v1/assistant/troubleshoot}      – error troubleshooting</li>
 *   <li>{@code GET  /api/v1/assistant/capabilities}      – feature manifest</li>
 *   <li>{@code GET  /api/v1/assistant/knowledge-sources} – list of registered knowledge sources</li>
 *   <li>{@code POST /api/v1/assistant/suggestions}       – contextual design suggestions</li>
 * </ul>
 */
@Path("/api/v1/assistant")
@PermitAll
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WayangAssistantApi {

    private static final Logger LOG = Logger.getLogger(WayangAssistantApi.class);

    @Inject
    WayangAssistantService assistantService;

    @Inject
    KnowledgeSourceRegistry knowledgeRegistry;

    // -----------------------------------------------------------------------
    // Single-turn question answering
    // -----------------------------------------------------------------------

    @POST
    @Path("/ask")
    public Response askQuestion(AssistantRequest request) {
        LOG.infof("Processing question: %s", request.question());
        if (request.question() == null || request.question().isBlank()) {
            return badRequest("question is required");
        }
        try {
            List<DocSearchResult> docs = assistantService.searchDocumentation(request.question());
            Map<String, Object> resp = new HashMap<>();
            resp.put("question", request.question());
            resp.put("documentationResults", toDocList(docs));
            resp.put("suggestion", generateAnswerSuggestion(request.question(), docs));
            return Response.ok(resp).build();
        } catch (Exception e) {
            return serverError("Failed to process question", e);
        }
    }

    // -----------------------------------------------------------------------
    // Multi-turn Chat
    // -----------------------------------------------------------------------

    /**
     * Multi-turn conversational chat endpoint.
     * If {@code sessionId} is omitted a new session UUID is created automatically.
     */
    @POST
    @Path("/chat")
    public Response chat(ChatRequest request) {
        if (request.message() == null || request.message().isBlank()) {
            return badRequest("message is required");
        }
        String sessionId = (request.sessionId() != null && !request.sessionId().isBlank())
                ? request.sessionId() : UUID.randomUUID().toString();
        try {
            ChatResult result = assistantService.chat(sessionId, request.message());
            Map<String, Object> resp = new HashMap<>();
            resp.put("sessionId", result.getSessionId());
            resp.put("reply", result.getReply());
            resp.put("relevantDocs", toDocList(result.getRelevantDocs()));
            resp.put("history", toHistoryList(result.getHistory()));
            return Response.ok(resp).build();
        } catch (Exception e) {
            return serverError("Failed to process chat message", e);
        }
    }

    /**
     * Return the conversation history for a session.
     */
    @GET
    @Path("/chat/{sessionId}/history")
    public Response getChatHistory(@PathParam("sessionId") String sessionId) {
        List<ConversationMessage> history = assistantService.getSessionHistory(sessionId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("sessionId", sessionId);
        resp.put("history", toHistoryList(history));
        resp.put("messageCount", history.size());
        return Response.ok(resp).build();
    }

    /**
     * Delete a chat session and its history.
     */
    @DELETE
    @Path("/chat/{sessionId}")
    public Response deleteChatSession(@PathParam("sessionId") String sessionId) {
        boolean removed = assistantService.deleteSession(sessionId);
        if (removed) {
            return Response.ok(Map.of("sessionId", sessionId, "deleted", true)).build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", "Session not found: " + sessionId)).build();
    }

    // -----------------------------------------------------------------------
    // Project generation
    // -----------------------------------------------------------------------

    @POST
    @Path("/generate-project")
    public Response generateProject(ProjectGenerationRequest request) {
        if (request.intent() == null || request.intent().isBlank()) {
            return badRequest("intent is required");
        }
        try {
            ProjectDescriptor descriptor = assistantService.generateProject(request.intent());
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("project", descriptor);
            resp.put("summary", generateProjectSummary(descriptor));
            resp.put("nextSteps", List.of(
                    "Review the generated project structure",
                    "Customise workflow nodes and configurations",
                    "Add your own prompts and model settings",
                    "Deploy via `POST /api/v1/projects/{id}/executions`"
            ));
            return Response.ok(resp).build();
        } catch (Exception e) {
            return serverError("Failed to generate project", e);
        }
    }

    // -----------------------------------------------------------------------
    // Error troubleshooting
    // -----------------------------------------------------------------------

    @POST
    @Path("/troubleshoot")
    public Response troubleshootError(ErrorTroubleshootingRequest request) {
        if (request.errorMessage() == null || request.errorMessage().isBlank()) {
            return badRequest("errorMessage is required");
        }
        try {
            ErrorTroubleshootingResult result = assistantService.troubleshootError(request.errorMessage());
            Map<String, Object> resp = new HashMap<>();
            resp.put("errorMessage", result.getErrorMessage());
            resp.put("advice", result.getAdvice());
            resp.put("documentationResults", toDocList(result.getDocumentationResults()));
            resp.put("additionalHelp", List.of(
                    "Check application.properties for configuration issues",
                    "Review logs for additional context",
                    "Ensure all Maven dependencies are properly declared",
                    "Verify that required services (PostgreSQL, Kafka) are running"
            ));
            return Response.ok(resp).build();
        } catch (Exception e) {
            return serverError("Failed to troubleshoot error", e);
        }
    }

    // -----------------------------------------------------------------------
    // Design Suggestions
    // -----------------------------------------------------------------------

    @POST
    @Path("/suggestions")
    public Response getSuggestions(ProjectSuggestionsRequest request) {
        if (request.project() == null) {
            return badRequest("project is required");
        }
        try {
            List<String> suggestions = AssistantHelper.suggestImprovements(request.project());
            Map<String, Object> resp = new HashMap<>();
            resp.put("projectName", request.project().getName());
            resp.put("suggestions", suggestions);
            resp.put("count", suggestions.size());
            resp.put("timestamp", System.currentTimeMillis());
            return Response.ok(resp).build();
        } catch (Exception e) {
            return serverError("Failed to generate suggestions", e);
        }
    }

    // -----------------------------------------------------------------------
    // Knowledge sources
    // -----------------------------------------------------------------------

    /**
     * List all registered knowledge sources (docs sites + GitHub repos).
     */
    @GET
    @Path("/knowledge-sources")
    public Response getKnowledgeSources() {
        List<Map<String, Object>> sources = knowledgeRegistry.getSources().stream()
                .map(src -> Map.<String, Object>of(
                        "id", src.id(),
                        "name", src.name(),
                        "url", src.baseUrl(),
                        "type", src.type().name()
                ))
                .toList();
        return Response.ok(Map.of("sources", sources, "count", sources.size())).build();
    }

    // -----------------------------------------------------------------------
    // Capabilities manifest
    // -----------------------------------------------------------------------

    @GET
    @Path("/capabilities")
    public Response getCapabilities() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("name", "Wayang Internal Assistant");
        resp.put("version", "2.0.0");
        resp.put("capabilities", List.of(
                Map.of("id", "ask",              "endpoint", "/api/v1/assistant/ask",              "description", "Single-turn Q&A over Wayang docs"),
                Map.of("id", "chat",             "endpoint", "/api/v1/assistant/chat",             "description", "Multi-turn conversational chat with session memory"),
                Map.of("id", "generate-project", "endpoint", "/api/v1/assistant/generate-project", "description", "Generate Wayang project from natural-language intent"),
                Map.of("id", "troubleshoot",     "endpoint", "/api/v1/assistant/troubleshoot",     "description", "Categorised error troubleshooting advice"),
                Map.of("id", "knowledge-sources","endpoint", "/api/v1/assistant/knowledge-sources","description", "List all knowledge sources")
        ));
        resp.put("knowledgeSources", List.of(
                "wayang-ai.github.io", "gollek-ai.github.io", "gamelan-ai.github.io",
                "github.com/bhangun/wayang", "github.com/bhangun/gollek", "github.com/bhangun/gamelan"
        ));
        resp.put("tools", List.of(
                Map.of("id", "wayang-doc-search",       "name", "Documentation Search"),
                Map.of("id", "wayang-project-generator","name", "Project Generator"),
                Map.of("id", "wayang-error-help",       "name", "Error Troubleshooter"),
                Map.of("id", "web-search",              "name", "Web Search")
        ));
        resp.put("activeSessions", assistantService.activeSessionCount());
        return Response.ok(resp).build();
    }

    // -----------------------------------------------------------------------
    // DTO records (concise Java 16+ records)
    // -----------------------------------------------------------------------

    public record AssistantRequest(String question) {}
    public record ChatRequest(String sessionId, String message) {}
    public record ProjectGenerationRequest(String intent, String name, String description) {}
    public record ErrorTroubleshootingRequest(String errorMessage, String context) {}
    public record ProjectSuggestionsRequest(ProjectDescriptor project) {}

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private List<Map<String, Object>> toDocList(List<DocSearchResult> docs) {
        return docs.stream()
                .map(r -> Map.<String, Object>of(
                        "title",   r.getTitle(),
                        "snippet", r.getSnippet(),
                        "url",     r.getUrl(),
                        "score",   r.getScore()
                ))
                .toList();
    }

    private List<Map<String, Object>> toHistoryList(List<ConversationMessage> history) {
        return history.stream()
                .map(m -> Map.<String, Object>of(
                        "role",      m.role().name().toLowerCase(),
                        "content",   m.content(),
                        "timestamp", m.timestamp().toString()
                ))
                .toList();
    }

    private String generateAnswerSuggestion(String question, List<DocSearchResult> results) {
        long valid = results.stream().filter(r -> !"No results found".equals(r.getTitle())).count();
        if (valid == 0) return "No documentation matches found. Try different keywords or use /troubleshoot for errors.";
        return String.format("Found %d relevant result(s) across all knowledge sources.", valid);
    }

    private String generateProjectSummary(ProjectDescriptor descriptor) {
        return "Generated project: " + descriptor.getName() + "\n"
                + "Capabilities: " + String.join(", ", descriptor.getCapabilities()) + "\n"
                + "Workflows: " + descriptor.getWorkflows().size();
    }

    private Response badRequest(String msg) {
        return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", msg)).build();
    }

    private Response serverError(String msg, Exception e) {
        LOG.error(msg, e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", msg + ": " + e.getMessage())).build();
    }
}
