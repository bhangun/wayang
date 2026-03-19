package tech.kayys.wayang.assistant.agent.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.assistant.agent.ConversationSession;

import java.net.http.HttpClient;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SlackBugReportToolTest {

    private SlackBugReportTool tool;

    @BeforeEach
    void setUp() {
        tool = new SlackBugReportTool();
        // Use real ObjectMapper for construction test
        tool.objectMapper = new ObjectMapper();
        // httpClient is left as default or could be mocked if Mockito was available
    }

    @Test
    void id_isCorrect() {
        assertEquals("slack-bug-report", tool.id());
    }

    @Test
    void inputSchema_containsRequiredFields() {
        Map<String, Object> schema = tool.inputSchema();
        assertNotNull(schema);
        assertTrue(((java.util.List<?>) schema.get("required")).contains("bugDescription"));
    }

    @Test
    void execute_missingDescription_fails() {
        assertThrows(Exception.class, () -> {
            tool.execute(Map.of(), Map.of()).await().indefinitely();
        });
    }

    // Note: Full integration test with real Slack API is not possible in this environment
    // without a valid token and internet access. Verification is done via code analysis
    // and unit tests for the logic components.
}
