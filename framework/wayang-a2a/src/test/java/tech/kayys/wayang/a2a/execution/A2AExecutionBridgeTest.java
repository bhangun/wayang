package tech.kayys.wayang.a2a.execution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.communication.api.*;
import tech.kayys.wayang.communication.capability.CapabilityId;
import tech.kayys.wayang.communication.message.MessagePayload;
import tech.kayys.wayang.communication.protocol.ProtocolContext;
import tech.kayys.wayang.communication.task.TaskId;
import tech.kayys.wayang.communication.task.TaskStatus;
import tech.kayys.wayang.security.propagation.SecurityContextSnapshot;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

import static org.junit.jupiter.api.Assertions.*;

class A2AExecutionBridgeTest {

    private DefaultA2ATaskRegistry taskRegistry;

    @BeforeEach
    void setUp() {
        taskRegistry = new DefaultA2ATaskRegistry();
    }

    @Test
    void testA2AExecutionBridgeExecute() {
        AgentCommunicator mockCommunicator = new AgentCommunicator() {
            @Override
            public java.util.concurrent.CompletionStage<AgentResponse> send(AgentRequest request, ProtocolContext context) {
                return CompletableFuture.completedFuture(
                        AgentResponse.success(MessagePayload.text("echo: " + request.payload().value()))
                );
            }

            @Override
            public AgentTask submit(AgentRequest request, ProtocolContext context) { return null; }

            @Override
            public Flow.Publisher<AgentEvent> stream(AgentRequest request, ProtocolContext context) { return null; }
        };

        DefaultA2AExecutionBridge bridge = new DefaultA2AExecutionBridge(mockCommunicator, taskRegistry);

        AgentRef target = AgentRef.local("echo-agent", "Echo Agent", "runtime-1");
        AgentRequest agentReq = A2ARequestMapper.toAgentRequest(target, "echo", "hello world", Map.of());
        SecurityContextSnapshot sec = A2ASecurityMapper.fromA2A("user-alice", "Alice", "tenant-1", Map.of());

        A2AExecutionRequest a2aReq = A2AExecutionRequest.of("a2a-task-1", agentReq, sec);

        AgentResponse response = bridge.execute(a2aReq).toCompletableFuture().join();
        assertNotNull(response);
        assertTrue(response.success());
        assertEquals("echo: hello world", response.payload().value());
    }

    @Test
    void testA2AExecutionBridgeSubmitAndRegistryMapping() {
        AgentCommunicator mockCommunicator = new AgentCommunicator() {
            @Override
            public java.util.concurrent.CompletionStage<AgentResponse> send(AgentRequest request, ProtocolContext context) {
                return CompletableFuture.completedFuture(AgentResponse.success(MessagePayload.text("ok")));
            }

            @Override
            public AgentTask submit(AgentRequest request, ProtocolContext context) {
                return new AgentTask() {
                    @Override public TaskId id() { return TaskId.of("agent-task-99"); }
                    @Override public TaskStatus status() { return TaskStatus.WORKING; }
                    @Override public AgentRef agent() { return request.target(); }
                    @Override public CompletableFuture<AgentResult> result() {
                        return CompletableFuture.completedFuture(AgentResult.success(MessagePayload.text("done")));
                    }
                    @Override public Flow.Publisher<AgentEvent> events() { return null; }
                    @Override public CompletableFuture<Void> cancel() { return CompletableFuture.completedFuture(null); }
                };
            }

            @Override
            public Flow.Publisher<AgentEvent> stream(AgentRequest request, ProtocolContext context) { return null; }
        };

        DefaultA2AExecutionBridge bridge = new DefaultA2AExecutionBridge(mockCommunicator, taskRegistry);

        AgentRef target = AgentRef.local("worker", "Worker Agent", "runtime-1");
        AgentRequest agentReq = A2ARequestMapper.toAgentRequest(target, "work", "payload", Map.of());
        SecurityContextSnapshot sec = A2ASecurityMapper.fromA2A("agent-master", "Master", "tenant-1", Map.of());

        A2AExecutionRequest a2aReq = A2AExecutionRequest.of("a2a-task-42", agentReq, sec);

        A2ATaskExecution taskExec = bridge.submit(a2aReq).toCompletableFuture().join();
        assertNotNull(taskExec);
        assertEquals("a2a-task-42", taskExec.a2aTaskId());
        assertNotNull(taskExec.executionId());

        // Verify task registry mapping
        assertTrue(taskRegistry.findByA2aTaskId("a2a-task-42").isPresent());
        assertTrue(taskRegistry.findByExecutionId(taskExec.executionId()).isPresent());
        assertEquals(taskExec.executionId(), taskRegistry.findByA2aTaskId("a2a-task-42").get().executionId());
    }
}
