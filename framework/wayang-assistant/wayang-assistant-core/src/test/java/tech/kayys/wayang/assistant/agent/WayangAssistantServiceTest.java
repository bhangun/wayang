package tech.kayys.wayang.assistant.agent;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import tech.kayys.wayang.assistant.agent.tool.WayangProjectGeneratorTool;

public class WayangAssistantServiceTest {

    @Test
    public void testSearchFallback() {
        WayangAssistantService svc = new WayangAssistantService();
        List<WayangAssistantService.DocSearchResult> results = svc.searchDocumentation("Wayang");
        assertNotNull(results);
        // Should return at least one result (fallback may produce message if docs not found)
        assertTrue(!results.isEmpty(), "Should return at least one result");
    }

    @Test
    public void testProjectGeneration() {
        WayangAssistantService svc = new WayangAssistantService();
        var desc = svc.generateProject("build a simple workflow");
        assertNotNull(desc);
        assertNotNull(desc.getName());
        assertTrue(desc.getCapabilities().contains("agent") || desc.getCapabilities().contains("workflow"));
    }

    @Test
    public void testTroubleshoot() {
        WayangAssistantService svc = new WayangAssistantService();
        WayangAssistantService.ErrorTroubleshootingResult result = svc.troubleshootError("nonexistent error");
        assertNotNull(result);
        assertNotNull(result.getAdvice());
    }

    @Test
    public void testProjectGeneratorTool() {
        WayangAssistantService svc = new WayangAssistantService();
        WayangProjectGeneratorTool tool = new WayangProjectGeneratorTool();
        // inject service manually (package-private field)
        tool.service = svc;

        var input = Map.of("intent", (Object)"create an example");
        var uni = tool.execute(input, Map.of());
        Map<String, Object> out = uni.await().indefinitely();
        assertNotNull(out);
        assertTrue(out.containsKey("project") || out.containsKey("success"));
    }
}
