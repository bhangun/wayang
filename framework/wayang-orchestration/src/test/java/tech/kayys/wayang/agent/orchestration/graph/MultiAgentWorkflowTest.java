package tech.kayys.wayang.agent.orchestration.graph;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.agent.orchestration.graph.checkpoint.InMemoryCheckpointStrategy;
import tech.kayys.wayang.agent.orchestration.graph.edge.ConditionalEdge;
import tech.kayys.wayang.agent.orchestration.graph.engine.CompiledWorkflow;
import tech.kayys.wayang.agent.orchestration.graph.engine.WorkflowGraph;
import tech.kayys.wayang.agent.orchestration.graph.node.GraphNode;
import tech.kayys.wayang.agent.orchestration.graph.state.AppendReducer;
import tech.kayys.wayang.agent.orchestration.graph.state.GraphState;
import tech.kayys.wayang.agent.orchestration.graph.state.StateUpdate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MultiAgentWorkflowTest {

    @Test
    public void testWorkflowExecutionAndReducers() {
        
        // 1. Define Dummy Nodes
        GraphNode researcher = state -> {
            Integer loops = state.get("loop_count");
            if (loops == null) loops = 0;
            return new StateUpdate()
                    .put("messages", List.of("Research iteration " + loops))
                    .put("loop_count", loops + 1);
        };

        GraphNode writer = state -> {
            return new StateUpdate()
                    .put("messages", List.of("Draft written based on research."));
        };

        // 2. Define Conditional Edge
        ConditionalEdge shouldContinue = state -> {
            Integer loops = state.get("loop_count");
            if (loops != null && loops < 2) {
                return "researcher"; // Loop back
            }
            return CompiledWorkflow.END;
        };

        // 3. Build Graph
        CompiledWorkflow workflow = new WorkflowGraph()
                .addNode("researcher", researcher)
                .addNode("writer", writer)
                .addEdge("researcher", "writer")
                .addConditionalEdge("writer", shouldContinue)
                .setEntryPoint("researcher")
                .withCheckpointStrategy(new InMemoryCheckpointStrategy())
                .compile();

        // 4. Initialize State with AppendReducer
        GraphState initialState = new GraphState()
                .withReducer("messages", new AppendReducer<String>());

        // 5. Invoke Workflow
        GraphState finalState = workflow.invoke(initialState, "thread-123");

        // 6. Verify Reducer combined the messages
        List<String> messages = finalState.get("messages");
        assertNotNull(messages);
        assertEquals(4, messages.size());
        assertEquals("Research iteration 0", messages.get(0));
        assertEquals("Draft written based on research.", messages.get(1));
        assertEquals("Research iteration 1", messages.get(2));
        assertEquals("Draft written based on research.", messages.get(3));
    }
}
