package tech.kayys.wayang.agent.evaluator;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.sdk.executor.core.Executor;
import tech.kayys.wayang.agent.AgentType;
import tech.kayys.wayang.agent.executor.AbstractAgentExecutor;

import java.util.Map;
import tech.kayys.wayang.agent.schema.EvaluatorAgentConfig;

/**
 * Executor for Evaluator Agents.
 * Responsible for evaluating outputs and providing feedback.
 */
@ApplicationScoped
@Executor(executorType = "agent-evaluator", version = "1.0.0")
public class EvaluatorAgentExecutor extends AbstractAgentExecutor {

    @Override
    public String getExecutorType() {
        return "agent-evaluator";
    }

    @Override
    protected AgentType getAgentType() {
        return AgentType.EVALUATOR;
    }

    @Override
    protected Uni<NodeExecutionResult> doExecute(NodeExecutionTask task) {
        logger.info("Evaluator agent executing task: {}", task.nodeId());

        EvaluatorAgentConfig config = objectMapper.convertValue(task.context(), EvaluatorAgentConfig.class);

        return Uni.createFrom().item(() -> {
            Map<String, Object> output = Map.of(
                    "status", "COMPLETED",
                    "evaluation", "stub-score-100",
                    "result", "Evaluation completed successfully (stub)");
            return createSuccessResult(task, output);
        });
    }
}
