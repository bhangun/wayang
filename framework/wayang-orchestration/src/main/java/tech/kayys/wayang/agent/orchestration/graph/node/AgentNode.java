package tech.kayys.wayang.agent.orchestration.graph.node;

import tech.kayys.wayang.spi.agent.Agent;
import tech.kayys.wayang.agent.orchestration.graph.state.GraphState;
import tech.kayys.wayang.agent.orchestration.graph.state.StateUpdate;

import java.util.HashMap;
import java.util.Map;

/**
 * A specialized GraphNode that delegates execution to a standard Wayang Agent.
 * This acts as the bridge between standalone agents and the Multi-Agent graph.
 */
public class AgentNode implements GraphNode {

    private final Agent agent;
    private final String inputKey;
    private final String outputKey;

    /**
     * @param agent the Wayang Agent to execute
     * @param inputKey the key in the GraphState containing the agent's prompt/input
     * @param outputKey the key in the StateUpdate where the agent's response should be written
     */
    public AgentNode(Agent agent, String inputKey, String outputKey) {
        this.agent = agent;
        this.inputKey = inputKey;
        this.outputKey = outputKey;
    }

    @Override
    public StateUpdate execute(GraphState state) {
        String input = state.get(inputKey);
        if (input == null) {
            input = "No input provided."; // Or throw an exception depending on strictness
        }

        // Delegate execution to the underlying ReAct / PlanAndSolve agent
        try {
            Object response = agent.process(input);
            return new StateUpdate()
                    .put(outputKey, response);
        } catch (tech.kayys.wayang.agent.spi.approval.ApprovalRequiredException e) {
            // Rethrow immediately so the graph engine pauses and waits for HITL
            throw new RuntimeException("Execution paused for HITL approval", e);
        } catch (Exception e) {
            throw new RuntimeException("Agent execution failed", e);
        }
    }
}
