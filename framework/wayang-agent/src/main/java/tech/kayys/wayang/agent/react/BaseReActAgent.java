package tech.kayys.wayang.agent.react;

import tech.kayys.wayang.agent.Agent;
import tech.kayys.wayang.agent.PermissionDecision;
import tech.kayys.wayang.agent.WayangAgentListener;
import tech.kayys.wayang.provider.ChatMessage;
import tech.kayys.wayang.provider.Provider;
import tech.kayys.wayang.spi.memory.Memory;
import tech.kayys.wayang.tool.Tool;
import tech.kayys.wayang.tool.ToolInvocation;
import tech.kayys.wayang.tool.ToolResult;
import tech.kayys.wayang.context.api.BudgetedContextCompiler;
import tech.kayys.wayang.context.api.ContextPlanner;
import tech.kayys.wayang.agent.spi.AgentListener;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A base implementation of a Reasoning and Acting (ReAct) agent.
 */
public abstract class BaseReActAgent implements Agent {
    protected Provider provider;
    protected Memory<ChatMessage> memory;
    protected String modelId;
    protected String systemPrompt;
    protected List<Tool> tools = new ArrayList<>();
    protected boolean autoApproveTools = false;
    protected Path workspace;
    
    protected BudgetedContextCompiler contextCompiler;
    protected ContextPlanner contextPlanner;
    protected Integer tokenBudget;
    
    protected List<AgentListener> listeners = new ArrayList<>();

    @Override
    public Memory<ChatMessage> getMemory() {
        return memory;
    }

    @Override
    public void setMemory(Memory<ChatMessage> memory) {
        this.memory = memory;
    }

    @Override
    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    @Override
    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    @Override
    public String getModelId() {
        return modelId;
    }

    @Override
    public void setSystemPrompt(String sp) {
        this.systemPrompt = sp;
    }

    @Override
    public Collection<Tool> tools() {
        return tools;
    }

    @Override
    public void setTools(List<Tool> tools) {
        this.tools.clear();
        this.tools.addAll(tools);
    }

    @Override
    public boolean autoApproveTools() {
        return autoApproveTools;
    }

    @Override
    public void setAutoApproveTools(boolean v) {
        this.autoApproveTools = v;
    }

    @Override
    public Path workspace() {
        return workspace;
    }
    
    public void setWorkspace(Path workspace) {
        this.workspace = workspace;
    }

    public void setContextCompiler(BudgetedContextCompiler compiler, ContextPlanner planner, Integer budget) {
        this.contextCompiler = compiler;
        this.contextPlanner = planner;
        this.tokenBudget = budget;
    }
    
    public void setListeners(List<AgentListener> listeners) {
        if (listeners != null) {
            this.listeners.addAll(listeners);
        }
    }
    
    protected void fireAgentStart(String sessionId) {
        listeners.forEach(l -> l.onAgentStart(this, sessionId));
    }
    
    protected void fireThought(String thought) {
        listeners.forEach(l -> l.onThought(this, thought));
    }
    
    protected void fireToolStart(ToolInvocation invocation) {
        listeners.forEach(l -> l.onToolStart(this, invocation));
    }
    
    protected void fireToolResult(ToolInvocation invocation, ToolResult result) {
        listeners.forEach(l -> l.onToolResult(this, invocation, result));
    }
    
    protected void fireAgentEnd(String finalResponse) {
        listeners.forEach(l -> l.onAgentEnd(this, finalResponse));
    }
    
    protected void fireAgentError(Throwable error) {
        listeners.forEach(l -> l.onAgentError(this, error));
    }
}
