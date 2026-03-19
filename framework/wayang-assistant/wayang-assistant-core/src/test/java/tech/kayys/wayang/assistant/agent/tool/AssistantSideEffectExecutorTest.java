package tech.kayys.wayang.assistant.agent.tool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.assistant.agent.ConversationSession;

import static org.junit.jupiter.api.Assertions.*;

public class AssistantSideEffectExecutorTest {

    private AssistantSideEffectExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new AssistantSideEffectExecutor();
        executor.assistantTools = null; // Mock/Null for pure logic test if any
    }

    @Test
    void executeSlackReport_gracefulWithNoTools() {
        // Should not throw NPE if tools are empty
        assertDoesNotThrow(() -> executor.executeSlackReport("test bug", new ConversationSession("sess")));
    }
}
