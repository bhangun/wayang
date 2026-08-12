package tech.kayys.wayang.a2a.tool;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tech.kayys.wayang.a2a.api.A2AClient;
import tech.kayys.wayang.a2a.model.A2AMessage;
import tech.kayys.wayang.a2a.model.A2ATask;
import tech.kayys.wayang.a2a.model.A2ATaskStatus;
import tech.kayys.wayang.tool.ToolContext;
import tech.kayys.wayang.tool.ToolInvocation;
import tech.kayys.wayang.tool.ToolResult;

@ExtendWith(MockitoExtension.class)
class A2AClientToolTest {

    @Mock
    private A2AClient mockClient;
    
    @Mock
    private ToolInvocation mockInvocation;
    
    @Mock
    private ToolContext mockContext;

    @Test
    void testExecute() throws Exception {
        A2AClientTool tool = new A2AClientTool(mockClient, "target-agent-123");
        
        when(mockInvocation.arguments()).thenReturn(Map.of("message", "Test message"));
        
        A2ATask mockTask = new A2ATask(
            "task-123",
            null,
            A2ATaskStatus.RUNNING,
            java.util.List.of(),
            Map.of()
        );
        when(mockClient.sendMessage(any(A2AMessage.class))).thenReturn(CompletableFuture.completedFuture(mockTask));
        
        ToolResult result = tool.execute(mockInvocation, mockContext).get();
        
        assertTrue(result.isSuccess());
        assertEquals("task-123", result.getOutputs().get("taskId"));
        assertEquals("RUNNING", result.getOutputs().get("status"));
        
        verify(mockClient).sendMessage(argThat(msg -> 
            msg.role() == A2AMessage.Role.USER
        ));
    }
}
