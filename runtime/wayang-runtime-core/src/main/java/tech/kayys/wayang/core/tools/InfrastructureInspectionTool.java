package tech.kayys.wayang.agent.core.tools;

import tech.kayys.wayang.agent.core.metrics.AgentMetricsCollector;
import tech.kayys.wayang.tools.spi.Tool;
import tech.kayys.wayang.tools.spi.ToolContext;
import tech.kayys.wayang.tools.spi.ToolResult;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@ApplicationScoped
public class InfrastructureInspectionTool implements Tool {

    private volatile boolean enabled = true;

    @Inject
    AgentMetricsCollector metricsCollector;

    @Override
    public String id() { return "tool.inspect_infrastructure"; }

    @Override
    public String name() { return "inspect_infrastructure"; }

    @Override
    public String description() {
        return "Queries internal Wayang and Gollek infrastructure metrics (RAM, CPU, tokens, latency). Can be used to make system-aware orchestration decisions.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(), // no required params
            "required", List.of()
        );
    }

    @Override
    public ToolResult execute(Map<String, Object> params, ToolContext context) {
        if (!enabled) {
            return ToolResult.error("Infrastructure Inspection Tool is currently disabled.");
        }

        try {
            Map<String, Object> metrics = new HashMap<>();

            // 1. Fetch OS Metrics (CPU)
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            metrics.put("system_load_average", osBean.getSystemLoadAverage());
            metrics.put("available_processors", osBean.getAvailableProcessors());

            // 2. Fetch JVM Memory Metrics (RAM)
            MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heap = memBean.getHeapMemoryUsage();
            metrics.put("heap_used_mb", heap.getUsed() / (1024 * 1024));
            metrics.put("heap_max_mb", heap.getMax() / (1024 * 1024));

            // 3. Fetch Wayang Token & Latency Metrics
            if (metricsCollector != null) {
                metrics.putAll(metricsCollector.getMetricsSummary());
            }

            return ToolResult.success(metrics.toString());
        } catch (Exception e) {
            return ToolResult.error("Failed to query infrastructure metrics: " + e.getMessage());
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
