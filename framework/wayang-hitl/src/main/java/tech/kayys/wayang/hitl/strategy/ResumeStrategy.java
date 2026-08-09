package tech.kayys.wayang.hitl.strategy;

import tech.kayys.wayang.agent.Agent;

/**
 * Defines how an agent should resume execution after an ApprovalRequiredException was thrown.
 */
public interface ResumeStrategy {
    
    /**
     * Called when a HumanTask is approved.
     * 
     * @param agent the agent waiting for approval
     * @param taskId the ID of the approved task
     */
    void onTaskApproved(Agent agent, String taskId);
}
