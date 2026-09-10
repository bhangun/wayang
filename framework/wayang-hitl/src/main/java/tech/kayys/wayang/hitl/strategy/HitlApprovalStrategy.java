package tech.kayys.wayang.hitl.strategy;

import tech.kayys.wayang.agent.Agent;
import tech.kayys.wayang.agent.spi.approval.ApprovalRequiredException;
import tech.kayys.wayang.agent.spi.approval.ApprovalStrategy;
import tech.kayys.wayang.hitl.domain.HumanTask;
import tech.kayys.wayang.hitl.domain.HumanTaskId;
import tech.kayys.wayang.hitl.domain.HumanTaskStatus;
import tech.kayys.wayang.tool.Tool;
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
        String toolName = invocation != null ? invocation.name() : "unknown";
        
        // Check if any capability of the tool inherently requires approval
        if (agent != null && agent.tools() != null) {
            Tool matchedTool = agent.tools().stream()
                .filter(t -> t.descriptor() != null && toolName.equals(t.descriptor().name()))
                .findFirst()
                .orElse(null);
            if (matchedTool != null && matchedTool.toolCapabilities() != null) {
                for (Capability capability : matchedTool.toolCapabilities()) {
                    if (capability.requiresApproval()) {
                        requiresApproval = true;
                        break;
                    }
                }
            }
        }

        if (requiresApproval) {
            // 1. Create a HumanTask
            HumanTaskId taskId = new HumanTaskId(UUID.randomUUID().toString());
            
            HumanTask task = HumanTask.builder()
                .workflowRunId("system")
                .nodeId("tool-approval")
                .tenantId("default")
                .taskType("tool_approval")
                .title("Approve tool execution: " + toolName)
                .description("Requires human approval to proceed.")
                .build();
            // The task status update is now handled by the builder or domain logic if needed.

            // 2. Suspend Agent Execution
            throw new ApprovalRequiredException(
                "Tool " + toolName + " requires human approval.", 
                taskId.value()
            );
        }
    }
}
