package tech.kayys.wayang.agent.executor.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tech.kayys.wayang.agent.core.audit.AgentArtifact;

class FileAgentAuditServiceTest {

    private FileAgentAuditService service;
    private String tempDirPath;

    @BeforeEach
    void setUp() throws Exception {
        Path tempDir = Files.createTempDirectory("wayang-audit-test-");
        tempDirPath = tempDir.toString();
        service = new FileAgentAuditService();
        service.auditDirectory = tempDirPath;
    }

    @AfterEach
    void tearDown() throws Exception {
        // Clean up temp directory recursively
        try (var stream = Files.walk(Paths.get(tempDirPath))) {
            stream.sorted((a, b) -> b.compareTo(a))
                  .forEach(p -> {
                      try {
                          Files.delete(p);
                      } catch (Exception e) {}
                  });
        }
    }

    @Test
    void testSaveArtifactAndRetrieveList() {
        String runId = UUID.randomUUID().toString();
        String tenantId = "test-tenant";
        AgentArtifact artifact1 = new AgentArtifact(
                UUID.randomUUID().toString(),
                runId,
                tenantId,
                "plan",
                "# Execution Plan\n1. Do this.",
                "markdown",
                Map.of(),
                Instant.now()
        );

        AgentArtifact artifact2 = new AgentArtifact(
                UUID.randomUUID().toString(),
                runId,
                tenantId,
                "task",
                "- [ ] task 1",
                "markdown",
                Map.of(),
                Instant.now()
        );

        // Save
        service.saveArtifact(artifact1).await().indefinitely();
        service.saveArtifact(artifact2).await().indefinitely();

        // Check Files Exist
        Path tenantDir = Paths.get(tempDirPath, tenantId);
        Path runDir = tenantDir.resolve(runId);
        assertTrue(Files.exists(runDir), "Run directory should exist");

        // Retrieve List
        List<AgentArtifact> retrieved = service.getArtifactsByRun(runId, tenantId).await().indefinitely();
        assertEquals(2, retrieved.size(), "Should find 2 files on disk");
    }
}
