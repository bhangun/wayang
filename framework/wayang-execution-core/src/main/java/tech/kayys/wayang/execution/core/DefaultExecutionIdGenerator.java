package tech.kayys.wayang.execution.core;

import tech.kayys.wayang.execution.ExecutionIdGenerator;

import java.util.UUID;

/**
 * UUID-based execution ID generator.
 */
public final class DefaultExecutionIdGenerator implements ExecutionIdGenerator {

    private static final String EXEC_PREFIX = "exec-";
    private static final String TRACE_PREFIX = "trace-";

    @Override
    public String generate() {
        return EXEC_PREFIX + UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public String generateTrace() {
        return TRACE_PREFIX + UUID.randomUUID().toString().replace("-", "");
    }
}
