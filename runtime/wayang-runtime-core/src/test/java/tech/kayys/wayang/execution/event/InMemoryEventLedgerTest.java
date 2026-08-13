package tech.kayys.wayang.execution.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryEventLedgerTest {

    private InMemoryEventLedger ledger;
    private static final String EX_ID = "test-execution-1";

    @BeforeEach
    void setup() {
        ledger = new InMemoryEventLedger();
    }

    @Test
    void record_and_retrieve_events() {
        ledger.record(ExecutionEvent.of(EX_ID, 0, ExecutionEventType.EXECUTION_STARTED, "kernel", Map.of()));
        ledger.record(ExecutionEvent.of(EX_ID, 1, ExecutionEventType.TOOL_EXECUTED,      "executor", Map.of("tool", "web.search")));
        ledger.record(ExecutionEvent.of(EX_ID, 2, ExecutionEventType.EXECUTION_COMPLETED,"kernel", Map.of()));

        List<ExecutionEvent> events = ledger.events(EX_ID);
        assertEquals(3, events.size());
        assertEquals(ExecutionEventType.EXECUTION_STARTED,   events.get(0).type());
        assertEquals(ExecutionEventType.TOOL_EXECUTED,       events.get(1).type());
        assertEquals(ExecutionEventType.EXECUTION_COMPLETED, events.get(2).type());
    }

    @Test
    void filter_by_type() {
        ledger.record(ExecutionEvent.of(EX_ID, 0, ExecutionEventType.TOOL_EXECUTED, "exec", Map.of()));
        ledger.record(ExecutionEvent.of(EX_ID, 1, ExecutionEventType.TOOL_FAILED,   "exec", Map.of()));
        ledger.record(ExecutionEvent.of(EX_ID, 2, ExecutionEventType.TOOL_EXECUTED, "exec", Map.of()));

        List<ExecutionEvent> toolDone = ledger.events(EX_ID, ExecutionEventType.TOOL_EXECUTED);
        assertEquals(2, toolDone.size());
    }

    @Test
    void metrics_updated_on_record() {
        ledger.record(ExecutionEvent.of(EX_ID, 0, ExecutionEventType.EXECUTION_STARTED, "kernel", Map.of()));
        ledger.record(ExecutionEvent.of(EX_ID, 1, ExecutionEventType.TOOL_EXECUTED,      "exec", Map.of()));
        ledger.record(ExecutionEvent.of(EX_ID, 2, ExecutionEventType.TOOL_CACHE_HIT,     "cache", Map.of()));
        ledger.record(ExecutionEvent.of(EX_ID, 3, ExecutionEventType.EXECUTION_COMPLETED,"kernel", Map.of()));

        Optional<ExecutionMetrics> metricsOpt = ledger.metrics(EX_ID);
        assertTrue(metricsOpt.isPresent());
        ExecutionMetrics m = metricsOpt.get();
        assertEquals(1, m.toolCalls());
        assertEquals(1, m.cacheHits());
        assertTrue(m.latency().toMillis() >= 0, "Latency should be non-negative");
    }

    @Test
    void purge_removes_data() {
        ledger.record(ExecutionEvent.of(EX_ID, 0, ExecutionEventType.EXECUTION_STARTED, "kernel", Map.of()));
        assertEquals(1, ledger.totalEventCount());

        ledger.purge(EX_ID);

        assertTrue(ledger.events(EX_ID).isEmpty());
        assertTrue(ledger.metrics(EX_ID).isEmpty());
        assertEquals(0, ledger.totalEventCount());
    }

    @Test
    void total_event_count_across_executions() {
        ledger.record(ExecutionEvent.of("exec-a", 0, ExecutionEventType.TOOL_EXECUTED, "a", Map.of()));
        ledger.record(ExecutionEvent.of("exec-b", 0, ExecutionEventType.TOOL_EXECUTED, "b", Map.of()));
        ledger.record(ExecutionEvent.of("exec-b", 1, ExecutionEventType.TOOL_EXECUTED, "b", Map.of()));

        assertEquals(3, ledger.totalEventCount());
    }

    @Test
    void null_event_does_not_throw() {
        assertDoesNotThrow(() -> ledger.record(null));
        assertEquals(0, ledger.totalEventCount());
    }

    @Test
    void metrics_track_token_usage() {
        ledger.record(ExecutionEvent.of(EX_ID, 0, ExecutionEventType.MODEL_RESPONSE_RECEIVED, "model",
            Map.of("inputTokens", 500L, "outputTokens", 200L)));

        ExecutionMetrics m = ledger.metrics(EX_ID).orElseThrow();
        assertEquals(500L, m.inputTokens());
        assertEquals(200L, m.outputTokens());
    }
}
