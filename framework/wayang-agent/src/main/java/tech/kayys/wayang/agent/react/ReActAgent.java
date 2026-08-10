package tech.kayys.wayang.agent.react;

import tech.kayys.wayang.agent.WayangAgentListener;
import tech.kayys.wayang.provider.ChatMessage;
import tech.kayys.wayang.provider.Provider;
import tech.kayys.wayang.provider.StreamEvent;
import tech.kayys.wayang.provider.ToolSpec;
import tech.kayys.wayang.spi.plugin.PluginRegistry;
import tech.kayys.wayang.context.api.model.CompiledContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
        String effectiveSystemPrompt = systemPromptWithCompiledContext(userInput);
        
        List<ToolSpec> toolSpecs = tools.stream()
                .map(t -> new ToolSpec(t.descriptor().name(), t.descriptor().description(), t.descriptor().inputSchema()))
                .collect(Collectors.toList());

        try {
            provider.streamChat(history, effectiveSystemPrompt, toolSpecs, 0.7, 4096, event -> {
                if (event instanceof StreamEvent.TextDelta textEvent) {
                    listener.onTextDelta(textEvent.text());
                } else if (event instanceof StreamEvent.ToolUseStart toolCall) {
                    listener.onToolCallStart(toolCall.id(), toolCall.name());
                    // Normally the agent would execute the tool here, append to history, and re-prompt the LLM.
                    // This is a stub for the POC.
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
