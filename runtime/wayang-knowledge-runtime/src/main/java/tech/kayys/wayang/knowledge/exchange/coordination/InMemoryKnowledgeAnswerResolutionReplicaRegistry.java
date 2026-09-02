package tech.kayys.wayang.knowledge.exchange.coordination;

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


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class
InMemoryKnowledgeAnswerResolutionReplicaRegistry
        implements KnowledgeAnswerResolutionReplicaRegistry {

    private final ConcurrentMap<
            String,
            ConcurrentMap<
                    String,
                    KnowledgeAnswerResolutionReplica>>
            replicas =
            new ConcurrentHashMap<>();

    @Override
    public void register(
            KnowledgeAnswerResolutionReplica replica) {

        replicas
                .computeIfAbsent(
                        replica.keyFingerprint(),
                        ignored ->
                                new ConcurrentHashMap<>()
                )
                .put(
                        replica.runtimeId(),
                        replica
                );
    }

    @Override
    public Optional<
            KnowledgeAnswerResolutionReplica>
    find(
            String keyFingerprint,
            String runtimeId) {

        var runtimeReplicas =
                replicas.get(keyFingerprint);

        if (runtimeReplicas == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                runtimeReplicas.get(runtimeId)
        );
    }

    @Override
    public List<
            KnowledgeAnswerResolutionReplica>
    findAll(
            String keyFingerprint) {

        var runtimeReplicas =
                replicas.get(keyFingerprint);

        if (runtimeReplicas == null) {
            return List.of();
        }

        return List.copyOf(
                runtimeReplicas.values()
        );
    }

    @Override
    public void remove(
            String keyFingerprint,
            String runtimeId) {

        var runtimeReplicas =
                replicas.get(keyFingerprint);

        if (runtimeReplicas != null) {
            runtimeReplicas.remove(runtimeId);
        }
    }
}
