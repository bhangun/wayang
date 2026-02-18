package tech.kayys.wayang.agent.basic;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.sdk.executor.core.Executor;
import tech.kayys.wayang.agent.AgentType;
import tech.kayys.wayang.agent.executor.AbstractAgentExecutor;

import java.util.Map;

/**
 * Executor for Basic (Common) Agents.
 * Responsible for general purpose tasks.
 */
@ApplicationScoped
@Executor(type = "agent-common", version = "1.0.0")
public class BasicAgentExecutor extends AbstractAgentExecutor {

    @Override
    protected AgentType getAgentType() {
        return AgentType.COMMON;
    }

    @Override
    protected Uni<NodeExecutionResult> doExecute(NodeExecutionTask task) {
        logger.info("Basic agent executing task: {}", task.nodeId());

        return Uni.createFrom().item(() -> {
            Map<String, Object> output = Map.of(
                    "status", "COMPLETED",
                    "result", "Task completed successfully by Basic Agent (stub)");
            return createSuccessResult(task, output);
        });
    }
}
