package tech.kayys.wayang.agent.impl;

import tech.kayys.wayang.agent.Agent;

import tech.kayys.wayang.provider.Provider;
import tech.kayys.wayang.tool.Tool;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class DefaultWayangAgentBuilder {
    private Provider provider;
    private final List<Tool> tools = new ArrayList<>();
    private String systemPrompt = "You are a helpful assistant.";
    private double temperature = 0.0;
    private int maxTokens = 4096;
    private boolean autoApproveTools = false;
    private java.nio.file.Path workspace = java.nio.file.Path.of(System.getProperty("user.dir"));
    private DefaultWayangAgent.ToolExecutorBridge toolExecutorBridge;

    public DefaultWayangAgentBuilder provider(Provider provider) {
        this.provider = provider;
        return this;
    }

    public DefaultWayangAgentBuilder addTool(Tool tool) {
        this.tools.add(tool);
        return this;
    }

    public DefaultWayangAgentBuilder addAllTools(List<Tool> tools) {
        this.tools.addAll(tools);
        return this;
    }

    public DefaultWayangAgentBuilder registerOsTools() {
        // Load all SPI tools
        ServiceLoader<Tool> loader = ServiceLoader.load(Tool.class);
        int before = this.tools.size();
        loader.forEach(t -> {
            System.err.println("[DefaultWayangAgentBuilder] Loaded tool via SPI: " + t.getId());
            this.tools.add(t);
        });
        int after = this.tools.size();
        System.err.println("[DefaultWayangAgentBuilder] registerOsTools: loaded " + (after - before) + " tools (total=" + after + ")");
        return this;
    }

    public DefaultWayangAgentBuilder systemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
        return this;
    }

    public DefaultWayangAgentBuilder temperature(double temperature) {
        this.temperature = temperature;
        return this;
    }

    public DefaultWayangAgentBuilder maxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    public DefaultWayangAgentBuilder autoApproveTools(boolean autoApproveTools) {
        this.autoApproveTools = autoApproveTools;
        return this;
    }

    public DefaultWayangAgentBuilder workspace(java.nio.file.Path workspace) {
        this.workspace = workspace;
        return this;
    }

    public DefaultWayangAgentBuilder toolExecutorBridge(DefaultWayangAgent.ToolExecutorBridge toolExecutorBridge) {
        this.toolExecutorBridge = toolExecutorBridge;
        return this;
    }

    public Agent build() {
        if (provider == null) {
            throw new IllegalStateException("Provider must be set");
        }
        return new DefaultWayangAgent(provider, tools, systemPrompt, temperature, maxTokens, autoApproveTools, workspace, toolExecutorBridge);
    }
}
