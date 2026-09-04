package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.journal.DefaultKnowledgeAnswerResolutionCommitService;
import tech.kayys.wayang.knowledge.exchange.journal.DefaultKnowledgeAnswerResolutionJournalAppender;
import tech.kayys.wayang.knowledge.exchange.journal.DefaultKnowledgeAnswerResolutionJournalRecovery;
import tech.kayys.wayang.knowledge.exchange.journal.InMemoryKnowledgeAnswerResolutionJournal;
import tech.kayys.wayang.knowledge.exchange.journal.KnowledgeAnswerResolutionCommitIndex;
import tech.kayys.wayang.knowledge.exchange.journal.KnowledgeAnswerResolutionJournal;
import tech.kayys.wayang.knowledge.exchange.journal.KnowledgeAnswerResolutionJournalState;
import tech.kayys.wayang.knowledge.exchange.journal.KnowledgeAnswerResolutionLogEntry;
import tech.kayys.wayang.knowledge.exchange.journal.KnowledgeAnswerResolutionLogEntryType;
import tech.kayys.wayang.knowledge.exchange.statemachine.DefaultKnowledgeAnswerResolutionApplyEngine;
import tech.kayys.wayang.knowledge.exchange.statemachine.DefaultKnowledgeAnswerResolutionStateCanonicalizer;
import tech.kayys.wayang.knowledge.exchange.statemachine.DefaultKnowledgeAnswerResolutionStateFingerprinter;
import tech.kayys.wayang.knowledge.exchange.statemachine.DefaultKnowledgeAnswerResolutionStateMachine;
import tech.kayys.wayang.knowledge.exchange.statemachine.KnowledgeAnswerResolutionConsensusStateStatus;
import tech.kayys.wayang.knowledge.exchange.statemachine.KnowledgeAnswerResolutionState;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConsensusJournalAndStateMachineTest {

    @Test
    @DisplayName("P067: Test Consensus Journal Append, Monotonicity & Commit Service")
    void testConsensusJournalAndCommit() {
        KnowledgeAnswerResolutionJournal journal = new InMemoryKnowledgeAnswerResolutionJournal();
        var appender = new DefaultKnowledgeAnswerResolutionJournalAppender(journal);
        Instant now = Instant.now();

        KnowledgeAnswerResolutionLogEntry e0 = appender.append(
                1L, "epoch-1", "key-1", "cons-1", "r1",
                KnowledgeAnswerResolutionLogEntryType.PROPOSAL_CREATED, "payload-hash-1", now, null);
        assertEquals(0L, e0.index());

        KnowledgeAnswerResolutionLogEntry e1 = appender.append(
                1L, "epoch-1", "key-1", "cons-1", "r2",
                KnowledgeAnswerResolutionLogEntryType.CONSENSUS_ACTIVATED, "payload-hash-1", now, null);
        assertEquals(1L, e1.index());

        assertEquals(1L, journal.lastIndex());
        assertTrue(journal.get(0L).isPresent());
        assertEquals(2, journal.range(0L, 1L).size());

        var commitService = new DefaultKnowledgeAnswerResolutionCommitService();
        KnowledgeAnswerResolutionCommitIndex ci = commitService.commitThrough(1L, 1L);
        assertEquals(1L, ci.committedIndex());
        assertEquals(-1L, ci.appliedIndex());

        commitService.applyThrough(0L);
        assertEquals(0L, commitService.currentIndex().appliedIndex());

        var recovery = new DefaultKnowledgeAnswerResolutionJournalRecovery();
        KnowledgeAnswerResolutionJournalState recoveredState = recovery.recover(journal, 0L);
        assertEquals(1L, recoveredState.lastIndex());
        assertEquals(1L, recoveredState.committedIndex());
        assertEquals(0L, recoveredState.appliedIndex());
        assertEquals("epoch-1", recoveredState.currentEpochId());
    }

    @Test
    @DisplayName("P068: Test State Machine Apply Engine & Canonical State Fingerprinting")
    void testStateMachineAndFingerprint() {
        var fingerprinter = new DefaultKnowledgeAnswerResolutionStateFingerprinter(
                new DefaultKnowledgeAnswerResolutionStateCanonicalizer());
        var stateMachine = new DefaultKnowledgeAnswerResolutionStateMachine(fingerprinter);
        var commitService = new DefaultKnowledgeAnswerResolutionCommitService();
        commitService.commitThrough(1L, 1L);

        var applyEngine = new DefaultKnowledgeAnswerResolutionApplyEngine(stateMachine, commitService);
        Instant now = Instant.now();

        KnowledgeAnswerResolutionLogEntry e0 = new KnowledgeAnswerResolutionLogEntry(
                0L, 1L, "epoch-1", "key-fp-1", "cons-1", "r1",
                KnowledgeAnswerResolutionLogEntryType.PROPOSAL_CREATED, "payload-fp-1", now, null);

        applyEngine.apply(e0);
        assertEquals(0L, applyEngine.lastAppliedIndex());
        KnowledgeAnswerResolutionState s0 = applyEngine.state();
        assertEquals(KnowledgeAnswerResolutionConsensusStateStatus.PROPOSED, s0.consensuses().get("cons-1").status());

        KnowledgeAnswerResolutionLogEntry e1 = new KnowledgeAnswerResolutionLogEntry(
                1L, 1L, "epoch-1", "key-fp-1", "cons-1", "r1",
                KnowledgeAnswerResolutionLogEntryType.CONSENSUS_ACTIVATED, "payload-fp-1", now, null);

        applyEngine.apply(e1);
        assertEquals(1L, applyEngine.lastAppliedIndex());
        KnowledgeAnswerResolutionState s1 = applyEngine.state();
        assertEquals(KnowledgeAnswerResolutionConsensusStateStatus.ACTIVE, s1.consensuses().get("cons-1").status());

        String fp = applyEngine.stateFingerprint();
        assertNotNull(fp);
        assertFalse(fp.isBlank());
    }
}
