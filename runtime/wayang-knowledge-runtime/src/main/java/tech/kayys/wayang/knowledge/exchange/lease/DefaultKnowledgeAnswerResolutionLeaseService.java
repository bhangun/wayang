package tech.kayys.wayang.knowledge.exchange.lease;

import tech.kayys.wayang.knowledge.*;
import tech.kayys.wayang.knowledge.seal.*;
import tech.kayys.wayang.knowledge.snapshot.*;
import tech.kayys.wayang.knowledge.snapshot.pack.*;
import tech.kayys.wayang.knowledge.snapshot.artifact.*;
import tech.kayys.wayang.knowledge.snapshot.merkle.*;
import tech.kayys.wayang.knowledge.exchange.*;
import tech.kayys.wayang.knowledge.exchange.auth.*;
import tech.kayys.wayang.knowledge.exchange.session.*;
import tech.kayys.wayang.knowledge.exchange.binding.*;
import tech.kayys.wayang.knowledge.exchange.envelope.*;
import tech.kayys.wayang.knowledge.exchange.trust.*;
import tech.kayys.wayang.knowledge.exchange.identity.*;
import tech.kayys.wayang.knowledge.exchange.capability.*;
import tech.kayys.wayang.knowledge.exchange.protocol.*;
import tech.kayys.wayang.knowledge.exchange.transport.*;
import tech.kayys.wayang.knowledge.exchange.framing.*;
import tech.kayys.wayang.knowledge.exchange.transfer.*;
import tech.kayys.wayang.knowledge.exchange.replication.*;
import tech.kayys.wayang.knowledge.exchange.sync.*;
import tech.kayys.wayang.knowledge.exchange.federation.*;
import tech.kayys.wayang.knowledge.exchange.routing.*;
import tech.kayys.wayang.knowledge.exchange.fusion.*;
import tech.kayys.wayang.knowledge.exchange.coverage.*;
import tech.kayys.wayang.knowledge.exchange.gap.*;
import tech.kayys.wayang.knowledge.exchange.attribution.*;
import tech.kayys.wayang.knowledge.exchange.contradiction.*;
import tech.kayys.wayang.knowledge.exchange.factuality.*;
import tech.kayys.wayang.knowledge.exchange.uncertainty.*;
import tech.kayys.wayang.knowledge.exchange.compact.*;
import tech.kayys.wayang.knowledge.exchange.resolution.*;
import tech.kayys.wayang.knowledge.exchange.quorum.*;
import tech.kayys.wayang.knowledge.exchange.selection.*;
import tech.kayys.wayang.knowledge.exchange.coordination.*;
import tech.kayys.wayang.knowledge.exchange.attestation.*;
import tech.kayys.wayang.knowledge.exchange.proof.*;
import tech.kayys.wayang.knowledge.exchange.validity.*;
import tech.kayys.wayang.knowledge.exchange.lease.*;
import tech.kayys.wayang.knowledge.exchange.recovery.*;


import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class DefaultKnowledgeAnswerResolutionLeaseService
        implements KnowledgeAnswerResolutionLeaseService {

    private final KnowledgeAnswerResolutionLeaseStore leaseStore;
    private final KnowledgeAnswerResolutionFreshnessEvaluator evaluator;

    public DefaultKnowledgeAnswerResolutionLeaseService(
            KnowledgeAnswerResolutionLeaseStore leaseStore,
            KnowledgeAnswerResolutionFreshnessEvaluator evaluator) {

        this.leaseStore = leaseStore;
        this.evaluator = evaluator;
    }

    @Override
    public KnowledgeAnswerResolutionLease issue(
            String consensusId,
            String keyFingerprint,
            String runtimeId,
            KnowledgeAnswerResolutionFreshnessPolicy policy,
            Instant now) {

        KnowledgeAnswerResolutionLease lease =
                new KnowledgeAnswerResolutionLease(
                        UUID.randomUUID().toString(),
                        consensusId,
                        keyFingerprint,
                        runtimeId,
                        now,
                        now.plus(policy.leaseDuration()),
                        now,
                        0,
                        "issued"
                );

        leaseStore.put(lease);

        return lease;
    }

    @Override
    public KnowledgeAnswerResolutionLease renew(
            KnowledgeAnswerResolutionLease lease,
            KnowledgeAnswerResolutionFreshnessPolicy policy,
            Instant now) {

        if (now.isBefore(lease.issuedAt())) {
            throw new IllegalArgumentException(
                    "Cannot renew lease before issuance");
        }

        KnowledgeAnswerResolutionLease renewed =
                new KnowledgeAnswerResolutionLease(
                        lease.leaseId(),
                        lease.consensusId(),
                        lease.keyFingerprint(),
                        lease.runtimeId(),
                        lease.issuedAt(),
                        now.plus(policy.leaseDuration()),
                        now,
                        lease.renewalSequence() + 1,
                        "renewed"
                );

        leaseStore.put(renewed);

        return renewed;
    }

    @Override
    public KnowledgeAnswerResolutionFreshnessDecision evaluate(
            KnowledgeAnswerResolutionLease lease,
            List<String> participantRuntimeIds,
            KnowledgeAnswerResolutionFreshnessPolicy policy,
            Instant now) {

        return evaluator.evaluate(
                lease,
                participantRuntimeIds,
                policy,
                now
        );
    }
}
