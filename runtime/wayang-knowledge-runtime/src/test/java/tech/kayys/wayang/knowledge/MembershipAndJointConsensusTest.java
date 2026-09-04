package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.attestation.KnowledgeAnswerResolutionConsensusEpoch;
import tech.kayys.wayang.knowledge.exchange.attestation.KnowledgeAnswerResolutionVote;
import tech.kayys.wayang.knowledge.exchange.membership.DefaultKnowledgeAnswerResolutionConsensusMembershipValidator;
import tech.kayys.wayang.knowledge.exchange.membership.DefaultKnowledgeAnswerResolutionEpochFinalizationGuard;
import tech.kayys.wayang.knowledge.exchange.membership.DefaultKnowledgeAnswerResolutionEpochService;
import tech.kayys.wayang.knowledge.exchange.membership.DefaultKnowledgeAnswerResolutionJointQuorumEvaluator;
import tech.kayys.wayang.knowledge.exchange.membership.DefaultKnowledgeAnswerResolutionMembershipChangeValidator;
import tech.kayys.wayang.knowledge.exchange.membership.DefaultKnowledgeAnswerResolutionMembershipFinalizer;
import tech.kayys.wayang.knowledge.exchange.membership.DefaultKnowledgeAnswerResolutionMembershipFingerprinter;
import tech.kayys.wayang.knowledge.exchange.membership.DefaultKnowledgeAnswerResolutionMembershipTransitionEngine;
import tech.kayys.wayang.knowledge.exchange.membership.KnowledgeAnswerResolutionJointMembership;
import tech.kayys.wayang.knowledge.exchange.membership.KnowledgeAnswerResolutionJointQuorumDecision;
import tech.kayys.wayang.knowledge.exchange.membership.KnowledgeAnswerResolutionMembership;
import tech.kayys.wayang.knowledge.exchange.membership.KnowledgeAnswerResolutionMembershipChange;
import tech.kayys.wayang.knowledge.exchange.membership.KnowledgeAnswerResolutionMembershipChangeType;
import tech.kayys.wayang.knowledge.exchange.membership.KnowledgeAnswerResolutionMembershipSet;
import tech.kayys.wayang.knowledge.exchange.membership.KnowledgeAnswerResolutionMembershipState;
import tech.kayys.wayang.knowledge.exchange.membership.KnowledgeAnswerResolutionMembershipTransition;
import tech.kayys.wayang.knowledge.exchange.membership.KnowledgeAnswerResolutionMembershipTransitionResult;
import tech.kayys.wayang.knowledge.exchange.membership.KnowledgeAnswerResolutionMembershipTransitionState;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MembershipAndJointConsensusTest {

    @Test
    @DisplayName("P065: Test Dynamic Membership and Epoch Creation")
    void testMembershipAndEpochCreation() {
        Instant now = Instant.now();
        KnowledgeAnswerResolutionMembership m1 = new KnowledgeAnswerResolutionMembership(
                "r1", "t1", "w1", "p1", KnowledgeAnswerResolutionMembershipState.ACTIVE, now, null, null);
        KnowledgeAnswerResolutionMembership m2 = new KnowledgeAnswerResolutionMembership(
                "r2", "t1", "w1", "p1", KnowledgeAnswerResolutionMembershipState.ACTIVE, now, null, null);
        KnowledgeAnswerResolutionMembership m3 = new KnowledgeAnswerResolutionMembership(
                "r3", "t1", "w1", "p1", KnowledgeAnswerResolutionMembershipState.ACTIVE, now, null, null);

        var fingerprinter = new DefaultKnowledgeAnswerResolutionMembershipFingerprinter();

        KnowledgeAnswerResolutionMembershipSet set = new KnowledgeAnswerResolutionMembershipSet(
                "epoch-1", 1L, List.of(m1, m2, m3), 2, "fp-placeholder", now, null);

        String fp = fingerprinter.fingerprint(set);
        assertNotNull(fp);
        assertFalse(fp.isBlank());

        set = new KnowledgeAnswerResolutionMembershipSet(
                "epoch-1", 1L, List.of(m1, m2, m3), 2, fp, now, null);

        var epochService = new DefaultKnowledgeAnswerResolutionEpochService();
        KnowledgeAnswerResolutionConsensusEpoch epoch = epochService.create(set, null, now);

        assertEquals(0L, epoch.sequence());
        assertEquals(fp, epoch.participantSetFingerprint());
        assertTrue(epoch.effectiveAt(now));

        assertTrue(epochService.validate(epoch, set, now));

        var changeValidator = new DefaultKnowledgeAnswerResolutionMembershipChangeValidator();
        KnowledgeAnswerResolutionMembershipChange change = new KnowledgeAnswerResolutionMembershipChange(
                "c1", "epoch-1", KnowledgeAnswerResolutionMembershipChangeType.ADD_RUNTIME,
                List.of("r1", "r2", "r3"), List.of("r1", "r2", "r3", "r4", "r5"), 3,
                "r1", now, now, "scaling cluster", null);

        assertTrue(changeValidator.validate(change, set));
    }

    @Test
    @DisplayName("P066: Test Joint Consensus Evaluation and Safe Transition Finalization")
    void testJointConsensusAndFinalization() {
        Instant now = Instant.now();
        KnowledgeAnswerResolutionJointMembership joint = new KnowledgeAnswerResolutionJointMembership(
                "trans-1", "epoch-1", "epoch-2",
                List.of("r1", "r2", "r3"),
                List.of("r1", "r2", "r3", "r4", "r5"),
                2, 3,
                now, null
        );

        assertTrue(joint.contains("r1"));
        assertTrue(joint.contains("r4"));
        assertFalse(joint.contains("r9"));

        // Only r1 and r2 vote -> satisfies old quorum (2), but NOT new quorum (3 required)
        KnowledgeAnswerResolutionVote v1 = new KnowledgeAnswerResolutionVote(
                "v1", "trans-1", "kf1", "r1", "t1", "w1", "p1", "rf1", "df1", null, true, true, now, null, Map.of());
        KnowledgeAnswerResolutionVote v2 = new KnowledgeAnswerResolutionVote(
                "v2", "trans-1", "kf1", "r2", "t1", "w1", "p1", "rf1", "df1", null, true, true, now, null, Map.of());

        var evaluator = new DefaultKnowledgeAnswerResolutionJointQuorumEvaluator();
        KnowledgeAnswerResolutionJointQuorumDecision partialDecision = evaluator.evaluate(joint, List.of(v1, v2));

        assertTrue(partialDecision.oldQuorumReached());
        assertFalse(partialDecision.newQuorumReached());
        assertFalse(partialDecision.reached());

        // Now r4 votes -> satisfies both old (2) and new (3: r1, r2, r4)
        KnowledgeAnswerResolutionVote v4 = new KnowledgeAnswerResolutionVote(
                "v4", "trans-1", "kf1", "r4", "t1", "w1", "p1", "rf1", "df1", null, true, true, now, null, Map.of());

        KnowledgeAnswerResolutionJointQuorumDecision fullDecision = evaluator.evaluate(joint, List.of(v1, v2, v4));
        assertTrue(fullDecision.reached());

        KnowledgeAnswerResolutionMembershipTransition transition = new KnowledgeAnswerResolutionMembershipTransition(
                "trans-1", KnowledgeAnswerResolutionMembershipChangeType.ADD_RUNTIME,
                "epoch-1", "epoch-2",
                List.of("r1", "r2", "r3"),
                List.of("r1", "r2", "r3", "r4", "r5"),
                2, 3,
                "r1", now, now, "Cluster expansion", null
        );

        var transitionEngine = new DefaultKnowledgeAnswerResolutionMembershipTransitionEngine(evaluator);
        KnowledgeAnswerResolutionMembershipTransitionResult result = transitionEngine.evaluate(transition, joint, List.of(v1, v2, v4));

        assertEquals(KnowledgeAnswerResolutionMembershipTransitionState.FINALIZING, result.state());
        assertTrue(result.safeToFinalize());

        var guard = new DefaultKnowledgeAnswerResolutionEpochFinalizationGuard();
        assertTrue(guard.canFinalize(result));

        var finalizer = new DefaultKnowledgeAnswerResolutionMembershipFinalizer(guard);
        KnowledgeAnswerResolutionMembershipTransitionResult completed = finalizer.finalizeTransition(transition, result);

        assertEquals(KnowledgeAnswerResolutionMembershipTransitionState.COMPLETED, completed.state());
        assertEquals("epoch-2", completed.newEpochId());
    }
}
