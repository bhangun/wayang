package tech.kayys.wayang.agent.orchestration.graph;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.agent.orchestration.graph.checkpoint.InMemoryCheckpointStrategy;
import tech.kayys.wayang.agent.orchestration.graph.engine.CompiledWorkflow;
import tech.kayys.wayang.agent.orchestration.graph.engine.WorkflowGraph;
import tech.kayys.wayang.agent.orchestration.graph.node.GraphNode;
import tech.kayys.wayang.agent.orchestration.graph.state.GraphState;
import tech.kayys.wayang.agent.orchestration.graph.state.StateUpdate;
import tech.kayys.wayang.agent.spi.approval.ApprovalRequiredException;

import static org.junit.jupiter.api.Assertions.*;

public class GraphResumeTest {

    private boolean isApproved = false;

    @Test
    public void testPauseAndResume() {
        
        // A node that requires HITL approval on its first run
        GraphNode hitlNode = state -> {
            if (!isApproved) {
                throw new RuntimeException("Paused", new ApprovalRequiredException("Need manager approval", "task-123"));
            }
            return new StateUpdate().put("result", "approved_action_done");
        };

        CompiledWorkflow workflow = new WorkflowGraph()
                .addNode("action", hitlNode)
                .setEntryPoint("action")
                .withCheckpointStrategy(new InMemoryCheckpointStrategy())
                .compile();

        GraphState initialState = new GraphState();
        initialState.apply(new tech.kayys.wayang.agent.orchestration.graph.state.StateUpdate()
                .put("input", "do something dangerous"));

        String threadId = "thread-xyz";

        // 1. Initial Invoke - should throw exception (Pause)
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            workflow.invoke(initialState, threadId);
        });
        
        assertTrue(ex.getCause() instanceof ApprovalRequiredException);
        assertEquals("task-123", ((ApprovalRequiredException) ex.getCause()).getTaskId());

        // 2. Simulate HITL approval
        isApproved = true;

        // 3. Resume - should succeed
        GraphState finalState = workflow.invoke(new GraphState(), threadId);
        
        assertEquals("approved_action_done", finalState.get("result"));
    }
}
