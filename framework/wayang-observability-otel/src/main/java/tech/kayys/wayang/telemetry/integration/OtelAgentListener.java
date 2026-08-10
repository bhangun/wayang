package tech.kayys.wayang.telemetry.integration;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.agent.Agent;
import tech.kayys.wayang.agent.spi.AgentListener;
import tech.kayys.wayang.tool.ToolInvocation;
import tech.kayys.wayang.tool.ToolResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An AgentListener that automatically generates OpenTelemetry Spans for the agent lifecycle.
 * Useful for deep observability in Datadog, Jaeger, or LangSmith.
 */
@ApplicationScoped
public class OtelAgentListener implements AgentListener {

    private final Tracer tracer;
    
    // Maintain state between start and end events
    private final Map<String, Span> agentSpans = new ConcurrentHashMap<>();
    private final Map<String, Span> toolSpans = new ConcurrentHashMap<>();
    
    @Inject
    public OtelAgentListener(OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry.getTracer("wayang-agent-tracer");
    }

    @Override
    public void onAgentStart(Agent agent, String sessionId) {
        Span span = tracer.spanBuilder("agent.run")
                .setAttribute("agent.sessionId", sessionId)
                .startSpan();
        agentSpans.put(sessionId, span);
    }

    @Override
    public void onThought(Agent agent, String thought) {
        // Find the active span and add an event
        // In a real implementation, we'd look up by session. For POC, we'll use a simple log.
    }

    @Override
    public void onToolStart(Agent agent, ToolInvocation invocation) {
        Span span = tracer.spanBuilder("tool.execution")
                .setAttribute("tool.name", invocation.name())
                .startSpan();
        toolSpans.put(invocation.name(), span); // Simplified for POC
    }

    @Override
    public void onToolResult(Agent agent, ToolInvocation invocation, ToolResult result) {
        Span span = toolSpans.remove(invocation.name());
        if (span != null) {
            span.setAttribute("tool.success", result != null && result.isSuccess());
            if (result != null && !result.isSuccess()) {
                span.setAttribute("tool.error", result.getErrorMessage());
            }
            span.end();
        }
    }

    @Override
    public void onAgentEnd(Agent agent, String finalResponse) {
        // Assume session lookup for POC
        for (Span span : agentSpans.values()) {
            span.end();
        }
        agentSpans.clear();
    }

    @Override
    public void onAgentError(Agent agent, Throwable error) {
        for (Span span : agentSpans.values()) {
            span.recordException(error);
            span.end();
        }
        agentSpans.clear();
    }
}
