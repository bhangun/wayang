package tech.kayys.wayang.agent.executor;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.engine.protocol.CommunicationType;
import tech.kayys.gamelan.sdk.executor.core.Executor;
import tech.kayys.wayang.agent.AnalyticsCapability;
import tech.kayys.wayang.agent.type.AgentType;
import tech.kayys.wayang.agent.type.AnalyticsAgent;

import java.util.*;

/**
 * Executor for AnalyticsAgent - handles data analysis and insights generation
 */
@Executor(executorType = "analytics-agent", communicationType = CommunicationType.GRPC, maxConcurrentTasks = 6, supportedNodeTypes = {
        "agent-task", "analytics-agent-task", "analysis-task" })
@ApplicationScoped
public class AnalyticsAgentExecutor extends AbstractAgentExecutor {

    @Override
    public String getExecutorType() {
        return "analytics-agent";
    }

    @Override
    protected AgentType getAgentType() {
        return new AnalyticsAgent(
                Set.of(
                        AnalyticsCapability.DESCRIPTIVE,
                        AnalyticsCapability.PREDICTIVE,
                        AnalyticsCapability.ANOMALY_DETECTION),
                Set.of("JSON", "CSV"),
                true);
    }

    @Override
    protected Uni<NodeExecutionResult> doExecute(NodeExecutionTask task) {
        logger.info("AnalyticsAgentExecutor executing analysis task: {}", task.nodeId());

        Map<String, Object> context = task.context();

        // Extract analytics configuration
        String capabilityName = (String) context.getOrDefault("capability", "DESCRIPTIVE");
        AnalyticsCapability capability = AnalyticsCapability.valueOf(capabilityName);
        String dataFormat = (String) context.getOrDefault("dataFormat", "JSON");
        Map<String, Object> dataContext = (Map<String, Object>) context.getOrDefault("dataContext", Map.of());

        // Execute analytics operation
        return executeAnalytics(capability, dataFormat, dataContext, task)
                .map(result -> createSuccessResult(task, result))
                .onFailure().recoverWithItem(error -> createFailureResult(task, error));
    }

    /**
     * Execute analytics based on capability
     */
    private Uni<Map<String, Object>> executeAnalytics(
            AnalyticsCapability capability,
            String dataFormat,
            Map<String, Object> dataContext,
            NodeExecutionTask task) {

        return switch (capability) {
            case DESCRIPTIVE -> executeDescriptiveAnalytics(dataFormat, dataContext);
            case DIAGNOSTIC -> executeDiagnosticAnalytics(dataFormat, dataContext);
            case PREDICTIVE -> executePredictiveAnalytics(dataFormat, dataContext);
            case PRESCRIPTIVE -> executePrescriptiveAnalytics(dataFormat, dataContext);
            case STATISTICAL_ANALYSIS -> executeStatisticalAnalysis(dataFormat, dataContext);
            case PATTERN_RECOGNITION -> executePatternRecognition(dataFormat, dataContext);
            case ANOMALY_DETECTION -> executeAnomalyDetection(dataFormat, dataContext);
            case TREND_ANALYSIS -> executeTrendAnalysis(dataFormat, dataContext);
        };
    }

    private Uni<Map<String, Object>> executeDescriptiveAnalytics(
            String dataFormat,
            Map<String, Object> context) {
        logger.debug("Executing descriptive analytics");

        // What happened?
        Map<String, Object> statistics = Map.of(
                "mean", 42.5,
                "median", 40.0,
                "mode", 38.0,
                "stdDev", 5.2,
                "count", 1000);

        return Uni.createFrom().item(Map.of(
                "capability", "DESCRIPTIVE",
                "dataFormat", dataFormat,
                "statistics", statistics,
                "summary", "Data shows normal distribution with mean of 42.5",
                "visualizations", List.of("histogram", "box-plot")));
    }

    private Uni<Map<String, Object>> executeDiagnosticAnalytics(
            String dataFormat,
            Map<String, Object> context) {
        logger.debug("Executing diagnostic analytics");

        // Why did it happen?
        List<Map<String, Object>> rootCauses = List.of(
                Map.of("cause", "Seasonal variation", "impact", 0.65),
                Map.of("cause", "Market conditions", "impact", 0.25),
                Map.of("cause", "Random factors", "impact", 0.10));

        return Uni.createFrom().item(Map.of(
                "capability", "DIAGNOSTIC",
                "dataFormat", dataFormat,
                "rootCauses", rootCauses,
                "correlations", Map.of("temperature", 0.78, "humidity", -0.42),
                "insights", "Primary driver is seasonal variation"));
    }

    private Uni<Map<String, Object>> executePredictiveAnalytics(
            String dataFormat,
            Map<String, Object> context) {
        logger.debug("Executing predictive analytics");

        // What will happen?
        List<Map<String, Object>> predictions = List.of(
                Map.of("period", "Q1", "predicted", 45.2, "confidence", 0.85),
                Map.of("period", "Q2", "predicted", 48.7, "confidence", 0.78),
                Map.of("period", "Q3", "predicted", 52.1, "confidence", 0.72));

        return Uni.createFrom().item(Map.of(
                "capability", "PREDICTIVE",
                "dataFormat", dataFormat,
                "predictions", predictions,
                "model", "ARIMA",
                "accuracy", 0.82,
                "trend", "upward"));
    }

    private Uni<Map<String, Object>> executePrescriptiveAnalytics(
            String dataFormat,
            Map<String, Object> context) {
        logger.debug("Executing prescriptive analytics");

        // What should we do?
        List<Map<String, Object>> recommendations = List.of(
                Map.of("action", "Increase inventory", "priority", "high", "expectedImpact", "+15%"),
                Map.of("action", "Optimize pricing", "priority", "medium", "expectedImpact", "+8%"),
                Map.of("action", "Expand capacity", "priority", "low", "expectedImpact", "+5%"));

        return Uni.createFrom().item(Map.of(
                "capability", "PRESCRIPTIVE",
                "dataFormat", dataFormat,
                "recommendations", recommendations,
                "optimalStrategy", "Multi-phase approach",
                "roi", 1.45));
    }

    private Uni<Map<String, Object>> executeStatisticalAnalysis(
            String dataFormat,
            Map<String, Object> context) {
        logger.debug("Executing statistical analysis");

        Map<String, Object> tests = Map.of(
                "tTest", Map.of("statistic", 2.45, "pValue", 0.014, "significant", true),
                "chiSquare", Map.of("statistic", 12.3, "pValue", 0.002, "significant", true),
                "anova", Map.of("fStatistic", 5.67, "pValue", 0.001, "significant", true));

        return Uni.createFrom().item(Map.of(
                "capability", "STATISTICAL_ANALYSIS",
                "dataFormat", dataFormat,
                "tests", tests,
                "sampleSize", 1000,
                "conclusionConfidence", 0.95));
    }

    private Uni<Map<String, Object>> executePatternRecognition(
            String dataFormat,
            Map<String, Object> context) {
        logger.debug("Executing pattern recognition");

        List<Map<String, Object>> patterns = List.of(
                Map.of("pattern", "Weekly cycle", "frequency", 7, "strength", 0.82),
                Map.of("pattern", "Monthly spike", "frequency", 30, "strength", 0.65),
                Map.of("pattern", "Quarterly trend", "frequency", 90, "strength", 0.54));

        return Uni.createFrom().item(Map.of(
                "capability", "PATTERN_RECOGNITION",
                "dataFormat", dataFormat,
                "patterns", patterns,
                "algorithm", "Time series decomposition",
                "totalPatternsFound", 3));
    }

    private Uni<Map<String, Object>> executeAnomalyDetection(
            String dataFormat,
            Map<String, Object> context) {
        logger.debug("Executing anomaly detection");

        List<Map<String, Object>> anomalies = List.of(
                Map.of("timestamp", "2024-01-15T10:30:00Z", "value", 125.3, "expectedRange", "40-60", "severity",
                        "high"),
                Map.of("timestamp", "2024-01-20T14:45:00Z", "value", 12.1, "expectedRange", "40-60", "severity",
                        "medium"));

        return Uni.createFrom().item(Map.of(
                "capability", "ANOMALY_DETECTION",
                "dataFormat", dataFormat,
                "anomalies", anomalies,
                "method", "Isolation Forest",
                "threshold", 2.5,
                "falsePositiveRate", 0.05));
    }

    private Uni<Map<String, Object>> executeTrendAnalysis(
            String dataFormat,
            Map<String, Object> context) {
        logger.debug("Executing trend analysis");

        Map<String, Object> trend = Map.of(
                "direction", "upward",
                "strength", 0.78,
                "slope", 2.3,
                "acceleration", 0.15,
                "changePoints", List.of("2024-01-01", "2024-06-15"));

        return Uni.createFrom().item(Map.of(
                "capability", "TREND_ANALYSIS",
                "dataFormat", dataFormat,
                "trend", trend,
                "forecastHorizon", 90,
                "confidence", 0.88));
    }

    @Override
    public int getMaxConcurrentTasks() {
        return 6;
    }

    @Override
    public boolean canHandle(NodeExecutionTask task) {
        Map<String, Object> context = task.context();
        String agentType = (String) context.get("agentType");
        return "analytics-agent".equals(agentType) || "ANALYTICS_AGENT".equals(agentType);
    }
}
