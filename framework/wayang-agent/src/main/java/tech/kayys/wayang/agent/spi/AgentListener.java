package tech.kayys.wayang.agent.spi;

import tech.kayys.wayang.agent.Agent;
import tech.kayys.wayang.tool.ToolInvocation;
import tech.kayys.wayang.tool.ToolResult;

/**
 * SPI for listening to agent lifecycle events.
 * Used for observability, telemetry, and audit logging.
 */
public interface AgentListener {
    default void onAgentStart(Agent agent, String sessionId) {}
    default void onThought(Agent agent, String thought) {}
    default void onToolStart(Agent agent, ToolInvocation invocation) {}
    default void onToolResult(Agent agent, ToolInvocation invocation, ToolResult result) {}
    default void onAgentEnd(Agent agent, String finalResponse) {}
    default void onAgentError(Agent agent, Throwable error) {}
}
