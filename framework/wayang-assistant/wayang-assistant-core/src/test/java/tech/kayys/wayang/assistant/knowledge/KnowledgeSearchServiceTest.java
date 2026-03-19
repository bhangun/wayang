package tech.kayys.wayang.assistant.knowledge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.assistant.agent.WayangAssistantService.DocSearchResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeSearchServiceTest {

    private KnowledgeSearchService service;

    @BeforeEach
    void setUp() {
        service = new KnowledgeSearchService();
        service.knowledgeRegistry = null;
    }

    @Test
    void searchDocumentation_returnsResultsEvenIfPathMissing() {
        // By default it might warn but should return a "not available" placeholder instead of crashing
        List<DocSearchResult> results = service.searchDocumentation("test query");
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }
}
