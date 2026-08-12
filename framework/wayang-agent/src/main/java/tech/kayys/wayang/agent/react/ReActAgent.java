package tech.kayys.wayang.agent.react;

import tech.kayys.wayang.agent.WayangAgentListener;
import tech.kayys.wayang.provider.ChatMessage;
import tech.kayys.wayang.provider.StreamEvent;
import tech.kayys.wayang.provider.ToolSpec;
import tech.kayys.wayang.tool.Tool;
import tech.kayys.wayang.tool.ToolInvocation;
import tech.kayys.wayang.tool.ToolResult;
import tech.kayys.wayang.context.api.model.CompiledContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Standard ReAct loop implementation.
 *
 * <p>Execution flow per iteration:
 * <ol>
 *   <li>Optional: checkpoint (saves state before model call)</li>
 *   <li>provider.streamChat — model generates text or requests tool use</li>
 *   <li>On tool call: route through {@link ToolExecutorBridge} (policy pipeline)</li>
 *   <li>On tool result: append to memory + optional checkpoint</li>
 *   <li>Recurse — re-prompt the model with tool results in history</li>
 * </ol>
 */
public class ReActAgent extends BaseReActAgent {

    @Override
    public void send(String userInput, WayangAgentListener listener) {
        if (memory != null) {
            memory.addMessage(ChatMessage.userText(userInput));
        }
        executeLoop(userInput, listener, 0);
    }

    // -------------------------------------------------------------------------
    // Core loop
    // -------------------------------------------------------------------------

    private void executeLoop(String userInput, WayangAgentListener listener, int stepCount) {
        // --- Budget guard ---
        int maxSteps = 25;
        if (stepCount >= maxSteps) {
            listener.onError("Execution budget exceeded (" + maxSteps + " steps).");
            return;
        }

        List<ChatMessage> history = memory != null
            ? memory.getHistory()
            : Collections.singletonList(ChatMessage.userText(userInput));

        String effectiveSystemPrompt = systemPromptWithCompiledContext(userInput);

        List<ToolSpec> toolSpecs = tools.stream()
            .map(t -> new ToolSpec(t.descriptor().name(),
                                   t.descriptor().description(),
                                   t.descriptor().inputSchema()))
            .collect(Collectors.toList());

        // --- Pre-model checkpoint ---
        saveCheckpoint();

        AtomicReference<String> currentToolId   = new AtomicReference<>();
        AtomicReference<String> currentToolName = new AtomicReference<>();

        try {
            provider.streamChat(history, effectiveSystemPrompt, toolSpecs, 0.7, 4096, event -> {
                if (event instanceof StreamEvent.TextDelta textEvent) {
                    listener.onTextDelta(textEvent.text());

                } else if (event instanceof StreamEvent.ToolUseStart toolCall) {
                    listener.onToolCallStart(toolCall.id(), toolCall.name());
                    currentToolId.set(toolCall.id());
                    currentToolName.set(toolCall.name());

                } else if (event instanceof StreamEvent.ToolUseEnd toolEnd) {
                    handleToolEnd(toolEnd, currentToolName.get(), userInput, listener, stepCount);

                } else if (event instanceof StreamEvent.MessageStop stopEvent) {
                    listener.onDone(stopEvent.reason());
                }
            });
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            listener.onError("Model communication error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Tool handling
    // -------------------------------------------------------------------------

    private void handleToolEnd(
            StreamEvent.ToolUseEnd toolEnd,
            String toolName,
            String userInput,
            WayangAgentListener listener,
            int stepCount) {

        if (toolName == null) {
            listener.onError("Received ToolUseEnd without a preceding ToolUseStart.");
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

        Map<String, Object> arguments = toolEnd.input().asStringObjectMap();

        ToolInvocation invocation = buildInvocation(toolName, arguments);

        try {
            ToolResult result = executeToolViaPolicy(matchingTool, invocation);

            listener.onToolResult(toolEnd.id(), toolName, result);

            // Append the tool result to conversation history.
            if (memory != null) {
                String resultStr = result.getOutputs() != null ? result.getOutputs().toString() : "";
                memory.addMessage(ChatMessage.toolResults(List.of(
                    (tech.kayys.wayang.resource.ContentPart.ToolResult) tech.kayys.wayang.resource.ContentPart.toolResult(
                        toolEnd.id(), resultStr, !result.isSuccess())
                )));
            }

            // Post-tool checkpoint.
            saveCheckpoint();

            // Re-prompt the model with tool results in history.
            executeLoop(userInput, listener, stepCount + 1);

        } catch (Exception e) {
            listener.onError("Tool execution failed [" + toolName + "]: " + e.getMessage());
        }
    }

    /**
     * Routes execution through the policy pipeline when a {@link ToolExecutorBridge} is available,
     * otherwise falls back to calling the tool directly.  This keeps the agent usable in tests
     * and headless scenarios where no CDI container is present.
     */
    private ToolResult executeToolViaPolicy(Tool tool, ToolInvocation invocation) throws Exception {
        if (toolExecutor != null) {
            return toolExecutor.executeViaPolicy(invocation, () -> {
                try {
                    return tool.execute(invocation, null).get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        // Fallback: direct execution (no policy, no retry, no CB).
        return tool.execute(invocation, null).get();
    }

    // -------------------------------------------------------------------------
    // Checkpointing
    // -------------------------------------------------------------------------

    /**
     * Saves a checkpoint if both a {@link CheckpointBridge} and an execution ID are available.
     * The bridge is provided by {@code DefaultAgentExecution} during wiring.
     */
    private void saveCheckpoint() {
        if (checkpointBridge != null && executionId != null) {
            // We pass a minimal AgentContext snapshot.  Full context injection
            // happens via DefaultAgentExecution; here we just signal the step boundary.
            checkpointBridge.save(executionId, null);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ToolInvocation buildInvocation(String toolName, Map<String, Object> arguments) {
        return new ToolInvocation() {
            @Override public String name() { return toolName; }
            @Override public Map<String, Object> arguments() { return arguments; }
            @Override public tech.kayys.wayang.identity.ResourceId id() { return null; }
            @Override public tech.kayys.wayang.extension.Metadata metadata() {
                return tech.kayys.wayang.extension.Metadata.builder().build();
            }
            @Override public tech.kayys.wayang.resource.ResourceType type() {
                return new tech.kayys.wayang.resource.ResourceType.Custom("invocation");
            }
        };
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
        } catch (RuntimeException e) {
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
