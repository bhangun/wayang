package tech.kayys.wayang.guardrails.strategy;

import tech.kayys.wayang.agent.Agent;
import tech.kayys.wayang.agent.spi.approval.ApprovalRequiredException;
import tech.kayys.wayang.guardrails.ExecutionResult;
import tech.kayys.wayang.tool.ToolInvocation;

import java.util.UUID;

/**
 * Escalates a guardrail violation to a Human-in-the-Loop task for manual manager override.
 * Requires the `wayang-hitl` module to be present on the classpath.
 */
public class HitlEscalationFallbackStrategy implements GuardrailFallbackStrategy {

    @Override
    public void handleViolation(Agent agent, ToolInvocation invocation, ExecutionResult result) throws ApprovalRequiredException {
        String taskId = UUID.randomUUID().toString();
        
        // Internally, this would use the wayang-hitl HumanTask repository to persist the task.
        // It escalates the guardrail violation so a human can review and potentially override it.
        
        throw new ApprovalRequiredException(
            "Guardrail violation requires manager override: " + result.toString(),
            taskId
        );
    }
}
