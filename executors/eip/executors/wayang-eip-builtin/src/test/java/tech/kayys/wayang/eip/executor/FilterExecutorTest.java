package tech.kayys.wayang.eip.executor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import tech.kayys.gamelan.engine.execution.ExecutionToken;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.engine.node.NodeExecutionStatus;
import tech.kayys.gamelan.engine.node.NodeId;
import tech.kayys.gamelan.engine.run.RetryPolicy;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;
import tech.kayys.wayang.eip.service.FilterEvaluator;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FilterExecutorTest {

    @Test
    void executeReturnsPassedWhenExpressionMatches() {
        FilterExecutor executor = new FilterExecutor();
        executor.objectMapper = lenientMapper();
        executor.filterEvaluator = new FilterEvaluator();

        NodeExecutionTask task = createTask(Map.of(
                "expression", "true",
                "inverse", false,
                "message", "hello"));

        NodeExecutionResult result = executor.execute(task)
                .await().atMost(Duration.ofSeconds(3));

        assertEquals(NodeExecutionStatus.COMPLETED, result.status());
        assertEquals(true, result.output().get("passed"));
        assertEquals(false, result.output().get("filtered"));
        assertEquals("Message too large", result.output().get("reason"));
    }

    @Test
    void executeRoutesWhenFilteredOut() {
        FilterExecutor executor = new FilterExecutor();
        executor.objectMapper = lenientMapper();
        executor.filterEvaluator = new FilterEvaluator();

        NodeExecutionTask task = createTask(Map.of(
                "expression", "false",
                "inverse", false,
                "onFilteredRoute", "dead-letter",
                "message", "hello"));

        NodeExecutionResult result = executor.execute(task)
                .await().atMost(Duration.ofSeconds(3));

        assertEquals(NodeExecutionStatus.COMPLETED, result.status());
        assertEquals(false, result.output().get("passed"));
        assertEquals(true, result.output().get("filtered"));
        assertEquals("dead-letter", result.output().get("nextRoute"));
    }

    private ObjectMapper lenientMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private NodeExecutionTask createTask(Map<String, Object> context) {
        WorkflowRunId runId = new WorkflowRunId(UUID.randomUUID().toString());
        NodeId nodeId = new NodeId("filter-test-node");
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
