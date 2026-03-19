package tech.kayys.wayang.assistant.agent.intent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.assistant.agent.ConversationSession;

import static org.junit.jupiter.api.Assertions.*;

public class AssistantIntentClassifierTest {

    private AssistantIntentClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new AssistantIntentClassifier();
        classifier.inferenceService = null; // Test keyword fallback
    }

    @Test
    void detectIntent_bugKeywords() {
        String intent = classifier.detectIntent("The system crashed with an error", null);
        assertEquals("BUG", intent);
    }

    @Test
    void detectIntent_analyticsKeywords() {
        String intent = classifier.detectIntent("Show me the current metrics", null);
        assertEquals("ANALYTICS", intent);
    }

    @Test
    void detectIntent_troubleshootKeywords() {
        String intent = classifier.detectIntent("Help me troubleshoot this issue", null);
        assertEquals("TROUBLESHOOT", intent);
    }

    @Test
    void detectIntent_searchKeywords() {
        String intent = classifier.detectIntent("How to use RAG?", null);
        assertEquals("SEARCH", intent);
    }

    @Test
    void detectIntent_fallbackToChat() {
        String intent = classifier.detectIntent("Hello, how are you?", null);
        assertEquals("CHAT", intent);
    }
}
