package tech.kayys.wayang.agent.react;

import tech.kayys.wayang.agent.WayangAgentListener;
import tech.kayys.wayang.provider.ChatMessage;
import tech.kayys.wayang.provider.Provider;
import tech.kayys.wayang.provider.StreamEvent;
import tech.kayys.wayang.provider.ToolSpec;
import tech.kayys.wayang.spi.plugin.PluginRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Standard ReAct loop implementation.
 */
public class ReActAgent extends BaseReActAgent {

    @Override
    public void send(String userInput, WayangAgentListener listener) {
        if (memory != null) {
            memory.addMessage(ChatMessage.userText(userInput));
        }

        List<ChatMessage> history = memory != null ? memory.getHistory() : Collections.singletonList(ChatMessage.userText(userInput));
        
        List<ToolSpec> toolSpecs = tools.stream()
                .map(t -> new ToolSpec(t.name(), t.description(), t.inputSchema()))
                .collect(Collectors.toList());

        try {
            provider.streamChat(history, systemPrompt, toolSpecs, 0.7, 4096, event -> {
                if (event instanceof StreamEvent.TextDelta textEvent) {
                    listener.onTextDelta(textEvent.text());
                } else if (event instanceof StreamEvent.ToolCall toolCall) {
                    listener.onToolCallStart(toolCall.id(), toolCall.name());
                    // Normally the agent would execute the tool here, append to history, and re-prompt the LLM.
                    // This is a stub for the POC.
                } else if (event instanceof StreamEvent.MessageStop stopEvent) {
                    listener.onDone(stopEvent.stopReason());
                }
            });
        } catch (IOException | InterruptedException e) {
            listener.onError(e.getMessage());
        }
    }

    @Override
    public void initialize() throws Exception {
        // Initialization logic for ReAct
    }

    @Override
    public Object process(Object request) throws Exception {
        // Non-streaming process method
        return null;
    }

    @Override
    public String getId() {
        return "react-agent";
    }

    @Override
    public tech.kayys.wayang.spi.agent.AgentPipeline getPipeline() {
        return null; // Stub
    }

    @Override
    public String id() {
        return "react";
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
    }

    @Override
    public void init(PluginRegistry registry) throws Exception {
    }
}
