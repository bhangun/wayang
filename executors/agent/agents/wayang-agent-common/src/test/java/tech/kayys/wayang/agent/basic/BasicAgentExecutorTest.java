package tech.kayys.wayang.agent.basic;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BasicAgentExecutorTest {

    @Test
    void executeReturnsCompletedStubResult() {
        TestBasicAgentExecutor executor = new TestBasicAgentExecutor();
        executor.setObjectMapper(lenientMapper());

        NodeExecutionTask task = createTask(Map.of(
                "model", "gpt-4o-mini",
                "temperature", 0.3));

        NodeExecutionResult result = executor.execute(task)
                .await().atMost(Duration.ofSeconds(3));

        assertEquals(NodeExecutionStatus.COMPLETED, result.status());
        assertEquals("COMPLETED", result.output().get("status"));
        assertEquals("Task completed successfully by Basic Agent (stub)", result.output().get("result"));
    }

    @Test
    void canHandleMatchesAgentType() {
        BasicAgentExecutor executor = new TestBasicAgentExecutor();

        NodeExecutionTask matching = createTask(Map.of("agentType", "agent-basic"));
        NodeExecutionTask different = createTask(Map.of("agentType", "agent-coder"));
        NodeExecutionTask missing = createTask(Map.of("model", "x"));

        assertTrue(executor.canHandle(matching));
        assertFalse(executor.canHandle(different));
        assertFalse(executor.canHandle(missing));
    }

    private ObjectMapper lenientMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private NodeExecutionTask createTask(Map<String, Object> context) {
        WorkflowRunId runId = new WorkflowRunId(UUID.randomUUID().toString());
        NodeId nodeId = new NodeId("basic-test-node");
        int attempt = 1;
        return new NodeExecutionTask(
                runId,
                nodeId,
                attempt,
                ExecutionToken.create(runId, nodeId, attempt, Duration.ofMinutes(5)),
                context,
                RetryPolicy.none());
    }

    private static final class TestBasicAgentExecutor extends BasicAgentExecutor {
        private void setObjectMapper(ObjectMapper mapper) {
            this.objectMapper = mapper;
        }
    }
}
