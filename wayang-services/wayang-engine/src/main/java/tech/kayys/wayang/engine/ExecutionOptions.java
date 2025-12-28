package tech.kayys.wayang.engine;

/**
 * Common execution options for all workflow types.
 */
public class ExecutionOptions {
    private String priority = "NORMAL"; // LOW, NORMAL, HIGH, CRITICAL
    private Long timeoutMs = 300000L; // 5 minutes default
    private boolean dryRun = false;
    private boolean enableProvenance = true;
    private boolean enableTelemetry = true;
    private String executionMode = "async"; // async, sync, scheduled

    public static ExecutionOptions defaults() {
        return new ExecutionOptions();
    }

    public ExecutionOptions withPriority(String priority) {
        this.priority = priority;
        return this;
    }

    public ExecutionOptions withTimeout(long timeoutMs) {
        this.timeoutMs = timeoutMs;
        return this;
    }

    public ExecutionOptions asDryRun() {
        this.dryRun = true;
        return this;
    }

    // Getters and setters
    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(Long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public boolean isEnableProvenance() {
        return enableProvenance;
    }

    public void setEnableProvenance(boolean enable) {
        this.enableProvenance = enable;
    }

    public boolean isEnableTelemetry() {
        return enableTelemetry;
    }

    public void setEnableTelemetry(boolean enable) {
        this.enableTelemetry = enable;
    }

    public String getExecutionMode() {
        return executionMode;
    }

    public void setExecutionMode(String mode) {
        this.executionMode = mode;
    }

    public long timeoutMs() {
        return timeoutMs;
    }
}