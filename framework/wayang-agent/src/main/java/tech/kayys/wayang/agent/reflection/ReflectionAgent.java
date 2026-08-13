package tech.kayys.wayang.agent.reflection;

import tech.kayys.wayang.agent.WayangAgentListener;
import tech.kayys.wayang.agent.react.BaseReActAgent;
import tech.kayys.wayang.provider.ChatMessage;
import tech.kayys.wayang.provider.StreamEvent;
import tech.kayys.wayang.provider.ToolSpec;
import tech.kayys.wayang.tool.Tool;
import tech.kayys.wayang.tool.ToolInvocation;
import tech.kayys.wayang.tool.ToolResult;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Reflection Agent (§38) — Generate → Critique → Refine loop.
 *
 * <ol>
 *   <li><b>Generate</b> — produce an initial answer (with tool use).</li>
 *   <li><b>Critique</b> — self-evaluate: is the answer complete, accurate, well-structured?</li>
 *   <li><b>Refine</b>   — if critique found issues, produce an improved version.</li>
 * </ol>
 *
 * <p>The loop repeats up to {@code maxRounds} times (default 2).
 * Each round is fully observable via the {@link WayangAgentListener}.</p>
 */
public class ReflectionAgent extends BaseReActAgent {

    private int maxRounds = 2;

    public ReflectionAgent() {}

    public ReflectionAgent(int maxRounds) {
        this.maxRounds = maxRounds;
    }

    private static final String CRITIQUE_SUFFIX =
        "\n\nYou are now in CRITIQUE mode. Review your previous answer critically.\n" +
        "Ask yourself:\n" +
        "- Is the answer complete?\n" +
        "- Is it accurate and well-reasoned?\n" +
        "- Is it clear and well-structured?\n\n" +
        "Output one of:\n" +
        "CRITIQUE: SATISFACTORY — answer meets all criteria.\n" +
        "CRITIQUE: NEEDS REFINEMENT — followed by specific issues and an improved answer.";

    @Override
    public void send(String userInput, WayangAgentListener listener) {
        if (memory != null) memory.addMessage(ChatMessage.userText(userInput));

        // Generate initial answer
        listener.onTextDelta("\n[Reflection] Round 1: Generating initial answer\n");
        String answer = runGenerate(listener);
        if (answer == null) return;

        // Critique-Refine loop
        for (int round = 1; round <= maxRounds; round++) {
            listener.onTextDelta("\n[Reflection] Round " + (round + 1) + ": Critiquing\n");
            String critique = runCritique(listener);
            if (critique == null) return;

            if (critique.contains("CRITIQUE: SATISFACTORY")) {
                listener.onTextDelta("\n[Reflection] Critique satisfied — answer accepted.\n");
                break;
            }

            if (round < maxRounds) {
                listener.onTextDelta("\n[Reflection] Round " + (round + 1) + ": Refining\n");
                answer = runGenerate(listener);
                if (answer == null) return;
            }
        }

        listener.onDone("stop");
    }

    // ── Generate (with tool support) ─────────────────────────────────────────

    private String runGenerate(WayangAgentListener listener) {
        StringBuilder buf = new StringBuilder();
        AtomicReference<String> pendingTool = new AtomicReference<>();
        List<ToolSpec> specs = buildToolSpecs();

        try {
            provider.streamChat(currentHistory(), basePrompt(), specs, 0.7, 4096, event -> {
                if (event instanceof StreamEvent.TextDelta td) {
                    listener.onTextDelta(td.text());
                    buf.append(td.text());
                } else if (event instanceof StreamEvent.ToolUseStart ts) {
                    listener.onToolCallStart(ts.id(), ts.name());
                    pendingTool.set(ts.name());
                } else if (event instanceof StreamEvent.ToolUseEnd te) {
                    invokeTool(te, pendingTool.get(), listener);
                }
            });
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            listener.onError("Generation failed: " + e.getMessage());
            return null;
        }

        String text = buf.toString().trim();
        if (memory != null) memory.addMessage(ChatMessage.assistantText(text));
        return text;
    }

    // ── Critique (text-only, no tools) ───────────────────────────────────────

    private String runCritique(WayangAgentListener listener) {
        StringBuilder buf = new StringBuilder();
        try {
            provider.streamChat(currentHistory(), basePrompt() + CRITIQUE_SUFFIX,
                List.of(), 0.3, 2048, event -> {
                if (event instanceof StreamEvent.TextDelta td) {
                    listener.onTextDelta(td.text());
                    buf.append(td.text());
                }
            });
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            listener.onError("Critique failed: " + e.getMessage());
            return null;
        }
        String text = buf.toString().trim();
        if (memory != null) memory.addMessage(ChatMessage.assistantText(text));
        return text;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void invokeTool(StreamEvent.ToolUseEnd toolEnd, String toolName, WayangAgentListener listener) {
        if (toolName == null) return;
        Tool tool = tools.stream().filter(t -> t.descriptor().name().equals(toolName))
            .findFirst().orElse(null);
        if (tool == null) { listener.onError("Tool not found: " + toolName); return; }
        Map<String, Object> args = toolEnd.input().asStringObjectMap();
        ToolInvocation inv = buildInvocation(toolName, args);
        try {
            ToolResult result = toolExecutor != null
                ? toolExecutor.executeViaPolicy(inv, () -> safeDirect(tool, inv))
                : safeDirect(tool, inv);
            listener.onToolResult(toolEnd.id(), toolName, result);
            if (memory != null) {
                String out = result.getOutputs() != null ? result.getOutputs().toString() : "";
                memory.addMessage(ChatMessage.toolResults(List.of(
                    (tech.kayys.wayang.resource.ContentPart.ToolResult)
                        tech.kayys.wayang.resource.ContentPart.toolResult(toolEnd.id(), out, !result.isSuccess()))));
            }
        } catch (Exception e) {
            listener.onError("Tool [" + toolName + "] error: " + e.getMessage());
        }
    }

    private ToolResult safeDirect(Tool tool, ToolInvocation inv) {
        try { return tool.execute(inv, null).get(); }
        catch (Exception ex) { throw new RuntimeException(ex); }
    }

    private List<ChatMessage> currentHistory() {
        return memory != null ? memory.getHistory() : Collections.emptyList();
    }

    private String basePrompt() {
        return systemPrompt != null ? systemPrompt : "You are a helpful AI assistant.";
    }

    private List<ToolSpec> buildToolSpecs() {
        return tools.stream()
            .map(t -> new ToolSpec(t.descriptor().name(), t.descriptor().description(), t.descriptor().inputSchema()))
            .collect(Collectors.toList());
    }

    private ToolInvocation buildInvocation(String name, Map<String, Object> args) {
        return new ToolInvocation() {
            public String name() { return name; }
            public Map<String, Object> arguments() { return args; }
            public tech.kayys.wayang.identity.ResourceId id() { return null; }
            public tech.kayys.wayang.extension.Metadata metadata() {
                return tech.kayys.wayang.extension.Metadata.builder().build();
            }
            public tech.kayys.wayang.resource.ResourceType type() {
                return new tech.kayys.wayang.resource.ResourceType.Custom("invocation");
            }
        };
    }
}
