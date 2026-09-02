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


public final class DefaultKnowledgeEvidenceTransferStateMachine
        implements KnowledgeEvidenceTransferStateMachine {

    @Override
    public void transition(
            KnowledgeEvidenceTransferSession session,
            KnowledgeEvidenceTransferState target
    ) {

        var current = session.state();

        boolean valid =
                switch (current) {

                    case CREATED ->
                            target == KnowledgeEvidenceTransferState.AUTHORIZING
                                    || target == KnowledgeEvidenceTransferState.CANCELLED;

                    case AUTHORIZING ->
                            target == KnowledgeEvidenceTransferState.AUTHORIZED
                                    || target == KnowledgeEvidenceTransferState.FAILED;

                    case AUTHORIZED ->
                            target == KnowledgeEvidenceTransferState.NEGOTIATING
                                    || target == KnowledgeEvidenceTransferState.CANCELLED;

                    case NEGOTIATING ->
                            target == KnowledgeEvidenceTransferState.TRANSFERRING
                                    || target == KnowledgeEvidenceTransferState.FAILED;

                    case TRANSFERRING ->
                            target == KnowledgeEvidenceTransferState.TRANSFERRING
                                    || target == KnowledgeEvidenceTransferState.PAUSED
                                    || target == KnowledgeEvidenceTransferState.VERIFYING
                                    || target == KnowledgeEvidenceTransferState.CANCELLED
                                    || target == KnowledgeEvidenceTransferState.FAILED;

                    case PAUSED ->
                            target == KnowledgeEvidenceTransferState.TRANSFERRING
                                    || target == KnowledgeEvidenceTransferState.CANCELLED
                                    || target == KnowledgeEvidenceTransferState.EXPIRED;

                    case VERIFYING ->
                            target == KnowledgeEvidenceTransferState.COMPLETED
                                    || target == KnowledgeEvidenceTransferState.FAILED;

                    default -> false;
                };

        if (!valid) {

            throw new KnowledgeEvidenceExchangeTransportException(
                    "Invalid transfer transition: "
                            + current
                            + " -> "
                            + target
            );
        }
    }
}
