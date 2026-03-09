package tech.kayys.wayang.eip.executor;

import org.junit.jupiter.api.Test;
import tech.kayys.gamelan.engine.execution.ExecutionToken;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionStatus;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.engine.node.NodeId;
import tech.kayys.gamelan.engine.run.RetryPolicy;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DebugPrintExecutorTest {

  @Test
  void executePassesThroughMessageAndAddsDebugMetadata() {
    DebugPrintExecutor executor = new DebugPrintExecutor();
    NodeExecutionTask task = createTask(Map.of(
        "prefix", "TRACE NODE",
        "level", "debug",
        "message", Map.of("id", "abc-123")));

    NodeExecutionResult result = executor.execute(task)
        .await().atMost(Duration.ofSeconds(3));

    assertEquals(NodeExecutionStatus.COMPLETED, result.status());
    assertEquals(Map.of("id", "abc-123"), result.output().get("message"));
    assertEquals(true, result.output().get("debugPrinted"));
    assertEquals("debug", result.output().get("debugLevel"));
    assertEquals("TRACE NODE", result.output().get("debugPrefix"));
    assertTrue(result.output().containsKey("debugPrintedAt"));
  }

  @Test
  void executeFallsBackToPayloadFieldWhenMessageMissing() {
    DebugPrintExecutor executor = new DebugPrintExecutor();
    NodeExecutionTask task = createTask(Map.of(
        "payload", "from-upstream"));

    NodeExecutionResult result = executor.execute(task)
        .await().atMost(Duration.ofSeconds(3));

    assertEquals(NodeExecutionStatus.COMPLETED, result.status());
    assertEquals("from-upstream", result.output().get("message"));
  }

  private NodeExecutionTask createTask(Map<String, Object> context) {
    WorkflowRunId runId = new WorkflowRunId(UUID.randomUUID().toString());
    NodeId nodeId = new NodeId("debug-print-test-node");
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
