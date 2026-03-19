package tech.kayys.wayang.assistant.agent.tool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class WayangLogReaderToolTest {

    private WayangLogReaderTool tool;

    @BeforeEach
    void setUp() {
        tool = new WayangLogReaderTool();
    }

    @Test
    void execute_fileNotFoundReturnsSuccessFalse(@TempDir Path tempDir) {
        tool.serverLogPath = tempDir.resolve("non-existent.log").toString();
        
        Map<String, Object> result = tool.execute(Map.of("logType", "SERVER"), Map.of())
                .await().indefinitely();
                
        assertFalse((Boolean) result.get("success"));
        assertTrue(((String) result.get("error")).contains("Log file not found"));
    }

    @Test
    void execute_readsLastLines(@TempDir Path tempDir) throws IOException {
        Path logFile = tempDir.resolve("test.log");
        List<String> content = List.of("Line 1", "Line 2", "Line 3", "Line 4", "Line 5");
        Files.write(logFile, content);
        
        tool.serverLogPath = logFile.toString();
        
        // Read last 3 lines
        Map<String, Object> result = tool.execute(Map.of("logType", "SERVER", "maxLines", 3), Map.of())
                .await().indefinitely();
                
        assertTrue((Boolean) result.get("success"));
        assertEquals(3, result.get("lineCount"));
        assertEquals("Line 3\nLine 4\nLine 5", result.get("content"));
    }
}
