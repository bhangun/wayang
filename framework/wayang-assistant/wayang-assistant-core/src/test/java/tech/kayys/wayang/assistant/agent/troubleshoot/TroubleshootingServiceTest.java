package tech.kayys.wayang.assistant.agent.troubleshoot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.assistant.agent.WayangAssistantService.ErrorTroubleshootingResult;
import tech.kayys.wayang.assistant.knowledge.KnowledgeSearchService;

import static org.junit.jupiter.api.Assertions.*;

public class TroubleshootingServiceTest {

    private TroubleshootingService service;

    @BeforeEach
    void setUp() {
        service = new TroubleshootingService();
        service.searchService = new KnowledgeSearchService();
    }

    @Test
    void troubleshootError_nullPointerAdvice() {
        ErrorTroubleshootingResult result = service.troubleshootError("NullPointerException at line 42");
        assertNotNull(result);
        assertTrue(result.getAdvice().contains("Null Pointer"));
    }

    @Test
    void troubleshootError_connectionAdvice() {
        ErrorTroubleshootingResult result = service.troubleshootError("Connection refused to database");
        assertNotNull(result);
        assertTrue(result.getAdvice().contains("Connection"));
    }
}
