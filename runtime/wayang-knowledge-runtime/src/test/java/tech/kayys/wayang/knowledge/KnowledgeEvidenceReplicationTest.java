package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.replication.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeEvidenceReplicationTest {

    @Test
    public void testReplicaRegistryAndPlanning() {
        InMemoryKnowledgeEvidenceArtifactReplicaRegistry registry =
                new InMemoryKnowledgeEvidenceArtifactReplicaRegistry();

        Instant now = Instant.now();
        KnowledgeEvidenceArtifactReplica replica1 = new KnowledgeEvidenceArtifactReplica(
                "art-1", "runtime-a", "tenant-1",
                KnowledgeEvidenceArtifactReplicaState.AVAILABLE,
                2048L, "sha256:art1", "merkle-1",
                now, now, now, Map.of()
        );

        registry.register(replica1);
        var found = registry.findByArtifact("art-1");
        assertFalse(found.isEmpty());
        assertEquals("runtime-a", found.get(0).runtimeId());

        KnowledgeEvidenceArtifactReplicaLocation loc1 = new KnowledgeEvidenceArtifactReplicaLocation(
                "runtime-a", "ep-1", "tenant-1", "us-east", true, true, Map.of()
        );
        KnowledgeEvidenceArtifactReplicaLocation loc2 = new KnowledgeEvidenceArtifactReplicaLocation(
                "runtime-b", "ep-2", "tenant-1", "us-west", false, true, Map.of()
        );

        DefaultKnowledgeEvidenceArtifactReplicationPlanner planner =
                new DefaultKnowledgeEvidenceArtifactReplicationPlanner(List.of(loc1, loc2));

        KnowledgeEvidenceArtifactReplicationPolicy policy = new KnowledgeEvidenceArtifactReplicationPolicy(
                1, 2, true, true, true, 1024 * 1024L, Map.of()
        );

        var selected = planner.plan("art-1", "tenant-1", "ws-1", "proj-1", policy);
        assertNotNull(selected);
        assertTrue(selected.size() <= 2);
    }
}
