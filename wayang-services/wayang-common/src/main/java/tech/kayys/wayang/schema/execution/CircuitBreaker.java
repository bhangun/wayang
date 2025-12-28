package tech.kayys.wayang.schema.execution;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class CircuitBreaker {
    private Boolean enabled = true;
    private Integer failureThreshold = 5;
    private Integer successThreshold = 2;
    private Integer timeoutMs = 60000;
    private Integer windowMs = 600000;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getFailureThreshold() {
        return failureThreshold;
    }

    public void setFailureThreshold(Integer failureThreshold) {
        if (failureThreshold != null && failureThreshold < 0) {
            throw new IllegalArgumentException("Failure threshold cannot be negative");
        }
        this.failureThreshold = failureThreshold;
    }

    public Integer getSuccessThreshold() {
        return successThreshold;
    }

    public void setSuccessThreshold(Integer successThreshold) {
        if (successThreshold != null && successThreshold < 0) {
            throw new IllegalArgumentException("Success threshold cannot be negative");
        }
        this.successThreshold = successThreshold;
    }

    public Integer getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(Integer timeoutMs) {
        if (timeoutMs != null && timeoutMs < 0) {
            throw new IllegalArgumentException("Timeout cannot be negative");
        }
        this.timeoutMs = timeoutMs;
    }

    public Integer getWindowMs() {
        return windowMs;
    }

    public void setWindowMs(Integer windowMs) {
        if (windowMs != null && windowMs < 0) {
            throw new IllegalArgumentException("Window duration cannot be negative");
        }
        this.windowMs = windowMs;
    }
}
