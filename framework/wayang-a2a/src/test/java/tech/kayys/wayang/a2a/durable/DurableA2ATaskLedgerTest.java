package tech.kayys.wayang.a2a.durable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.a2a.model.A2ATaskStatus;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class DurableA2ATaskLedgerTest {

    private DurableA2ATaskLedger ledger;

    @BeforeEach
    void setUp() {
        ledger = new DurableA2ATaskLedger();
    }

    @Test
    void create_registers_task_as_pending() {
        DurableA2ATask task = ledger.create("exec-1", "http://remote/agent", "ckpt-1");
        assertNotNull(task.taskId());
        assertEquals(A2ATaskStatus.PENDING, task.status());
        assertEquals("exec-1", task.callerExecutionId());
        assertEquals("ckpt-1", task.checkpointId());
        assertEquals(1, ledger.taskCount());
    }

    @Test
    void update_transitions_status() {
        DurableA2ATask task = ledger.create("exec-2", "http://remote/agent", null);
        DurableA2ATask running = ledger.update(task.taskId(), A2ATaskStatus.RUNNING);
        assertNotNull(running);
        assertEquals(A2ATaskStatus.RUNNING, running.status());
        assertFalse(running.isTerminal());
    }

    @Test
    void complete_marks_terminal_and_sets_completedAt() {
        DurableA2ATask task = ledger.create("exec-3", "http://remote/agent", null);
        DurableA2ATask done = ledger.complete(task.taskId());
        assertEquals(A2ATaskStatus.COMPLETED, done.status());
        assertTrue(done.isTerminal());
        assertNotNull(done.completedAt());
    }

    @Test
    void cancel_marks_cancelled() {
        DurableA2ATask task = ledger.create("exec-4", "http://remote/agent", null);
        DurableA2ATask cancelled = ledger.cancel(task.taskId());
        assertEquals(A2ATaskStatus.CANCELLED, cancelled.status());
        assertTrue(cancelled.isTerminal());
    }

    @Test
    void retry_increments_counter_and_resets_pending() {
        DurableA2ATask task = ledger.create("exec-5", "http://remote/agent", null);
        ledger.update(task.taskId(), A2ATaskStatus.FAILED);
        DurableA2ATask retried = ledger.retry(task.taskId());
        assertEquals(A2ATaskStatus.PENDING, retried.status());
        assertEquals(1, retried.retryCount());
    }

    @Test
    void wait_state_resolves_when_task_completes() throws Exception {
        DurableA2ATask task = ledger.create("exec-6", "http://remote/agent", null);
        RemoteAgentWaitState ws = ledger.createWaitState(task.taskId(), Duration.ofSeconds(2));

        // Resolve asynchronously from another thread
        Thread.ofVirtual().start(() -> {
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            ledger.complete(task.taskId());
        });

        DurableA2ATask resolved = ws.await();
        assertEquals(A2ATaskStatus.COMPLETED, resolved.status());
    }

    @Test
    void wait_state_times_out() {
        DurableA2ATask task = ledger.create("exec-7", "http://remote/agent", null);
        RemoteAgentWaitState ws = ledger.createWaitState(task.taskId(), Duration.ofMillis(100));

        assertThrows(TimeoutException.class, ws::await);
    }

    @Test
    void find_returns_empty_for_unknown_task() {
        Optional<DurableA2ATask> result = ledger.find("nonexistent");
        assertTrue(result.isEmpty());
    }

    @Test
    void byCallerExecution_filters_correctly() {
        ledger.create("caller-A", "http://a/agent", null);
        ledger.create("caller-A", "http://b/agent", null);
        ledger.create("caller-B", "http://c/agent", null);
        assertEquals(2, ledger.byCallerExecution("caller-A").size());
        assertEquals(1, ledger.byCallerExecution("caller-B").size());
    }

    @Test
    void checkpoint_and_eventseq_update() {
        DurableA2ATask task = ledger.create("exec-8", "http://remote/agent", null);
        ledger.updateCheckpoint(task.taskId(), "ckpt-99");
        ledger.updateEventSeq(task.taskId(), 42L);
        DurableA2ATask found = ledger.find(task.taskId()).orElseThrow();
        assertEquals("ckpt-99", found.checkpointId());
        assertEquals(42L, found.lastEventSeq());
    }
}
