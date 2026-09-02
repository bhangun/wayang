package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.sync.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeEvidenceSyncTest {

    @Test
    public void testInventoryReconciliationAndPlanner() {
        DefaultKnowledgeEvidenceArtifactInventoryReconciler reconciler =
                new DefaultKnowledgeEvidenceArtifactInventoryReconciler();

        Instant now = Instant.now();
        KnowledgeEvidenceArtifactInventoryEntry entry1 = new KnowledgeEvidenceArtifactInventoryEntry(
                "art-1", 1024L, "sha256:art1", "merkle-1", false, now, now, Map.of()
        );
        KnowledgeEvidenceArtifactInventoryEntry entry2 = new KnowledgeEvidenceArtifactInventoryEntry(
                "art-2", 2048L, "sha256:art2", "merkle-2", false, now, now, Map.of()
        );

        var snapshot1 = new KnowledgeEvidenceArtifactInventorySnapshot(
                "runtime-1", "tenant-1", "ws-1", "proj-1", List.of(entry1, entry2), "fp-1", now, now.plusSeconds(3600), Map.of()
        );
        var snapshot2 = new KnowledgeEvidenceArtifactInventorySnapshot(
                "runtime-2", "tenant-1", "ws-1", "proj-1", List.of(entry1), "fp-2", now, now.plusSeconds(3600), Map.of()
        );

        var result = reconciler.reconcile(snapshot1, snapshot2);
        assertNotNull(result);
        assertFalse(result.missingRemotely().isEmpty());
        assertEquals("art-2", result.missingRemotely().get(0));

        DefaultKnowledgeEvidenceArtifactSynchronizationPlanner planner =
                new DefaultKnowledgeEvidenceArtifactSynchronizationPlanner();

        var plan = planner.plan(result);
        assertNotNull(plan);
        assertEquals(List.of("art-2"), plan.push());
    }
}
