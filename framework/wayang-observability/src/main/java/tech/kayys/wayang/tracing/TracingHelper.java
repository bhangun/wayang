package tech.kayys.wayang.tracing;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import tech.kayys.wayang.execution.ExecutionContext;

/**
 * Tracing Helper - Convenience methods
 */
public class TracingHelper {

    private final TracingService tracing;

    public TracingHelper(TracingService tracing) {
        this.tracing = tracing;
    }

    public <T> T traceExecution(String name, ExecutionContext context, Callable<T> callable) throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("execution.id", context.executionId().toString());
        attributes.put("execution.graph", context.graph() != null ? context.graph().toString() : "none");

        return tracing.withSpan(name, () -> {
            tracing.addAttribute("execution.id", context.executionId().toString());
            try {
                return callable.call();
            } catch (Exception e) {
                tracing.recordException(e);
                throw e;
            }
        });
    }

    public void traceAgentExecution(String agentId, ExecutionContext context, Runnable runnable) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("agent.id", agentId);
        attributes.put("execution.id", context.executionId().toString());

        Runnable traced = () -> {
            tracing.addAttribute("agent.id", agentId);
            tracing.addAttribute("execution.id", context.executionId().toString());
            runnable.run();
        };
        tracing.withSpan("agent." + agentId, traced);
    }
}
