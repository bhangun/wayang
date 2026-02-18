package tech.kayys.wayang.agent.executor;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.wayang.agent.AgentType;
import tech.kayys.wayang.memory.spi.AgentMemory;
import tech.kayys.wayang.memory.spi.MemoryEntry;
import tech.kayys.wayang.tool.spi.ToolExecutor;

import java.time.Instant;
import java.util.Map;

@QuarkusTest
public class AgentExecutorIntegrationTest {

    @InjectMock
    AgentMemory agentMemory;

    @InjectMock
    ToolExecutor toolExecutor;

    @Inject
    TestAgentExecutor executor;

    @Test
    public void testMemoryStorageAfterExecute() {
        // Mock task
        NodeExecutionTask task = Mockito.mock(NodeExecutionTask.class);
        Mockito.when(task.context()).thenReturn(Map.of("agentId", "agent-123"));
        NodeExecutionResult result = Mockito.mock(NodeExecutionResult.class);
        Mockito.when(result.status()).thenReturn(tech.kayys.gamelan.engine.node.NodeExecutionStatus.COMPLETED);
        Mockito.when(result.output()).thenReturn(Map.of("response", "Task completed"));

        // Setup memory mock
        Mockito.when(agentMemory.store(Mockito.eq("agent-123"), Mockito.any()))
                .thenReturn(Uni.createFrom().voidItem());

        // Execute logic
        executor.afterExecute(task, result)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .assertCompleted();

        // Verify memory storage
        ArgumentCaptor<MemoryEntry> captor = ArgumentCaptor.forClass(MemoryEntry.class);
        Mockito.verify(agentMemory).store(Mockito.eq("agent-123"), captor.capture());
        Assertions.assertEquals("Task completed", captor.getValue().content());
    }

    @Test
    public void testToolExecution() {
        // Verify tool executor is injected and usable
        Mockito.when(toolExecutor.execute(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Uni.createFrom().item(Map.of("status", "ok")));

        Map<String, Object> result = executor.callTool("my-tool", Map.of())
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        Assertions.assertEquals("ok", result.get("status"));
        Mockito.verify(toolExecutor).execute(Mockito.eq("my-tool"), Mockito.any(), Mockito.any());
    }

    // Concrete implementation for testing
    @ApplicationScoped
    public static class TestAgentExecutor extends AbstractAgentExecutor {

        @Override
        protected Uni<NodeExecutionResult> doExecute(NodeExecutionTask task) {
            return Uni.createFrom().nullItem();
        }

        @Override
        protected AgentType getAgentType() {
            return AgentType.BASIC;
        }

        @Override
        public String getExecutorType() {
            return "test-agent";
        }

        // Helper to test tool injection
        public Uni<Map<String, Object>> callTool(String toolId, Map<String, Object> args) {
            if (this.toolExecutor == null) {
                return Uni.createFrom().failure(new IllegalStateException("ToolExecutor not injected"));
            }
            return this.toolExecutor.execute(toolId, args, Map.of());
        }
    }
}
