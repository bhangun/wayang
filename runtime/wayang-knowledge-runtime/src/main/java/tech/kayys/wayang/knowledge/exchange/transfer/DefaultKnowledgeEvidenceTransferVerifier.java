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


public final class DefaultKnowledgeEvidenceTransferVerifier
        implements KnowledgeEvidenceTransferVerifier {

    @Override
    public void verify(
            KnowledgeEvidenceTransferSession session,
            KnowledgeEvidenceTransferSource source
    ) {

        if (session.totalLength() >= 0 &&
                session.currentOffset()
                        != session.totalLength()) {

            throw new KnowledgeEvidenceExchangeTransportException(
                    "Transfer incomplete: "
                            + session.currentOffset()
                            + "/"
                            + session.totalLength()
            );
        }

        if (session.artifactFingerprint() != null &&
                source.fingerprint() != null &&
                !session.artifactFingerprint().equals(
                        source.fingerprint()
                )) {

            throw new KnowledgeEvidenceExchangeTransportException(
                    "Artifact fingerprint mismatch"
            );
        }

        if (session.merkleRoot() != null &&
                source.merkleRoot() != null &&
                !session.merkleRoot().equals(
                        source.merkleRoot()
                )) {

            throw new KnowledgeEvidenceExchangeTransportException(
                    "Merkle root mismatch"
            );
        }
    }
}
