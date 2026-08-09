package tech.kayys.wayang.hitl.strategy;

import tech.kayys.wayang.agent.Agent;

/**
 * Default strategy: The agent does nothing automatically. 
 * The client application is responsible for calling agent.resume() manually.
 */
public class ManualResumeStrategy implements ResumeStrategy {

    @Override
    public void onTaskApproved(Agent agent, String taskId) {
        // No-op. Wait for client to resume.
    }
}
