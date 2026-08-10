package tech.kayys.wayang.agent.react;

import tech.kayys.wayang.agent.WayangAgentListener;
import tech.kayys.wayang.provider.ChatMessage;
import tech.kayys.wayang.provider.Provider;
import tech.kayys.wayang.provider.StreamEvent;
import tech.kayys.wayang.provider.ToolSpec;
import tech.kayys.wayang.spi.plugin.PluginRegistry;
import tech.kayys.wayang.context.api.model.CompiledContext;
import tech.kayys.wayang.tool.Tool;
import tech.kayys.wayang.tool.ToolInvocation;
import tech.kayys.wayang.tool.ToolResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
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

        executeLoop(userInput, listener, 0);
    }
    
    private void executeLoop(String userInput, WayangAgentListener listener, int stepCount) {
        if (stepCount > 25) { // Budget limit
            listener.onError("Execution budget exceeded.");
            return;
        }
        
        List<ChatMessage> history = memory != null ? memory.getHistory() : Collections.singletonList(ChatMessage.userText(userInput));
        String effectiveSystemPrompt = systemPromptWithCompiledContext(userInput);
        
        List<ToolSpec> toolSpecs = tools.stream()
                .map(t -> new ToolSpec(t.descriptor().name(), t.descriptor().description(), t.descriptor().inputSchema()))
                .collect(Collectors.toList());

        AtomicReference<String> currentToolName = new AtomicReference<>();
        
        try {
            provider.streamChat(history, effectiveSystemPrompt, toolSpecs, 0.7, 4096, event -> {
                if (event instanceof StreamEvent.TextDelta textEvent) {
                    listener.onTextDelta(textEvent.text());
                } else if (event instanceof StreamEvent.ToolUseStart toolCall) {
                    listener.onToolCallStart(toolCall.id(), toolCall.name());
                    currentToolName.set(toolCall.name());
                } else if (event instanceof StreamEvent.ToolUseEnd toolEnd) {
                    String toolName = currentToolName.get();
                    if (toolName == null) {
                        listener.onError("Received ToolUseEnd without ToolUseStart");
                        return;
                    }
                    
                    Tool matchingTool = tools.stream()
                        .filter(t -> t.descriptor().name().equals(toolName))
                        .findFirst()
                        .orElse(null);
                        
                    if (matchingTool == null) {
                        listener.onError("Tool not found: " + toolName);
                        return;
                    }
                    
                    // Create invocation
                    Map<String, Object> arguments = toolEnd.input().asStringObjectMap();
                    ToolInvocation invocation = new ToolInvocation() {
                        @Override public String name() { return toolName; }
                        @Override public Map<String, Object> arguments() { return arguments; }
                        @Override public tech.kayys.wayang.identity.ResourceId id() { return null; }
                        @Override public tech.kayys.wayang.extension.Metadata metadata() { return tech.kayys.wayang.extension.Metadata.builder().build(); }
                        @Override public tech.kayys.wayang.resource.ResourceType type() { return new tech.kayys.wayang.resource.ResourceType.Custom("invocation"); }
                    };
                    
                    try {
                        // Execute tool synchronously for the loop
                        ToolResult result = matchingTool.execute(invocation, null).get();
                        listener.onToolResult(toolEnd.id(), toolName, result);
                        
                        // Append tool result
                        if (memory != null) {
                            String resultStr = result.getOutputs() != null ? result.getOutputs().toString() : "";
                            memory.addMessage(ChatMessage.toolResults(java.util.Collections.singletonList(
                                new tech.kayys.wayang.provider.ContentBlock.ToolResult(toolEnd.id(), resultStr, !result.isSuccess())
                            )));
                        }
                        
                        // Recurse for the next turn
                        executeLoop(userInput, listener, stepCount + 1);
                        
                    } catch (Exception e) {
                        listener.onError("Tool execution failed: " + e.getMessage());
                    }
                } else if (event instanceof StreamEvent.MessageStop stopEvent) {
                    listener.onDone(stopEvent.reason());
                }
            });
        } catch (IOException | InterruptedException e) {
            listener.onError(e.getMessage());
        }
    }

    private String systemPromptWithCompiledContext(String userInput) {
        if (contextCompiler == null || workspace == null || tokenBudget == null) {
            return systemPrompt;
        }

        Optional<Path> targetFile = inferTargetFile(userInput);
        if (targetFile.isEmpty()) {
            return systemPrompt;
        }

        try {
            CompiledContext compiled = contextCompiler.compile(workspace, targetFile.get(), 2, tokenBudget);
            if (compiled.entries().isEmpty()) {
                return systemPrompt;
            }
            String basePrompt = systemPrompt == null ? "" : systemPrompt.stripTrailing();
            return basePrompt
                    + "\n\nRelevant source context:\n"
                    + compiled.toPromptString();
        } catch (RuntimeException contextFailure) {
            return systemPrompt;
        }
    }

    private Optional<Path> inferTargetFile(String userInput) {
        if (userInput == null || userInput.isBlank()) {
            return Optional.empty();
        }

        Path root = workspace.toAbsolutePath().normalize();
        for (String token : userInput.split("\\s+")) {
            String candidate = token.replaceAll("^[`'\"(<\\[]+|[`'\"),>\\].:;]+$", "");
            if (!candidate.endsWith(".java")) {
                continue;
            }
            Path path = root.resolve(candidate).normalize();
            if (path.startsWith(root) && Files.isRegularFile(path)) {
                return Optional.of(path);
            }
        }
        return Optional.empty();
    }
}
