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

public final class InMemoryKnowledgeEvidenceTransferCheckpointStore
        implements KnowledgeEvidenceTransferCheckpointStore {

    private final ConcurrentMap<
            String,
            KnowledgeEvidenceTransferCheckpoint
            > checkpoints =
            new ConcurrentHashMap<>();

    @Override
    public void save(
            KnowledgeEvidenceTransferCheckpoint checkpoint
    ) {

        checkpoints.put(
                checkpoint.transferId(),
                checkpoint
        );
    }

    @Override
    public Optional<
            KnowledgeEvidenceTransferCheckpoint
            > find(
                    String transferId
            ) {

        return Optional.ofNullable(
                checkpoints.get(transferId)
        );
    }

    @Override
    public void delete(
            String transferId
    ) {

        checkpoints.remove(transferId);
    }
}
