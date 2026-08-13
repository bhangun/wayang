package tech.kayys.wayang.execution.governance;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ToolBudgetTest {

    @Test
    void unlimited_always_allows() {
        ToolBudget b = ToolBudget.unlimited();
        assertTrue(b.consume(1000, 0.5));
        assertTrue(b.consume(999999, 999.0));
        assertFalse(b.isCallBudgetExhausted());
    }

    @Test
    void call_limit_enforced() {
        ToolBudget b = new ToolBudget("t1", "u1", 3, -1.0, -1);
        assertTrue(b.consume(0, 0));
        assertTrue(b.consume(0, 0));
        assertTrue(b.consume(0, 0));
        assertTrue(b.isCallBudgetExhausted());
        assertFalse(b.consume(0, 0)); // 4th call rejected
    }

    @Test
    void cost_limit_enforced() {
        ToolBudget b = new ToolBudget("t1", "u1", -1, 1.0, -1);
        assertTrue(b.consume(0, 0.4));
        assertTrue(b.consume(0, 0.4));
        assertFalse(b.consume(0, 0.4)); // 0.4+0.4+0.4 = 1.2 > 1.0
    }

    @Test
    void duration_limit_enforced() {
        ToolBudget b = new ToolBudget("t1", "u1", -1, -1.0, 1000);
        assertTrue(b.consume(400, 0));
        assertTrue(b.consume(400, 0));
        assertFalse(b.consume(400, 0)); // 400+400+400=1200 > 1000
    }
}
