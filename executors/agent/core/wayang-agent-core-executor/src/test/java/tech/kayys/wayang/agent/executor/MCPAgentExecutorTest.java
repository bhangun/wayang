package tech.kayys.wayang.agent.executor;

import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tech.kayys.gamelan.engine.execution.ExecutionToken;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.engine.node.NodeId;
import tech.kayys.gamelan.engine.run.RetryPolicy;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;
import tech.kayys.wayang.agent.AgentType;
import tech.kayys.wayang.agent.a2a.AgentCommunicationProtocol;
import tech.kayys.wayang.agent.a2a.impl.DefaultAgentCommunicationProtocol;
import tech.kayys.wayang.agent.a2a.model.AgentMessage;
import tech.kayys.wayang.agent.mcp.MCPPromptProvider;
import tech.kayys.wayang.agent.mcp.MCPResourceProvider;
import tech.kayys.wayang.agent.mcp.MCPToolProvider;
import tech.kayys.wayang.agent.model.ToolResult;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MCPAgentExecutorTest {

    @Test
    void readResourceDelegatesToResourceProvider() {
        MCPResourceProvider resourceProvider = mock(MCPResourceProvider.class);
        when(resourceProvider.readResource("resource://project/spec"))
                .thenReturn(Uni.createFrom().item("project-spec"));

        TestMCPAgentExecutor executor = new TestMCPAgentExecutor();
        executor.resourceProvider = resourceProvider;

        String content = executor.invokeReadResource("resource://project/spec")
                .await().indefinitely();

        assertEquals("project-spec", content);
    }

    @Test
    void renderPromptDelegatesToPromptProvider() {
        MCPPromptProvider promptProvider = mock(MCPPromptProvider.class);
        Map<String, Object> arguments = Map.of("topic", "planning");
        when(promptProvider.renderPrompt("planner", arguments))
                .thenReturn(Uni.createFrom().item("plan prompt"));

        TestMCPAgentExecutor executor = new TestMCPAgentExecutor();
        executor.promptProvider = promptProvider;

        String rendered = executor.invokeRenderPrompt("planner", arguments)
                .await().indefinitely();

        assertEquals("plan prompt", rendered);
    }

    @Test
    void executeMcpToolReturnsToolOutput() {
        MCPToolProvider toolProvider = mock(MCPToolProvider.class);
        Map<String, Object> arguments = Map.of("q", "ai");
        when(toolProvider.executeTool("web_search", arguments))
                .thenReturn(Uni.createFrom().item(ToolResult.success("1", "web_search", "ok")));

        TestMCPAgentExecutor executor = new TestMCPAgentExecutor();
        executor.toolProvider = toolProvider;

        Object output = executor.invokeExecuteMCPTool("web_search", arguments)
                .await().indefinitely();

        assertEquals("ok", output);
    }

    @Test
    void sendToAgentBuildsExpectedMessage() {
        AgentCommunicationProtocol protocol = mock(AgentCommunicationProtocol.class);
        when(protocol.sendMessage(any())).thenReturn(Uni.createFrom().voidItem());

        TestMCPAgentExecutor executor = new TestMCPAgentExecutor();
        executor.communicationProtocol = protocol;
        Map<String, Object> payload = Map.of("step", "evaluate");

        executor.invokeSendToAgent("evaluator-agent", "review", payload)
                .await().indefinitely();

        ArgumentCaptor<AgentMessage> messageCaptor = ArgumentCaptor.forClass(AgentMessage.class);
        verify(protocol).sendMessage(messageCaptor.capture());
        AgentMessage message = messageCaptor.getValue();

        assertEquals("test-mcp-agent", message.getFromAgentId());
        assertEquals("evaluator-agent", message.getToAgentId());
        assertEquals("review", message.getSubject());
        assertEquals(payload, message.getPayload());
        assertEquals(AgentMessage.MessageType.REQUEST, message.getType());
    }

    @Test
    void broadcastToAgentsBuildsBroadcastMessage() {
        AgentCommunicationProtocol protocol = mock(AgentCommunicationProtocol.class);
        when(protocol.broadcast(any(), any())).thenReturn(Uni.createFrom().voidItem());

        TestMCPAgentExecutor executor = new TestMCPAgentExecutor();
        executor.communicationProtocol = protocol;
        List<String> targets = List.of("planner-agent", "coder-agent");
        Map<String, Object> payload = Map.of("signal", "sync");

        executor.invokeBroadcastToAgents(targets, "coordination", payload)
                .await().indefinitely();

        ArgumentCaptor<AgentMessage> messageCaptor = ArgumentCaptor.forClass(AgentMessage.class);
        verify(protocol).broadcast(messageCaptor.capture(), eq(targets));
        AgentMessage message = messageCaptor.getValue();

        assertEquals("test-mcp-agent", message.getFromAgentId());
        assertEquals("coordination", message.getSubject());
        assertEquals(payload, message.getPayload());
        assertEquals(AgentMessage.MessageType.BROADCAST, message.getType());
        assertNull(message.getToAgentId());
    }

    @Test
    void doExecuteRegistersAndUnregistersWithDefaultProtocol() {
        DefaultAgentCommunicationProtocol protocol = new DefaultAgentCommunicationProtocol();
        NodeExecutionResult expectedResult = mock(NodeExecutionResult.class);
        NodeExecutionTask task = createTask();

        TestMCPAgentExecutor executor = new TestMCPAgentExecutor();
        executor.communicationProtocol = protocol;
        executor.executionResult = expectedResult;

        NodeExecutionResult actualResult = executor.invokeDoExecute(task)
                .await().indefinitely();

        assertSame(expectedResult, actualResult);
        assertTrue(executor.sawSelfDuringExecution);
        List<String> remainingAgents = protocol.discoverAgents().await().indefinitely();
        assertFalse(remainingAgents.contains("test-mcp-agent"));
    }

    @Test
    void doExecuteSkipsRegistrationWhenProtocolIsNotDefault() {
        AgentCommunicationProtocol protocol = mock(AgentCommunicationProtocol.class);
        NodeExecutionResult expectedResult = mock(NodeExecutionResult.class);
        NodeExecutionTask task = createTask();

        TestMCPAgentExecutor executor = new TestMCPAgentExecutor();
        executor.communicationProtocol = protocol;
        executor.executionResult = expectedResult;

        NodeExecutionResult actualResult = executor.invokeDoExecute(task)
                .await().indefinitely();

        assertSame(expectedResult, actualResult);
        verifyNoInteractions(protocol);
    }

    static class TestMCPAgentExecutor extends MCPAgentExecutor {
        private NodeExecutionResult executionResult;
        private boolean sawSelfDuringExecution;

        Uni<String> invokeReadResource(String uri) {
            return super.readResource(uri);
        }

        Uni<String> invokeRenderPrompt(String promptName, Map<String, Object> arguments) {
            return super.renderPrompt(promptName, arguments);
        }

        Uni<Object> invokeExecuteMCPTool(String toolName, Map<String, Object> arguments) {
            return super.executeMCPTool(toolName, arguments);
        }

        Uni<Void> invokeSendToAgent(String targetAgentId, String subject, Map<String, Object> payload) {
            return super.sendToAgent(targetAgentId, subject, payload);
        }

        Uni<Void> invokeBroadcastToAgents(List<String> targetAgentIds, String subject, Map<String, Object> payload) {
            return super.broadcastToAgents(targetAgentIds, subject, payload);
        }

        Uni<NodeExecutionResult> invokeDoExecute(NodeExecutionTask task) {
            return super.doExecute(task);
        }

        @Override
        protected String getAgentId() {
            return "test-mcp-agent";
        }

        @Override
        protected Uni<NodeExecutionResult> executeWithMCP(NodeExecutionTask task) {
            if (communicationProtocol instanceof DefaultAgentCommunicationProtocol defaultProtocol) {
                return defaultProtocol.discoverAgents()
                        .invoke(agents -> sawSelfDuringExecution = agents.contains(getAgentId()))
                        .replaceWith(executionResult);
            }
            return Uni.createFrom().item(executionResult);
        }

        @Override
        protected AgentType getAgentType() {
            return AgentType.COMMON;
        }

        @Override
        public String getExecutorType() {
            return "mcp-test-executor";
        }
    }

    private NodeExecutionTask createTask() {
        WorkflowRunId runId = new WorkflowRunId(UUID.randomUUID().toString());
        NodeId nodeId = new NodeId("mcp-agent-test-node");
        int attempt = 1;
        return new NodeExecutionTask(
                runId,
                nodeId,
                attempt,
                ExecutionToken.create(runId, nodeId, attempt, Duration.ofMinutes(5)),
                Map.of(),
                RetryPolicy.none());
    }
}
