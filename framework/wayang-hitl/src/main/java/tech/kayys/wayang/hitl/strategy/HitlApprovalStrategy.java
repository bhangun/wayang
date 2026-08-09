package tech.kayys.wayang.hitl.strategy;

import tech.kayys.wayang.agent.Agent;
import tech.kayys.wayang.agent.spi.approval.ApprovalRequiredException;
import tech.kayys.wayang.agent.spi.approval.ApprovalStrategy;
import tech.kayys.wayang.hitl.domain.HumanTask;
import tech.kayys.wayang.hitl.domain.HumanTaskId;
import tech.kayys.wayang.hitl.domain.HumanTaskStatus;
import tech.kayys.wayang.tool.ToolInvocation;
import tech.kayys.wayang.tool.capability.Capability;

import java.util.UUID;

/**
 * Intercepts tool calls and routes them to the HITL system if they require approval.
 */
public class HitlApprovalStrategy implements ApprovalStrategy {

    private final ResumeStrategy resumeStrategy;

    public HitlApprovalStrategy(ResumeStrategy resumeStrategy) {
        this.resumeStrategy = resumeStrategy;
    }

    public HitlApprovalStrategy() {
        this.resumeStrategy = new ManualResumeStrategy();
    }

    @Override
    public void requestApproval(Agent agent, ToolInvocation invocation) throws ApprovalRequiredException {
        
        boolean requiresApproval = false;
        
        // Check if any capability of the tool inherently requires approval
        for (Capability capability : invocation.tool().capabilities()) {
            if (capability.requiresApproval()) {
                requiresApproval = true;
                break;
            }
        }

        if (requiresApproval) {
            // 1. Create a HumanTask
            HumanTaskId taskId = new HumanTaskId(UUID.randomUUID().toString());
            
            // In a real app, this would be persisted to the database via a Repository
            HumanTask task = new HumanTask(taskId, "tool_approval", "Approve tool execution: " + invocation.tool().name());
            task.updateStatus(HumanTaskStatus.CREATED, "System");

            // 2. Suspend Agent Execution
            throw new ApprovalRequiredException(
                "Tool " + invocation.tool().name() + " requires human approval.", 
                taskId.value()
            );
        }
    }
}
