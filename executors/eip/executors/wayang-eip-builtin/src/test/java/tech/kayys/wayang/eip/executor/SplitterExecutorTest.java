package tech.kayys.wayang.eip.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.junit.jupiter.api.Test;
import tech.kayys.gamelan.engine.execution.ExecutionToken;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.engine.node.NodeExecutionStatus;
import tech.kayys.gamelan.engine.node.NodeId;
import tech.kayys.gamelan.engine.run.RetryPolicy;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;
import tech.kayys.wayang.eip.strategy.SplitStrategyFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SplitterExecutorTest {

    @Test
    void executeSplitsListUsingFixedStrategy() {
        SplitterExecutor executor = new SplitterExecutor();
        executor.objectMapper = lenientMapper();
        executor.strategyFactory = createFactory();

        NodeExecutionTask task = createTask(Map.of(
                "strategy", "fixed",
                "batchSize", 2,
                "message", List.of("a", "b", "c", "d", "e")));

        NodeExecutionResult result = executor.execute(task)
                .await().atMost(Duration.ofSeconds(3));

        assertEquals(NodeExecutionStatus.COMPLETED, result.status());
        assertEquals(3, result.output().get("count"));
        assertEquals(3, result.output().get("partCount"));
        assertTrue(result.output().containsKey("splitAt"));
    }

    @Test
    void executeSplitsStringUsingDelimiterStrategy() {
        SplitterExecutor executor = new SplitterExecutor();
        executor.objectMapper = lenientMapper();
        executor.strategyFactory = createFactory();

        NodeExecutionTask task = createTask(Map.of(
                "strategy", "delimiter",
                "expression", ",",
                "batchSize", 10,
                "message", "x,y,z"));

        NodeExecutionResult result = executor.execute(task)
                .await().atMost(Duration.ofSeconds(3));

        assertEquals(NodeExecutionStatus.COMPLETED, result.status());
        assertEquals(3, result.output().get("count"));
        assertEquals(List.of("x", "y", "z"), result.output().get("items"));
    }

    private SplitStrategyFactory createFactory() {
        SplitStrategyFactory factory = new SplitStrategyFactory();
        try {
            var init = SplitStrategyFactory.class.getDeclaredMethod("init");
            init.setAccessible(true);
            init.invoke(factory);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SplitStrategyFactory for test", e);
        }
        return factory;
    }

    private ObjectMapper lenientMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private NodeExecutionTask createTask(Map<String, Object> context) {
        WorkflowRunId runId = new WorkflowRunId(UUID.randomUUID().toString());
        NodeId nodeId = new NodeId("splitter-test-node");
        int attempt = 1;
        return new NodeExecutionTask(
                runId,
                nodeId,
                attempt,
                ExecutionToken.create(runId, nodeId, attempt, Duration.ofMinutes(5)),
                context,
                RetryPolicy.none());
    }
}
