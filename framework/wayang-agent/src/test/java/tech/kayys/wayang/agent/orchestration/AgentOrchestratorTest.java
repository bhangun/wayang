package tech.kayys.wayang.agent.orchestration;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.agent.Agent;
import tech.kayys.wayang.agent.PermissionDecision;
import tech.kayys.wayang.agent.WayangAgentListener;
import tech.kayys.wayang.tool.ToolResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class AgentOrchestratorTest {

    /** Stub Agent that emits fixed text then calls onDone. */
    static Agent fakeAgent(String output) {
        return new Agent() {
            public void send(String input, WayangAgentListener listener) {
                listener.onTextDelta(output);
                listener.onDone("stop");
            }
            public tech.kayys.wayang.spi.memory.Memory<tech.kayys.wayang.provider.ChatMessage> getMemory() { return null; }
            public void setMemory(tech.kayys.wayang.spi.memory.Memory<tech.kayys.wayang.provider.ChatMessage> m) {}
            public void setProvider(tech.kayys.wayang.provider.Provider p) {}
            public void setModelId(String id) {}
            public String getModelId() { return null; }
            public void setSystemPrompt(String sp) {}
            public java.util.Collection<tech.kayys.wayang.tool.Tool> tools() { return List.of(); }
            public void setTools(List<tech.kayys.wayang.tool.Tool> t) {}
            public boolean autoApproveTools() { return true; }
            public void setAutoApproveTools(boolean v) {}
            public java.nio.file.Path workspace() { return null; }
        };
    }

    /** Collecting listener — correct WayangAgentListener signature. */
    static WayangAgentListener collecting(List<String> out) {
        return new WayangAgentListener() {
            public void onTextDelta(String text)           { out.add(text); }
            public void onToolCallStart(String id, String n) {}
            public void onToolCallReady(String id, String n, Map<String, Object> input) {}
            public void onToolPermissionNeeded(String id, String n, Map<String, Object> input,
                    Consumer<PermissionDecision> r) {}
            public void onToolResult(String id, String n, ToolResult r) {}
            public void onUsage(int in, int out2) {}
            public void onDone(String r)           {}
            public void onError(String e)          { out.add("ERROR:" + e); }
        };
    }

    @Test
    void sequential_runs_in_order() {
        AgentOrchestrator orc = new AgentOrchestrator()
            .register("alpha", fakeAgent("ALPHA"))
            .register("beta",  fakeAgent("BETA"));
        List<String> out = new ArrayList<>();
        orc.sequential("input", collecting(out));
        String joined = String.join("", out);
        assertTrue(joined.contains("ALPHA"), "ALPHA missing");
        assertTrue(joined.contains("BETA"),  "BETA missing");
        assertTrue(joined.indexOf("ALPHA") < joined.indexOf("BETA"), "ALPHA must come before BETA");
    }

    @Test
    void parallel_calls_all_agents() {
        AtomicInteger calls = new AtomicInteger();
        Agent counter = new Agent() {
            public void send(String input, WayangAgentListener listener) {
                calls.incrementAndGet(); listener.onDone("stop");
            }
            public tech.kayys.wayang.spi.memory.Memory<tech.kayys.wayang.provider.ChatMessage> getMemory() { return null; }
            public void setMemory(tech.kayys.wayang.spi.memory.Memory<tech.kayys.wayang.provider.ChatMessage> m) {}
            public void setProvider(tech.kayys.wayang.provider.Provider p) {}
            public void setModelId(String id) {}
            public String getModelId() { return null; }
            public void setSystemPrompt(String sp) {}
            public java.util.Collection<tech.kayys.wayang.tool.Tool> tools() { return List.of(); }
            public void setTools(List<tech.kayys.wayang.tool.Tool> t) {}
            public boolean autoApproveTools() { return true; }
            public void setAutoApproveTools(boolean v) {}
            public java.nio.file.Path workspace() { return null; }
        };
        new AgentOrchestrator()
            .register("a", counter).register("b", counter).register("c", counter)
            .parallel("test", collecting(new ArrayList<>()));
        assertEquals(3, calls.get());
    }

    @Test
    void routed_dispatches_to_correct_agent() {
        List<String> out = new ArrayList<>();
        new AgentOrchestrator()
            .register("fast",     fakeAgent("FAST"))
            .register("thorough", fakeAgent("THOROUGH"))
            .withRouter(i -> i.contains("quick") ? "fast" : "thorough")
            .routed("quick question", collecting(out));
        assertTrue(String.join("", out).contains("FAST"));
    }

    @Test
    void empty_orchestrator_emits_error() {
        List<String> out = new ArrayList<>();
        new AgentOrchestrator().sequential("hi", collecting(out));
        assertTrue(out.stream().anyMatch(s -> s.startsWith("ERROR:")));
    }

    @Test
    void registration_metadata() {
        AgentOrchestrator orc = new AgentOrchestrator()
            .register("x", fakeAgent("")).register("y", fakeAgent(""));
        assertEquals(2, orc.agentCount());
        assertEquals(List.of("x", "y"), orc.registeredAgents());
    }
}
