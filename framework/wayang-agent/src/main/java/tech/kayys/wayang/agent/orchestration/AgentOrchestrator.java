package tech.kayys.wayang.agent.orchestration;

import tech.kayys.wayang.agent.Agent;
import tech.kayys.wayang.agent.WayangAgentListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Multi-agent orchestrator (§39) — coordinates multiple {@link Agent} instances
 * on top of the same Execution Kernel.
 *
 * <h3>Patterns supported</h3>
 * <ul>
 *   <li><b>Sequential</b> — agents run in order; each receives the previous output as input.</li>
 *   <li><b>Parallel fan-out/fan-in</b> — all agents run concurrently; results are merged.</li>
 *   <li><b>Conditional routing</b> — a router function selects which agent handles the input.</li>
 * </ul>
 *
 * <p>The orchestrator does not replace the Execution Kernel — it coordinates <em>which</em>
 * agent runs and <em>in what order</em>, consistent with "An Agent decides. The Kernel executes."</p>
 */
public class AgentOrchestrator {

    private static final Logger LOG = Logger.getLogger(AgentOrchestrator.class.getName());

    private final Executor pool = Executors.newVirtualThreadPerTaskExecutor();

    /** Ordered list of (name, agent) registrations. */
    private final Map<String, Agent> agents = new LinkedHashMap<>();

    /** Optional router: given user input, returns the agent name to use. */
    private Function<String, String> router;

    /** Fan-in merger: given all agent outputs, produces the final answer. */
    private Function<Map<String, String>, String> merger =
        results -> String.join("\n\n---\n\n", results.values());

    // ── Builder-style API ────────────────────────────────────────────────────

    public AgentOrchestrator register(String name, Agent agent) {
        agents.put(name, agent);
        return this;
    }

    public AgentOrchestrator withRouter(Function<String, String> router) {
        this.router = router;
        return this;
    }

    public AgentOrchestrator withMerger(Function<Map<String, String>, String> merger) {
        this.merger = merger;
        return this;
    }

    // ── Execution modes ──────────────────────────────────────────────────────

    /**
     * Sequential pipeline — each agent gets the previous agent's output as input.
     */
    public void sequential(String userInput, WayangAgentListener listener) {
        if (agents.isEmpty()) {
            listener.onError("AgentOrchestrator: no agents registered.");
            listener.onDone("stop");
            return;
        }

        String currentInput = userInput;
        int agentIndex = 0;

        for (Map.Entry<String, Agent> entry : agents.entrySet()) {
            agentIndex++;
            String name = entry.getKey();
            Agent agent = entry.getValue();
            final String input = currentInput;

            StringBuilder outputBuffer = new StringBuilder();

            listener.onTextDelta("\n[Orchestrator] Agent " + agentIndex + " [" + name + "] starting\n");

            WayangAgentListener delegatingListener = new WayangAgentListener() {
                public void onTextDelta(String text) {
                    listener.onTextDelta(text);
                    outputBuffer.append(text);
                }
                public void onToolCallStart(String id, String n)  { listener.onToolCallStart(id, n); }
                public void onToolCallReady(String id, String n, java.util.Map<String, Object> input) {}
                public void onToolPermissionNeeded(String id, String n, java.util.Map<String, Object> input,
                        java.util.function.Consumer<tech.kayys.wayang.agent.PermissionDecision> r) {}
                public void onToolResult(String id, String n, tech.kayys.wayang.tool.ToolResult r) {
                    listener.onToolResult(id, n, r);
                }
                public void onUsage(int in, int out) { listener.onUsage(in, out); }
                public void onDone(String reason)    { /* handled below */ }
                public void onError(String error)    { listener.onError("[" + name + "] " + error); }
            };

            agent.send(input, delegatingListener);
            currentInput = outputBuffer.toString().trim();
            listener.onTextDelta("\n[Orchestrator] Agent [" + name + "] complete\n");
        }

        listener.onTextDelta("\n[Orchestrator] Sequential pipeline complete.\n");
        listener.onDone("stop");
    }

    /**
     * Parallel fan-out — all agents run concurrently; results merged by {@link #merger}.
     */
    public void parallel(String userInput, WayangAgentListener listener) {
        if (agents.isEmpty()) {
            listener.onError("AgentOrchestrator: no agents registered.");
            listener.onDone("stop");
            return;
        }

        Map<String, CompletableFuture<String>> futures = new LinkedHashMap<>();

        for (Map.Entry<String, Agent> entry : agents.entrySet()) {
            String name = entry.getKey();
            Agent agent = entry.getValue();

            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                StringBuilder buf = new StringBuilder();
                WayangAgentListener delegate = new WayangAgentListener() {
                    public void onTextDelta(String text) {
                        synchronized (listener) { listener.onTextDelta("[" + name + "] " + text); }
                        buf.append(text);
                    }
                    public void onToolCallStart(String id, String n) {}
                    public void onToolCallReady(String id, String n, java.util.Map<String, Object> input) {}
                    public void onToolPermissionNeeded(String id, String n, java.util.Map<String, Object> input,
                            java.util.function.Consumer<tech.kayys.wayang.agent.PermissionDecision> r) {}
                    public void onToolResult(String id, String n, tech.kayys.wayang.tool.ToolResult r) {}
                    public void onUsage(int in, int out) {}
                    public void onDone(String reason) {}
                    public void onError(String error) {
                        synchronized (listener) { listener.onError("[" + name + "] " + error); }
                    }
                };
                agent.send(userInput, delegate);
                return buf.toString().trim();
            }, pool);

            futures.put(name, future);
        }

        // Collect all results
        Map<String, String> results = new LinkedHashMap<>();
        futures.forEach((name, future) -> {
            try {
                results.put(name, future.join());
            } catch (Exception e) {
                LOG.warning("Agent [" + name + "] failed: " + e.getMessage());
                results.put(name, "[Agent " + name + " failed: " + e.getMessage() + "]");
            }
        });

        // Fan-in: merge
        String merged = merger.apply(results);
        listener.onTextDelta("\n[Orchestrator] Merged results:\n" + merged);
        listener.onDone("stop");
    }

    /**
     * Routed dispatch — the router selects a single agent by name.
     */
    public void routed(String userInput, WayangAgentListener listener) {
        if (router == null) {
            listener.onError("AgentOrchestrator: no router configured. Use withRouter().");
            listener.onDone("stop");
            return;
        }

        String targetName = router.apply(userInput);
        Agent target = agents.get(targetName);

        if (target == null) {
            listener.onError("AgentOrchestrator: router returned unknown agent name: " + targetName);
            listener.onDone("stop");
            return;
        }

        listener.onTextDelta("\n[Orchestrator] Routing to agent [" + targetName + "]\n");
        target.send(userInput, listener);
    }

    // ── Introspection ────────────────────────────────────────────────────────

    public List<String> registeredAgents() {
        return new ArrayList<>(agents.keySet());
    }

    public int agentCount() {
        return agents.size();
    }
}
