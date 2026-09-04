package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.checkpoint.DefaultKnowledgeAnswerResolutionCompactor;
import tech.kayys.wayang.knowledge.exchange.checkpoint.InMemoryKnowledgeAnswerResolutionCheckpointStore;
import tech.kayys.wayang.knowledge.exchange.checkpoint.KnowledgeAnswerResolutionCheckpointPolicy;
import tech.kayys.wayang.knowledge.exchange.checkpoint.KnowledgeAnswerResolutionCheckpointStatus;
import tech.kayys.wayang.knowledge.exchange.checkpoint.KnowledgeAnswerResolutionCheckpointStore;
import tech.kayys.wayang.knowledge.exchange.checkpoint.KnowledgeAnswerResolutionStateCheckpoint;
import tech.kayys.wayang.knowledge.exchange.journal.InMemoryKnowledgeAnswerResolutionJournal;
import tech.kayys.wayang.knowledge.exchange.journal.KnowledgeAnswerResolutionJournal;
import tech.kayys.wayang.knowledge.exchange.journal.KnowledgeAnswerResolutionLogEntry;
import tech.kayys.wayang.knowledge.exchange.journal.KnowledgeAnswerResolutionLogEntryType;
import tech.kayys.wayang.knowledge.exchange.statemachine.KnowledgeAnswerResolutionState;
import tech.kayys.wayang.knowledge.exchange.transfer.InMemoryKnowledgeAnswerResolutionSnapshotTransferStore;
import tech.kayys.wayang.knowledge.exchange.transfer.KnowledgeAnswerResolutionSnapshotDescriptor;
import tech.kayys.wayang.knowledge.exchange.transfer.KnowledgeAnswerResolutionSnapshotTransferChunk;
import tech.kayys.wayang.knowledge.exchange.transfer.KnowledgeAnswerResolutionSnapshotTransferState;
import tech.kayys.wayang.knowledge.exchange.transfer.KnowledgeAnswerResolutionSnapshotTransferStatus;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CheckpointAndSnapshotTransferTest {

    @Test
    @DisplayName("P069: Test State Checkpoint Storage & Compactor")
    void testCheckpointAndCompactor() {
        KnowledgeAnswerResolutionCheckpointStore store = new InMemoryKnowledgeAnswerResolutionCheckpointStore();
        Instant now = Instant.now();

        KnowledgeAnswerResolutionState state = new KnowledgeAnswerResolutionState(
                100L, 1L, "epoch-1", Map.of(), Set.of(), Set.of(), Set.of());

        KnowledgeAnswerResolutionStateCheckpoint cp = new KnowledgeAnswerResolutionStateCheckpoint(
                100L, 1L, "epoch-1", "fp-100", state, now,
                KnowledgeAnswerResolutionCheckpointStatus.ACTIVE
        );

        store.save(cp);
        assertTrue(store.latest().isPresent());
        assertEquals(100L, store.latest().get().lastAppliedIndex());
        assertEquals(KnowledgeAnswerResolutionCheckpointStatus.ACTIVE, store.latest().get().status());

        KnowledgeAnswerResolutionJournal journal = new InMemoryKnowledgeAnswerResolutionJournal();
        for (int i = 0; i <= 200; i++) {
            journal.append(new KnowledgeAnswerResolutionLogEntry(
                    i, 1L, "epoch-1", "key", "cons", "r1",
                    KnowledgeAnswerResolutionLogEntryType.VOTE_RECORDED, "pf", now, null));
        }

        var policy = new KnowledgeAnswerResolutionCheckpointPolicy(50, 50, 2, true, true);
        var compactor = new DefaultKnowledgeAnswerResolutionCompactor();

        long compacted = compactor.compact(journal, store, policy);
        assertEquals(100L, compacted);
    }

    @Test
    @DisplayName("P070: Test Snapshot Transfer Protocol Models and Store")
    void testSnapshotTransfer() {
        Instant now = Instant.now();
        KnowledgeAnswerResolutionSnapshotDescriptor desc = new KnowledgeAnswerResolutionSnapshotDescriptor(
                "snap-1", "r1", "t1", "epoch-1", 100L, 1L, 1024L, 256,
                "state-fp-1", "merkle-root-1", now, now.plusSeconds(3600)
        );

        assertEquals("snap-1", desc.snapshotId());
        assertEquals(1024L, desc.totalBytes());

        KnowledgeAnswerResolutionSnapshotTransferChunk chunk = new KnowledgeAnswerResolutionSnapshotTransferChunk(
                "transfer-1", "snap-1", 0L, "test payload".getBytes(), "chunk-fp", "proof"
        );
        assertEquals(0L, chunk.offset());
        assertNotNull(chunk.data());

        var transferStore = new InMemoryKnowledgeAnswerResolutionSnapshotTransferStore();
        KnowledgeAnswerResolutionSnapshotTransferState state = new KnowledgeAnswerResolutionSnapshotTransferState(
                "transfer-1", "snap-1", "r1", "r2",
                KnowledgeAnswerResolutionSnapshotTransferStatus.TRANSFERRING,
                1024L, 256L, 256L, "state-fp-1", "merkle-root-1", now, now
        );

        transferStore.save(state);
        assertTrue(transferStore.get("transfer-1").isPresent());
        assertEquals(KnowledgeAnswerResolutionSnapshotTransferStatus.TRANSFERRING, transferStore.get("transfer-1").get().status());
    }
}
