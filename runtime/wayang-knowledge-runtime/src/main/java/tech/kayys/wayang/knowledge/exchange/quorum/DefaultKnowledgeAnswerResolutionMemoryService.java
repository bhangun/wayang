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


import java.time.Instant;

public final class
DefaultKnowledgeAnswerResolutionMemoryService
        implements KnowledgeAnswerResolutionMemoryService {

    private final KnowledgeAnswerResolutionMemoryStore store;

    private final KnowledgeAnswerResolutionKeyFingerprinter
            fingerprinter;

    public DefaultKnowledgeAnswerResolutionMemoryService(
            KnowledgeAnswerResolutionMemoryStore store) {

        this(
                store,
                new Sha256KnowledgeAnswerResolutionKeyFingerprinter()
        );
    }

    public DefaultKnowledgeAnswerResolutionMemoryService(
            KnowledgeAnswerResolutionMemoryStore store,
            KnowledgeAnswerResolutionKeyFingerprinter
                    fingerprinter) {

        this.store = store;
        this.fingerprinter = fingerprinter;
    }

    @Override
    public KnowledgeAnswerResolutionCacheLookup lookup(
            KnowledgeAnswerResolutionKey key,
            Instant now) {

        String fingerprint =
                fingerprinter.fingerprint(key);

        var entry =
                store.get(fingerprint);

        if (entry.isEmpty()) {

            return new KnowledgeAnswerResolutionCacheLookup(
                    KnowledgeAnswerResolutionCacheStatus.MISS,
                    null,
                    "No cached resolution"
            );
        }

        var value = entry.get();

        if (value.invalidated()) {

            return new KnowledgeAnswerResolutionCacheLookup(
                    KnowledgeAnswerResolutionCacheStatus
                            .INVALIDATED,
                    value,
                    "Resolution explicitly invalidated"
            );
        }

        if (!value.activeAt(now)) {

            return new KnowledgeAnswerResolutionCacheLookup(
                    KnowledgeAnswerResolutionCacheStatus.STALE,
                    value,
                    "Resolution expired"
            );
        }

        return new KnowledgeAnswerResolutionCacheLookup(
                KnowledgeAnswerResolutionCacheStatus.HIT,
                value,
                "Resolution cache hit"
        );
    }

    @Override
    public void remember(
            KnowledgeAnswerResolutionMemoryEntry entry) {

        store.save(entry);
    }

    @Override
    public void invalidate(
            KnowledgeAnswerResolutionKey key) {

        String fingerprint =
                fingerprinter.fingerprint(key);

        store.invalidate(fingerprint);
    }
}
