package tech.kayys.wayang.assistant.agent.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.assistant.agent.ConversationSession;

import static org.junit.jupiter.api.Assertions.*;

public class AssistantSessionManagerTest {

    private AssistantSessionManager manager;

    @BeforeEach
    void setUp() {
        manager = new AssistantSessionManager();
        manager.maxSessions = 3; // Small cap for testing eviction
    }

    @Test
    void getOrCreateSession_createsNewSession() {
        ConversationSession session = manager.getOrCreateSession("session-1");
        assertNotNull(session);
        assertEquals("session-1", session.getSessionId());
        assertEquals(1, manager.activeSessionCount());
    }

    @Test
    void getOrCreateSession_returnsExistingSession() {
        ConversationSession s1 = manager.getOrCreateSession("session-1");
        ConversationSession s2 = manager.getOrCreateSession("session-1");
        assertSame(s1, s2);
        assertEquals(1, manager.activeSessionCount());
    }

    @Test
    void eviction_removesOldestSession() throws InterruptedException {
        manager.getOrCreateSession("s1");
        Thread.sleep(10);
        manager.getOrCreateSession("s2");
        Thread.sleep(10);
        manager.getOrCreateSession("s3");
        
        assertEquals(3, manager.activeSessionCount());
        
        // Trigger eviction
        manager.getOrCreateSession("s4");
        
        assertEquals(3, manager.activeSessionCount());
        assertNull(manager.getSessions().get("s1"), "s1 should have been evicted");
        assertNotNull(manager.getSessions().get("s4"));
    }

    @Test
    void deleteSession_removesSession() {
        manager.getOrCreateSession("s1");
        assertTrue(manager.deleteSession("s1"));
        assertEquals(0, manager.activeSessionCount());
    }
}
