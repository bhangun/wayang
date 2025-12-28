package tech.kayys.wayang.engine;

/**
 * Batch processing configuration.
 */
public class BatchConfig {
    private boolean enabled = false;
    private Integer batchSize = 100;
    private Integer parallelism = 1;
    private Long batchTimeoutMs = 300000L; // 5 minutes

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final BatchConfig config = new BatchConfig();

        public Builder enabled(boolean enabled) {
            config.enabled = enabled;
            return this;
        }

        public Builder batchSize(int size) {
            config.batchSize = size;
            return this;
        }

        public Builder parallelism(int parallelism) {
            config.parallelism = parallelism;
            return this;
        }

        public Builder batchTimeout(long ms) {
            config.batchTimeoutMs = ms;
            return this;
        }

        public BatchConfig build() {
            return config;
        }
    }

    // Getters and setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public Integer getParallelism() {
        return parallelism;
    }

    public void setParallelism(Integer parallelism) {
        this.parallelism = parallelism;
    }

    public Long getBatchTimeoutMs() {
        return batchTimeoutMs;
    }

    public void setBatchTimeoutMs(Long batchTimeoutMs) {
        this.batchTimeoutMs = batchTimeoutMs;
    }
}