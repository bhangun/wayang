package tech.kayys.wayang.agent.plan;

import tech.kayys.wayang.agent.WayangAgentListener;
import tech.kayys.wayang.agent.react.BaseReActAgent;
import tech.kayys.wayang.spi.plugin.PluginRegistry;

/**
 * A Multi-phase Plan-and-Solve Agent.
 */
public class PlanAndSolveAgent extends BaseReActAgent {

    @Override
    public void send(String userInput, WayangAgentListener listener) {
        // Phase 1: Planner
        listener.onTextDelta("[Phase 1] Planning: " + userInput + "\n");
        
        // Phase 2: Executor
        listener.onTextDelta("[Phase 2] Executing steps...\n");

        // Phase 3: Reviewer
        listener.onTextDelta("[Phase 3] Reviewing results...\n");
        
        listener.onDone("stop");
    }

}
