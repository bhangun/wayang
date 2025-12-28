package tech.kayys.wayang.agent.service;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.agent.dto.AgentMetrics;
import tech.kayys.wayang.agent.dto.AgentType;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class AgentMetricsCollector {

    private final Map<String, AgentMetricData> agentMetrics = new ConcurrentHashMap<>();
    private final AtomicLong totalExecutions = new AtomicLong(0);
    private final AtomicLong totalAgentsCreated = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);

    // Helper class to store the aggregated metrics
    private static class AgentMetricData {
        private long totalExecutions;
        private long successfulExecutions;
        private long failedExecutions;
        private long totalExecutionTimeMs;
        private long maxExecutionTimeMs = Long.MIN_VALUE;
        private long minExecutionTimeMs = Long.MAX_VALUE;
        private long totalTokensUsed;
        private double totalCost;

        public AgentMetricData(long totalExecutions, long successfulExecutions, long failedExecutions,
                              long totalExecutionTimeMs, long maxExecutionTimeMs, long minExecutionTimeMs,
                              long totalTokensUsed, double totalCost) {
            this.totalExecutions = totalExecutions;
            this.successfulExecutions = successfulExecutions;
            this.failedExecutions = failedExecutions;
            this.totalExecutionTimeMs = totalExecutionTimeMs;
            this.maxExecutionTimeMs = maxExecutionTimeMs;
            this.minExecutionTimeMs = minExecutionTimeMs;
            this.totalTokensUsed = totalTokensUsed;
            this.totalCost = totalCost;
        }

        public AgentMetricData() {
            // default constructor for new instances
        }

        public AgentMetrics toAgentMetrics() {
            long avgExecutionTimeMs = totalExecutions > 0 ? totalExecutionTimeMs / totalExecutions : 0;
            return new AgentMetrics(avgExecutionTimeMs, (int)totalTokensUsed, totalCost);
        }
    }

    public Uni<Void> recordAgentCreated(AgentType agentType) {
        totalAgentsCreated.incrementAndGet();
        Log.infof("Agent created: %s, Total: %d", agentType, totalAgentsCreated.get());
        return Uni.createFrom().voidItem();
    }

    public Uni<Void> recordExecution(String agentId, tech.kayys.wayang.agent.dto.ExecutionMode executionMode) {
        totalExecutions.incrementAndGet();
        Log.infof("Execution recorded for agent: %s, Mode: %s, Total: %d",
                  agentId, executionMode, totalExecutions.get());

        // Update agent-specific metrics
        AgentMetricData metrics = agentMetrics.computeIfAbsent(agentId, k -> new AgentMetricData());

        // Update the metrics (simplified - in real app would be more detailed)
        metrics.totalExecutions++;
        // For now, we just increment the execution count

        return Uni.createFrom().voidItem();
    }

    public Uni<AgentMetrics> getAgentMetrics(String agentId) {
        AgentMetricData metricData = agentMetrics.getOrDefault(agentId, new AgentMetricData());
        AgentMetrics metrics = metricData.toAgentMetrics();
        Log.infof("Retrieved metrics for agent: %s", agentId);
        return Uni.createFrom().item(metrics);
    }

    public Uni<Long> getTotalExecutions() {
        return Uni.createFrom().item(totalExecutions.get());
    }

    public Uni<Long> getTotalAgentsCreated() {
        return Uni.createFrom().item(totalAgentsCreated.get());
    }

    public Uni<Void> recordError(String agentId, String errorMessage) {
        totalErrors.incrementAndGet();
        Log.errorf("Error recorded for agent %s: %s", agentId, errorMessage);

        // Update agent metrics
        AgentMetricData metrics = agentMetrics.computeIfAbsent(agentId, k -> new AgentMetricData());

        metrics.failedExecutions++;

        return Uni.createFrom().voidItem();
    }

    public Uni<AgentMetrics> getOverallMetrics() {
        // For overall metrics, we'll return an average-like metric
        // We use 0s for duration and tokens since those are per-agent
        AgentMetrics overall = new AgentMetrics(
            0, // avg execution time - set to 0 for overall
            0, // tokens used - set to 0 for overall
            0.0 // total cost - set to 0.0 for overall
        );

        Log.info("Retrieved overall metrics");
        return Uni.createFrom().item(overall);
    }

    public Uni<Void> recordExecutionTime(String agentId, long executionTimeMs) {
        AgentMetricData metrics = agentMetrics.computeIfAbsent(agentId, k -> new AgentMetricData());

        // Update execution time metrics
        metrics.totalExecutionTimeMs += executionTimeMs;
        metrics.maxExecutionTimeMs = Math.max(metrics.maxExecutionTimeMs, executionTimeMs);
        if (metrics.minExecutionTimeMs == Long.MAX_VALUE) {
            metrics.minExecutionTimeMs = executionTimeMs;
        } else {
            metrics.minExecutionTimeMs = Math.min(metrics.minExecutionTimeMs, executionTimeMs);
        }

        return Uni.createFrom().voidItem();
    }
}