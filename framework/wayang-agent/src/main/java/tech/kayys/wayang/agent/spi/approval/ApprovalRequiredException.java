package tech.kayys.wayang.agent.spi.approval;

/**
 * Thrown when an agent attempts to execute a tool that requires human approval,
 * or when a guardrail intercepts the execution and escalates to a human.
 */
public class ApprovalRequiredException extends RuntimeException {

    private final String taskId;

    public ApprovalRequiredException(String message, String taskId) {
        super(message);
        this.taskId = taskId;
    }

    /**
     * @return The ID of the HumanTask created to approve this action.
     */
    public String getTaskId() {
        return taskId;
    }
}
