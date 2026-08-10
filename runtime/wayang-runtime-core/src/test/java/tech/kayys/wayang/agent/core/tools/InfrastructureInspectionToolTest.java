package tech.kayys.wayang.agent.core.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tech.kayys.wayang.agent.core.metrics.AgentMetricsCollector;
import tech.kayys.wayang.tools.spi.ToolResult;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class InfrastructureInspectionToolTest {

    private InfrastructureInspectionTool tool;
    private AgentMetricsCollector mockMetricsCollector;

    @BeforeEach
    public void setup() {
        tool = new InfrastructureInspectionTool();
        mockMetricsCollector = Mockito.mock(AgentMetricsCollector.class);
        tool.metricsCollector = mockMetricsCollector;
    }

    @Test
    public void testToolDisabled() {
        tool.setEnabled(false);
        ToolResult result = tool.execute(Map.of(), null);
        
        assertFalse(result.isSuccess());
        assertEquals("Infrastructure Inspection Tool is currently disabled.", result.getResult());
    }

    @Test
    public void testToolEnabledAndMetricsFetched() {
        tool.setEnabled(true);

        Mockito.when(mockMetricsCollector.getMetricsSummary()).thenReturn(Map.of(
            "active_runs", 5,
            "total_tokens", 1000
        ));

        ToolResult result = tool.execute(Map.of(), null);
        
        assertTrue(result.isSuccess());
        String output = result.getResult().toString();
        
        // Assert it contains system metrics
        assertTrue(output.contains("system_load_average"));
        assertTrue(output.contains("heap_used_mb"));
        
        // Assert it contains agent metrics
        assertTrue(output.contains("active_runs=5"));
        assertTrue(output.contains("total_tokens=1000"));
    }
}
