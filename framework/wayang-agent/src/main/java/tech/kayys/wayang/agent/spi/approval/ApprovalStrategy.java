package tech.kayys.wayang.agent.spi.approval;

import tech.kayys.wayang.agent.Agent;
import tech.kayys.wayang.tool.ToolInvocation;

/**
 * SPI for intercepting tool calls to require human approval or check against guardrails.
 */
public interface ApprovalStrategy {

    /**
     * Called before a tool is executed.
     * 
     * @param agent the agent executing the tool
     * @param invocation the details of the tool being called
     * @throws ApprovalRequiredException if the tool execution must be paused for human review
     */
    void requestApproval(Agent agent, ToolInvocation invocation) throws ApprovalRequiredException;
}
