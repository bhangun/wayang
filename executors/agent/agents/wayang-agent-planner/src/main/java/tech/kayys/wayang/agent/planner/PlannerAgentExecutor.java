package tech.kayys.wayang.agent.planner;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.sdk.executor.core.Executor;
import tech.kayys.wayang.agent.AgentType;
import tech.kayys.wayang.agent.executor.AbstractAgentExecutor;

import java.util.Map;

/**
 * Executor for Planner Agents.
 * Responsible for breaking down goals into tasks.
 */
@ApplicationScoped
@Executor(type = "agent-planner", version = "1.0.0")
public class PlannerAgentExecutor extends AbstractAgentExecutor {

    @Override
    protected AgentType getAgentType() {
        return AgentType.PLANNER;
    }

    @Override
    protected Uni<NodeExecutionResult> doExecute(NodeExecutionTask task) {
        logger.info("Planner agent executing task: {}", task.nodeId());

        return Uni.createFrom().item(() -> {
            Map<String, Object> output = Map.of(
                    "status", "COMPLETED",
                    "plan", "stub-generated-plan",
                    "result", "Plan generated successfully (stub)");
            return createSuccessResult(task, output);
        });
    }
}
