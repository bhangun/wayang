package tech.kayys.wayang.agent.research;

import tech.kayys.wayang.agent.WayangAgentListener;
import tech.kayys.wayang.agent.react.BaseReActAgent;
import tech.kayys.wayang.provider.ChatMessage;
import tech.kayys.wayang.provider.StreamEvent;
import tech.kayys.wayang.provider.ToolSpec;
import tech.kayys.wayang.tool.Tool;
import tech.kayys.wayang.tool.ToolInvocation;
import tech.kayys.wayang.tool.ToolResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Research Agent (§37) — structured multi-step research pipeline.
 *
 * <h3>Pipeline</h3>
 * <ol>
 *   <li><b>Query Decomposition</b> — break the research question into sub-queries.</li>
 *   <li><b>Retrieval</b>           — for each sub-query, invoke retrieval/search tools.</li>
 *   <li><b>Synthesis</b>           — aggregate evidence and produce a structured report.</li>
 * </ol>
 *
 * <p>The agent is tool-heavy by design: all retrieval happens through the {@link ToolExecutorBridge}
 * (policy pipeline). Synthesis is model-only (no tool calls).</p>
 */
public class ResearchAgent extends BaseReActAgent {

    private static final int MAX_SUB_QUERIES = 5;

    private static final String DECOMPOSE_SUFFIX =
        "\n\nYou are in RESEARCH DECOMPOSITION mode.\n" +
        "Break the research question into at most " + MAX_SUB_QUERIES + " specific sub-queries, " +
        "each answerable by a single tool call (web search, document retrieval, etc.).\n" +
        "Format:\nSUB-QUERIES:\n1. <query>\n2. <query>\n...";

    private static final String SYNTHESIZE_SUFFIX =
        "\n\nYou are in SYNTHESIS mode.\n" +
        "Review all retrieved evidence above and write a structured research report:\n" +
        "- Executive Summary\n" +
        "- Key Findings (with sources)\n" +
        "- Gaps and limitations\n" +
        "- Conclusion";

    @Override
    public void send(String userInput, WayangAgentListener listener) {
        if (memory != null) memory.addMessage(ChatMessage.userText(userInput));

        // Phase 1: Decompose
        listener.onTextDelta("\n[Research] Phase 1: Decomposing research question\n");
        String decomposition = runTextPhase(basePrompt() + DECOMPOSE_SUFFIX, listener, "Decomposition");
        if (decomposition == null) return;

        // Phase 2: Retrieve for each sub-query
        listener.onTextDelta("\n[Research] Phase 2: Retrieving evidence\n");
        List<String> evidence = runRetrieval(decomposition, listener);

        // Phase 3: Synthesize
        listener.onTextDelta("\n[Research] Phase 3: Synthesizing report\n");
        String report = runTextPhase(basePrompt() + SYNTHESIZE_SUFFIX, listener, "Synthesis");

        listener.onDone("stop");
    }

    // ── Phase 1: Decompose ───────────────────────────────────────────────────

    private String runTextPhase(String systemP, WayangAgentListener listener, String label) {
        StringBuilder buf = new StringBuilder();
        try {
            provider.streamChat(currentHistory(), systemP, List.of(), 0.3, 2048, event -> {
                if (event instanceof StreamEvent.TextDelta td) {
                    listener.onTextDelta(td.text());
                    buf.append(td.text());
                }
            });
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            listener.onError(label + " failed: " + e.getMessage());
            return null;
        }
        String text = buf.toString().trim();
        if (memory != null) memory.addMessage(ChatMessage.assistantText(text));
        return text;
    }

    // ── Phase 2: Retrieve ────────────────────────────────────────────────────

    private List<String> runRetrieval(String decomposition, WayangAgentListener listener) {
        List<String> results = new ArrayList<>();

        // Parse sub-queries from the decomposition text (simple line-scan)
        String[] lines = decomposition.split("\n");
        List<String> subQueries = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.matches("^\\d+\\..*")) {
                subQueries.add(trimmed.replaceFirst("^\\d+\\.\\s*", ""));
            }
        }

        if (subQueries.isEmpty()) {
            // Fallback: treat entire decomposition as one query
            subQueries.add(decomposition);
        }

        // Limit to MAX_SUB_QUERIES
        subQueries = subQueries.stream().limit(MAX_SUB_QUERIES).collect(Collectors.toList());

        for (int i = 0; i < subQueries.size(); i++) {
            String query = subQueries.get(i);
            listener.onTextDelta("\n  [Research] Retrieving sub-query " + (i + 1) + ": " + query + "\n");

            StringBuilder buf = new StringBuilder();
            AtomicReference<String> pendingTool = new AtomicReference<>();
            List<ToolSpec> specs = buildToolSpecs();

            // Build a targeted retrieval message
            if (memory != null) {
                memory.addMessage(ChatMessage.userText("Research sub-query: " + query));
            }

            try {
                provider.streamChat(currentHistory(), basePrompt() +
                    "\nAnswer this specific research sub-query using your retrieval tools: " + query,
                    specs, 0.3, 2048, event -> {
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
                listener.onError("Retrieval failed for sub-query " + (i + 1) + ": " + e.getMessage());
                continue;
            }

            String result = buf.toString().trim();
            results.add(result);
            if (memory != null) memory.addMessage(ChatMessage.assistantText(result));
        }

        return results;
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
        return systemPrompt != null ? systemPrompt : "You are an expert research assistant.";
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
