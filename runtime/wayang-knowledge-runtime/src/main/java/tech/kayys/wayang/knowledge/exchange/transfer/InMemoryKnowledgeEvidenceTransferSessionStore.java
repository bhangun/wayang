package tech.kayys.wayang.knowledge.exchange.transfer;

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


import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryKnowledgeEvidenceTransferSessionStore
        implements KnowledgeEvidenceTransferSessionStore {

    private final ConcurrentMap<
            String,
            KnowledgeEvidenceTransferSession
            > sessions =
            new ConcurrentHashMap<>();

    @Override
    public void create(
            KnowledgeEvidenceTransferSession session
    ) {

        var existing =
                sessions.putIfAbsent(
                        session.transferId(),
                        session
                );

        if (existing != null) {

            throw new IllegalStateException(
                    "Transfer already exists: "
                            + session.transferId()
            );
        }
    }

    @Override
    public void update(
            KnowledgeEvidenceTransferSession session
    ) {

        sessions.compute(
                session.transferId(),
                (id, previous) -> {

                    if (previous == null) {

                        throw new IllegalStateException(
                                "Unknown transfer: " + id
                        );
                    }

                    return session;
                }
        );
    }

    @Override
    public Optional<
            KnowledgeEvidenceTransferSession
            > find(
                    String transferId
            ) {

        return Optional.ofNullable(
                sessions.get(transferId)
        );
    }
}
