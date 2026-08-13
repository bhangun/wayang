package tech.kayys.wayang.memory;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.project.ProjectContext;
import static org.junit.jupiter.api.Assertions.*;

class MemoryScopeTest {

    @Test
    void testProjectContext() {
        ProjectContext ctx = new ProjectContext("proj-1", "tenant-1", "user-1", "ws-1");
        assertEquals("proj-1", ctx.projectId());
        assertEquals("tenant-1", ctx.tenantId());
        assertEquals("user-1", ctx.userId());
        assertEquals("ws-1", ctx.workspaceId());
    }

    @Test
    void testMemoryScopeValues() {
        assertNotNull(MemoryScope.EXECUTION);
        assertNotNull(MemoryScope.CONVERSATION);
        assertNotNull(MemoryScope.PROJECT);
        assertNotNull(MemoryScope.USER);
        assertNotNull(MemoryScope.TENANT);
    }
}
