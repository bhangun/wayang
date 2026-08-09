package tech.kayys.wayang.agent.spi.approval;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ApprovalStrategyTest {

    @Test
    public void testExceptionStructure() {
        ApprovalRequiredException ex = new ApprovalRequiredException("Requires human approval", "task-123");
        assertEquals("task-123", ex.getTaskId());
        assertEquals("Requires human approval", ex.getMessage());
    }
}
