package tech.kayys.wayang.execution;

import java.util.concurrent.CompletionStage;
import tech.kayys.wayang.tool.ToolInvocation;
import tech.kayys.wayang.tool.ToolResult;

/**
 * Pipeline for executing tools with policies.
 */
public interface AgentToolExecutor {
    
    /**
     * Evaluates and executes a tool invocation.
     * May return a decision to wait for approval, or execute and return a tool result.
     * 
     * @param invocation The requested tool invocation
     * @return Future containing the decision (WaitForApproval) or the final ToolResult
     */
    CompletionStage<AgentDecision> execute(ToolInvocation invocation);
}
