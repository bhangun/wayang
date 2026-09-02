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


import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class DefaultKnowledgeEvidenceTransferManager
        implements KnowledgeEvidenceTransferManager {

    private final KnowledgeEvidenceTransferSessionStore
            sessionStore;

    private final KnowledgeEvidenceTransferCheckpointStore
            checkpointStore;

    private final KnowledgeEvidenceTransferStateMachine
            stateMachine;

    private final KnowledgeEvidenceTransferVerifier
            verifier;

    private final KnowledgeEvidenceTransferQuota
            quota;

    public DefaultKnowledgeEvidenceTransferManager(
            KnowledgeEvidenceTransferSessionStore sessionStore,
            KnowledgeEvidenceTransferCheckpointStore checkpointStore,
            KnowledgeEvidenceTransferStateMachine stateMachine,
            KnowledgeEvidenceTransferVerifier verifier,
            KnowledgeEvidenceTransferQuota quota
    ) {

        this.sessionStore = sessionStore;
        this.checkpointStore = checkpointStore;
        this.stateMachine = stateMachine;
        this.verifier = verifier;
        this.quota = quota;
    }

    @Override
    public CompletableFuture<
            KnowledgeEvidenceTransferSession
            > download(
                    KnowledgeEvidenceTransferRequest request,
                    KnowledgeEvidenceTransferSource source,
                    KnowledgeEvidenceTransferSink sink
            ) {

        return CompletableFuture.supplyAsync(() -> {

            Instant now = Instant.now();

            if (request.expiredAt(now)) {

                throw new KnowledgeEvidenceExchangeTransportException(
                        "Transfer request expired"
                );
            }

            if (request.length() > quota.maxBytesPerTransfer()) {

                throw new KnowledgeEvidenceExchangeTransportException(
                        "Transfer exceeds quota"
                );
            }

            long total =
                    source.length();

            long start =
                    request.offset();

            if (start > total) {

                throw new KnowledgeEvidenceExchangeTransportException(
                        "Transfer offset exceeds resource length"
                );
            }

            long requestedLength =
                    request.length() < 0
                            ? total - start
                            : Math.min(
                                    request.length(),
                                    total - start
                            );

            var session =
                    new KnowledgeEvidenceTransferSession(
                            request.transferId() == null
                                    ? UUID.randomUUID().toString()
                                    : request.transferId(),
                            KnowledgeEvidenceTransferOperation.DOWNLOAD,
                            request.sessionId(),
                            request.streamId(),
                            source.artifactId(),
                            source.resourceId(),
                            start,
                            start,
                            requestedLength,
                            0,
                            KnowledgeEvidenceTransferState.CREATED,
                            source.fingerprint(),
                            source.fingerprint(),
                            source.merkleRoot(),
                            now,
                            now,
                            java.util.Map.of()
                    );

            sessionStore.create(session);

            return execute(
                    session,
                    request,
                    source,
                    sink
            );
        });
    }

    private KnowledgeEvidenceTransferSession execute(
            KnowledgeEvidenceTransferSession initial,
            KnowledgeEvidenceTransferRequest request,
            KnowledgeEvidenceTransferSource source,
            KnowledgeEvidenceTransferSink sink
    ) {

        var session = initial;

        try {

            stateMachine.transition(
                    session,
                    KnowledgeEvidenceTransferState.AUTHORIZING
            );

            session =
                    copyState(
                            session,
                            KnowledgeEvidenceTransferState.AUTHORIZING
                    );

            sessionStore.update(session);

            /*
             * Authorization is intentionally delegated to
             * P032/P031 security services.
             */

            stateMachine.transition(
                    session,
                    KnowledgeEvidenceTransferState.AUTHORIZED
            );

            session =
                    copyState(
                            session,
                            KnowledgeEvidenceTransferState.AUTHORIZED
                    );

            sessionStore.update(session);

            stateMachine.transition(
                    session,
                    KnowledgeEvidenceTransferState.NEGOTIATING
            );

            session =
                    copyState(
                            session,
                            KnowledgeEvidenceTransferState.NEGOTIATING
                    );

            sessionStore.update(session);

            stateMachine.transition(
                    session,
                    KnowledgeEvidenceTransferState.TRANSFERRING
            );

            session =
                    copyState(
                            session,
                            KnowledgeEvidenceTransferState.TRANSFERRING
                    );

            sessionStore.update(session);

            long offset =
                    session.currentOffset();

            long remaining =
                    session.totalLength();

            try (
                    var input =
                            source.open(
                                    offset,
                                    remaining
                            );

                    var output =
                            sink.open(offset)
            ) {

                byte[] buffer =
                        new byte[
                                Math.min(
                                        quota.maxChunkBytes(),
                                        1024 * 1024
                                )
                        ];

                int read;

                long sequence =
                        session.nextSequence();

                while (
                        (read = input.read(buffer)) != -1
                ) {

                    output.write(
                            buffer,
                            0,
                            read
                    );

                    offset += read;
                    remaining -= read;

                    var checkpoint =
                            new KnowledgeEvidenceTransferCheckpoint(
                                    session.transferId(),
                                    session.sessionId(),
                                    session.streamId(),
                                    session.artifactId(),
                                    session.resourceId(),
                                    offset,
                                    sequence + 1,
                                    source.fingerprint(),
                                    source.fingerprint(),
                                    source.merkleRoot(),
                                    Instant.now(),
                                    java.util.Map.of()
                            );

                    checkpointStore.save(
                            checkpoint
                    );

                    session =
                            copyProgress(
                                    session,
                                    offset,
                                    sequence + 1
                            );

                    sessionStore.update(session);

                    sequence++;
                }
            }

            sink.complete();

            stateMachine.transition(
                    session,
                    KnowledgeEvidenceTransferState.VERIFYING
            );

            session =
                    copyState(
                            session,
                            KnowledgeEvidenceTransferState.VERIFYING
                    );

            sessionStore.update(session);

            verifier.verify(
                    session,
                    source
            );

            stateMachine.transition(
                    session,
                    KnowledgeEvidenceTransferState.COMPLETED
            );

            session =
                    copyState(
                            session,
                            KnowledgeEvidenceTransferState.COMPLETED
                    );

            sessionStore.update(session);

            checkpointStore.delete(
                    session.transferId()
            );

            return session;

        } catch (Exception e) {

            try {
                sink.abort();
            } catch (Exception ignored) {
            }

            var failed =
                    copyState(
                            session,
                            KnowledgeEvidenceTransferState.FAILED
                    );

            sessionStore.update(failed);

            throw new KnowledgeEvidenceExchangeTransportException(
                    "Artifact transfer failed",
                    e
            );
        }
    }

    @Override
    public CompletableFuture<
            KnowledgeEvidenceTransferSession
            > resume(
                    String transferId,
                    KnowledgeEvidenceTransferSource source,
                    KnowledgeEvidenceTransferSink sink
            ) {

        return CompletableFuture.supplyAsync(() -> {

            var checkpoint =
                    checkpointStore.find(
                            transferId
                    ).orElseThrow(() ->
                            new KnowledgeEvidenceExchangeTransportException(
                                    "No checkpoint for transfer "
                                            + transferId
                            )
                    );

            var session =
                    sessionStore.find(
                            transferId
                    ).orElseThrow(() ->
                            new KnowledgeEvidenceExchangeTransportException(
                                    "No transfer session "
                                            + transferId
                            )
                    );

            if (!source.artifactId().equals(
                    checkpoint.artifactId()
            )) {

                throw new KnowledgeEvidenceExchangeTransportException(
                        "Resume artifact mismatch"
                );
            }

            if (source.fingerprint() != null &&
                    checkpoint.artifactFingerprint() != null &&
                    !source.fingerprint().equals(
                            checkpoint.artifactFingerprint()
                    )) {

                throw new KnowledgeEvidenceExchangeTransportException(
                        "Resume fingerprint mismatch"
                );
            }

            var resumed =
                    new KnowledgeEvidenceTransferRequest(
                            transferId,
                            KnowledgeEvidenceTransferOperation
                                    .RESUME_DOWNLOAD,
                            session.sessionId(),
                            session.streamId(),
                            source.artifactId(),
                            source.resourceId(),
                            checkpoint.offset(),
                            session.totalLength()
                                    - checkpoint.offset(),
                            source.fingerprint(),
                            null,
                            true,
                            true,
                            Instant.now(),
                            null,
                            java.util.Map.of()
                    );

            return execute(
                    session,
                    resumed,
                    source,
                    sink
            );
        });
    }

    @Override
    public void cancel(
            String transferId
    ) {

        var session =
                sessionStore.find(
                        transferId
                ).orElseThrow();

        if (session.terminal()) {
            return;
        }

        var cancelled =
                copyState(
                        session,
                        KnowledgeEvidenceTransferState.CANCELLED
                );

        sessionStore.update(cancelled);
    }

    private KnowledgeEvidenceTransferSession copyState(
            KnowledgeEvidenceTransferSession s,
            KnowledgeEvidenceTransferState state
    ) {

        return new KnowledgeEvidenceTransferSession(
                s.transferId(),
                s.operation(),
                s.sessionId(),
                s.streamId(),
                s.artifactId(),
                s.resourceId(),
                s.startOffset(),
                s.currentOffset(),
                s.totalLength(),
                s.nextSequence(),
                state,
                s.artifactFingerprint(),
                s.resourceFingerprint(),
                s.merkleRoot(),
                s.createdAt(),
                Instant.now(),
                s.metadata()
        );
    }

    private KnowledgeEvidenceTransferSession copyProgress(
            KnowledgeEvidenceTransferSession s,
            long offset,
            long nextSequence
    ) {

        return new KnowledgeEvidenceTransferSession(
                s.transferId(),
                s.operation(),
                s.sessionId(),
                s.streamId(),
                s.artifactId(),
                s.resourceId(),
                s.startOffset(),
                offset,
                s.totalLength(),
                nextSequence,
                s.state(),
                s.artifactFingerprint(),
                s.resourceFingerprint(),
                s.merkleRoot(),
                s.createdAt(),
                Instant.now(),
                s.metadata()
        );
    }
}
