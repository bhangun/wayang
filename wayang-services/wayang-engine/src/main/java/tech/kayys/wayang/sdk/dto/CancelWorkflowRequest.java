package tech.kayys.wayang.sdk.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * SDK DTO for Cancel Workflow Request.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CancelWorkflowRequest {
    private String reason;
    private boolean force;

    public CancelWorkflowRequest() {
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }
}
