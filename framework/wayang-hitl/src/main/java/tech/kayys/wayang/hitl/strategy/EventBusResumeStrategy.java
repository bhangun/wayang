package tech.kayys.wayang.hitl.strategy;

import tech.kayys.wayang.agent.Agent;

/**
 * An event-driven strategy: Automatically resumes the agent when the task is approved.
 * This assumes the agent supports a `.resume(taskId)` or similar method.
 */
public class EventBusResumeStrategy implements ResumeStrategy {

    @Override
    public void onTaskApproved(Agent agent, String taskId) {
        // In a real implementation, this might call agent.resume(taskId)
        // or re-submit the workflow to Gamelan.
        System.out.println("Auto-resuming agent for task: " + taskId);
    }
}
