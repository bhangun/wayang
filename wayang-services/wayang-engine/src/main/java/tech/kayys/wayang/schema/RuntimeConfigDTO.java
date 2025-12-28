package tech.kayys.wayang.schema;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.quarkus.runtime.annotations.RegisterForReflection;
import tech.kayys.wayang.schema.execution.RetryPolicy;
import tech.kayys.wayang.schema.governance.ResourceProfile;

/**
 * RuntimeConfigDTO - Runtime configuration
 */
@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuntimeConfigDTO {

    private String mode = "sync"; // sync, async, stream
    private RetryPolicy retryPolicy;
    private Integer timeoutMs;
    private ResourceProfile resourceProfile;

    // Getters and setters...
    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public Integer getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(Integer timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public ResourceProfile getResourceProfile() {
        return resourceProfile;
    }

    public void setResourceProfile(ResourceProfile resourceProfile) {
        this.resourceProfile = resourceProfile;
    }
}
