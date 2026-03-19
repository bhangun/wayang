package tech.kayys.wayang.assistant.agent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.assistant.agent.ConversationSession.ConversationMessage;
import tech.kayys.wayang.assistant.agent.tool.WayangDocSearchTool;
import tech.kayys.wayang.assistant.agent.tool.WayangErrorHelpTool;
import tech.kayys.wayang.assistant.agent.tool.WayangProjectGeneratorTool;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link WayangAssistantService}.
 *
 * These tests run without a Quarkus context: CDI-injected fields that are
 * optional (like {@code knowledgeRegistry}) remain null and the service falls
 * back gracefully to the local-docs search path.
 */
public class WayangAssistantServiceTest {

    private WayangAssistantService svc;

    @BeforeEach
    void setUp() {
        svc = new WayangAssistantService();
        
        // Manually initialize and inject dependencies since we are not in a CDI container
        svc.sessionManager = new tech.kayys.wayang.assistant.agent.session.AssistantSessionManager();
        svc.intentClassifier = new tech.kayys.wayang.assistant.agent.intent.AssistantIntentClassifier();
        svc.sideEffectExecutor = new tech.kayys.wayang.assistant.agent.tool.AssistantSideEffectExecutor();
        svc.searchService = new tech.kayys.wayang.assistant.knowledge.KnowledgeSearchService();
        svc.projectGenerator = new tech.kayys.wayang.assistant.agent.project.ProjectGeneratorService();
        svc.troubleshootingService = new tech.kayys.wayang.assistant.agent.troubleshoot.TroubleshootingService();
        
        // Link dependencies (Some services need each other)
        svc.troubleshootingService.searchService = svc.searchService;
        svc.searchService.knowledgeRegistry = null; // Local fallback
    }

    // -----------------------------------------------------------------------
    // Doc search
    // -----------------------------------------------------------------------

    @Test
    void searchDocumentation_returnsAtLeastOneResult() {
        List<WayangAssistantService.DocSearchResult> results = svc.searchDocumentation("Wayang");
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Should return at least one result");
    }

    @Test
    void searchDocumentation_unknownQueryReturnsNoResultsPlaceholder() {
        List<WayangAssistantService.DocSearchResult> results =
                svc.searchDocumentation("xyzzy_totally_unknown_term_42");
        assertFalse(results.isEmpty(), "Should always return at least a fallback entry");
    }

    // -----------------------------------------------------------------------
    // Multi-turn chat / sessions
    // -----------------------------------------------------------------------

    @Test
    void chat_firstMessageCreatesSession() {
        String sessionId = "test-session-1";
        WayangAssistantService.ChatResult result = svc.chat(sessionId, "What is Wayang?");

        assertNotNull(result);
        assertEquals(sessionId, result.getSessionId());
        assertNotNull(result.getReply());
        assertFalse(result.getReply().isBlank());
        assertFalse(result.getHistory().isEmpty());
    }

    @Test
    void chat_multiTurnPreservesHistory() {
        String sid = "multi-turn-test";
        svc.chat(sid, "How do I use RAG in Wayang?");
        svc.chat(sid, "What about web search?");
        WayangAssistantService.ChatResult third = svc.chat(sid, "Can I combine both?");

        List<ConversationMessage> history = third.getHistory();
        assertEquals(6, history.size(), "3 turns × 2 messages each = 6 history entries");
    }

    @Test
    void getSessionHistory_returnsCorrectHistory() {
        String sid = "history-test";
        svc.chat(sid, "Hello");
        List<ConversationMessage> history = svc.getSessionHistory(sid);
        assertFalse(history.isEmpty());
        assertEquals(ConversationMessage.Role.USER,      history.get(0).role());
        assertEquals(ConversationMessage.Role.ASSISTANT, history.get(1).role());
    }

    @Test
    void getSessionHistory_unknownSessionReturnsEmpty() {
        List<ConversationMessage> history = svc.getSessionHistory("unknown-session-xyz");
        assertTrue(history.isEmpty());
    }

    @Test
    void deleteSession_removesSession() {
        String sid = "delete-test";
        svc.chat(sid, "Hello");
        assertTrue(svc.deleteSession(sid), "Delete should return true");
        assertTrue(svc.getSessionHistory(sid).isEmpty(), "History should be empty after delete");
        assertFalse(svc.deleteSession(sid), "Second delete should return false (already gone)");
    }

    @Test
    void activeSessionCount_incrementsAndDecrements() {
        int before = svc.activeSessionCount();
        svc.chat("count-test-session", "hi");
        assertEquals(before + 1, svc.activeSessionCount());
        svc.deleteSession("count-test-session");
        assertEquals(before, svc.activeSessionCount());
    }

    // -----------------------------------------------------------------------
    // Project generation
    // -----------------------------------------------------------------------

    @Test
    void generateProject_basicAgent() {
        var desc = svc.generateProject("build a simple workflow");
        assertNotNull(desc);
        assertNotNull(desc.getName());
        assertFalse(desc.getCapabilities().isEmpty());
    }

    @Test
    void generateProject_detectsRagIntent() {
        var desc = svc.generateProject("create a RAG-based document Q&A bot");
        assertNotNull(desc);
        assertTrue(desc.getCapabilities().contains("rag"), "Should detect RAG capability");
    }

    @Test
    void generateProject_detectsHitlIntent() {
        var desc = svc.generateProject("build a workflow with human in the loop approval");
        assertNotNull(desc);
        assertTrue(desc.getCapabilities().contains("hitl"), "Should detect HITL capability");
    }

    @Test
    void generateProject_detectsOrchestratorIntent() {
        var desc = svc.generateProject("multi-agent orchestrator for code review");
        assertNotNull(desc);
        assertTrue(
                desc.getCapabilities().contains("orchestrator") || desc.getCapabilities().contains("multi-agent"),
                "Should detect orchestrator/multi-agent capability"
        );
    }

    // -----------------------------------------------------------------------
    // Error troubleshooting
    // -----------------------------------------------------------------------

    @Test
    void troubleshootError_producesNonEmptyAdvice() {
        var result = svc.troubleshootError("NullPointerException at AbstractAgentExecutor.execute");
        assertNotNull(result);
        assertNotNull(result.getAdvice());
        assertFalse(result.getAdvice().isBlank());
        assertTrue(result.getAdvice().contains("Null"), "Should include null-specific section");
    }

    @Test
    void troubleshootError_connectionError() {
        var result = svc.troubleshootError("Connection timeout connecting to PostgreSQL");
        assertTrue(result.getAdvice().contains("Connection"), "Should include connection-specific advice");
    }

    @Test
    void troubleshootError_cdiError() {
        var result = svc.troubleshootError("Unsatisfied dependencies for type GuardrailsService");
        assertTrue(result.getAdvice().contains("CDI"), "Should include CDI-specific advice");
    }

    @Test
    void troubleshootError_guardrailError() {
        var result = svc.troubleshootError("Request blocked by guardrail policy");
        assertTrue(result.getAdvice().contains("Guardrail"), "Should include guardrail-specific advice");
    }

    // -----------------------------------------------------------------------
    // Tool SPI classes
    // -----------------------------------------------------------------------

    @Test
    void wayangDocSearchTool_execute_returnsResults() {
        WayangDocSearchTool tool = new WayangDocSearchTool();
        // Inject service reflectively — avoids CDI requirement
        tool.searchService = svc.searchService;

        Map<String, Object> out = tool.execute(Map.of("query", (Object) "RAG"), Map.of())
                .await().indefinitely();
        assertNotNull(out);
        assertTrue(out.containsKey("results"), "Should contain results key");
    }

    @Test
    void wayangDocSearchTool_missingQuery_throws() {
        WayangDocSearchTool tool = new WayangDocSearchTool();
        tool.searchService = svc.searchService;
        assertThrows(Exception.class,
                () -> tool.execute(Map.of(), Map.of()).await().indefinitely());
    }

    @Test
    void wayangErrorHelpTool_execute_returnsAdvice() {
        WayangErrorHelpTool tool = new WayangErrorHelpTool();
        tool.troubleshootingService = svc.troubleshootingService;

        Map<String, Object> out = tool.execute(
                Map.of("error", (Object) "ClassNotFoundException"), Map.of())
                .await().indefinitely();
        assertNotNull(out);
        assertTrue(out.containsKey("advice"), "Should contain advice key");
    }

    @Test
    void wayangProjectGeneratorTool_execute_returnsProject() {
        WayangProjectGeneratorTool tool = new WayangProjectGeneratorTool();
        tool.projectGenerator = svc.projectGenerator;
        Map<String, Object> out = tool.execute(
                Map.of("intent", (Object) "create an example"), Map.of())
                .await().indefinitely();
        assertNotNull(out);
        assertTrue(out.containsKey("project") || out.containsKey("success"));
    }
}
