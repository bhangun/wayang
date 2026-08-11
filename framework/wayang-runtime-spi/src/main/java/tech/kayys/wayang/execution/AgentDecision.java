package tech.kayys.wayang.execution;

import java.util.List;

import tech.kayys.wayang.agent.AgentResponse;
import tech.kayys.wayang.provider.ChatMessage;
import tech.kayys.wayang.tool.ToolInvocation;

/**
 * Sealed interface for execution decisions in the AgentRuntime.
 */
public sealed interface AgentDecision permits 
    AgentDecision.CallModel, 
    AgentDecision.ExecuteTool, 
    AgentDecision.WaitForApproval, 
    AgentDecision.Finish, 
    AgentDecision.Fail, 
    AgentDecision.Retry {

    record CallModel(List<ChatMessage> messages) implements AgentDecision {}
    record ExecuteTool(ToolInvocation invocation) implements AgentDecision {}
    record WaitForApproval(ToolInvocation invocation) implements AgentDecision {}
    record Finish(AgentResponse response) implements AgentDecision {}
    record Fail(Throwable error) implements AgentDecision {}
    record Retry(String reason, int delayMs) implements AgentDecision {}
}
