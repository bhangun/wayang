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
import java.util.List;
import java.util.UUID;

public final class
KnowledgeAnswerResolutionCoordinator {

    private final KnowledgeAnswerResolutionEngine engine;

    private final KnowledgeAnswerResolutionMemoryService memory;

    private final KnowledgeAnswerResolutionKeyFingerprinter
            fingerprinter;

    public KnowledgeAnswerResolutionCoordinator(
            KnowledgeAnswerResolutionEngine engine,
            KnowledgeAnswerResolutionMemoryService memory) {

        this.engine = engine;
        this.memory = memory;
        this.fingerprinter =
                new Sha256KnowledgeAnswerResolutionKeyFingerprinter();
    }

    public KnowledgeAnswerResolutionResult resolve(
            KnowledgeAnswerResolutionKey key,
            List<KnowledgeVerifiedAnswerArtifact> artifacts,
            List<KnowledgeAnswerArtifactRelation> relations,
            KnowledgeAnswerResolutionContext context) {

        Instant now = Instant.now();

        var lookup =
                memory.lookup(key, now);

        if (lookup.status()
                == KnowledgeAnswerResolutionCacheStatus.HIT) {

            return lookup.entry()
                    .resolution();
        }

        var result =
                engine.resolve(
                        artifacts,
                        relations,
                        context
                );

        String fingerprint =
                fingerprinter.fingerprint(key);

        var entry =
                new KnowledgeAnswerResolutionMemoryEntry(
                        UUID.randomUUID().toString(),
                        fingerprint,
                        key,
                        result,
                        key.knowledgeSnapshotId(),
                        key.policySnapshotFingerprint(),
                        key.runtimeConfigurationFingerprint(),
                        now,
                        null,
                        false,
                        java.util.Map.of(
                                "cache",
                                "p056"
                        )
                );

        memory.remember(entry);

        return result;
    }
}
