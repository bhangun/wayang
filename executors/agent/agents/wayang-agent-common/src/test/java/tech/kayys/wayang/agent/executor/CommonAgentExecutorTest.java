package tech.kayys.wayang.agent.executor;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.wayang.memory.spi.AgentMemory;
import tech.kayys.wayang.tool.spi.ToolExecutor;

import java.util.Map;

@QuarkusTest
public class CommonAgentExecutorTest {

    @Inject
    CommonAgentExecutor executor;

    @InjectMock
    AgentMemory agentMemory;

    @InjectMock
    ToolExecutor toolExecutor;

    @Test
    public void testExecuteDataProcessor() {
        // Setup task
        NodeExecutionTask task = Mockito.mock(NodeExecutionTask.class);
        Mockito.when(task.nodeId()).thenReturn("node-1");
        Mockito.when(task.runId()).thenReturn("run-1");
        Mockito.when(task.attempt()).thenReturn(1);
        Mockito.when(task.context()).thenReturn(Map.of(
                "agentType", "common-agent",
                "specialization", "data-processor",
                "parameters", Map.of("key", "value"),
                "agentId", "agent-common-1"));

        // Mock memory to succeed (required by AbstractAgentExecutor.afterExecute)
        Mockito.when(agentMemory.store(Mockito.any(), Mockito.any()))
                .thenReturn(Uni.createFrom().voidItem());

        // Execute
        NodeExecutionResult result = executor.execute(task)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        // Verify
        Assertions.assertEquals(tech.kayys.gamelan.engine.node.NodeExecutionStatus.COMPLETED, result.status());
        Map<String, Object> output = result.output();
        Assertions.assertEquals("completed", output.get("status"));
        Assertions.assertEquals("data-processor", output.get("specialization"));

        // Verify internal logic result is present
        Map<String, Object> internalResult = (Map<String, Object>) output.get("result");
        Assertions.assertTrue((Boolean) internalResult.get("processed"));

        // Verify memory storage was called (via AbstractAgentExecutor.afterExecute)
        Mockito.verify(agentMemory).store(Mockito.eq("agent-common-1"), Mockito.any());
    }

    @Test
    public void testCanHandle() {
        NodeExecutionTask task = Mockito.mock(NodeExecutionTask.class);
        Mockito.when(task.context()).thenReturn(Map.of("agentType", "common-agent"));
        Assertions.assertTrue(executor.canHandle(task));

        Mockito.when(task.context()).thenReturn(Map.of("agentType", "other-agent"));
        Assertions.assertFalse(executor.canHandle(task));
    }
}
