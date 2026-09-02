package tech.kayys.wayang.knowledge.exchange.quorum;

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


import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class
InMemoryKnowledgeAnswerResolutionMemoryStore
        implements KnowledgeAnswerResolutionMemoryStore {

    private final ConcurrentMap<
            String,
            KnowledgeAnswerResolutionMemoryEntry
            > entries =
            new ConcurrentHashMap<>();

    @Override
    public void save(
            KnowledgeAnswerResolutionMemoryEntry entry) {

        entries.putIfAbsent(
                entry.keyFingerprint(),
                entry
        );
    }

    @Override
    public Optional<
            KnowledgeAnswerResolutionMemoryEntry> get(
            String keyFingerprint) {

        return Optional.ofNullable(
                entries.get(keyFingerprint)
        );
    }

    @Override
    public void invalidate(
            String keyFingerprint) {

        entries.computeIfPresent(
                keyFingerprint,
                (key, entry) ->
                        new KnowledgeAnswerResolutionMemoryEntry(
                                entry.memoryId(),
                                entry.keyFingerprint(),
                                entry.key(),
                                entry.resolution(),
                                entry.snapshotFingerprint(),
                                entry.evidenceFingerprint(),
                                entry.graphFingerprint(),
                                entry.createdAt(),
                                entry.expiresAt(),
                                true,
                                entry.metadata()
                        )
        );
    }

    @Override
    public void delete(
            String keyFingerprint) {

        entries.remove(keyFingerprint);
    }
}
