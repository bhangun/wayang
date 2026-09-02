package tech.kayys.wayang.knowledge.exchange.protocol;

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


import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

public final class DefaultKnowledgeEvidenceExchangeProtocolStateMachine
        implements KnowledgeEvidenceExchangeProtocolStateMachine {

    private KnowledgeEvidenceExchangeProtocolState state =
            KnowledgeEvidenceExchangeProtocolState.NEW;

    private final Map<
            KnowledgeEvidenceExchangeProtocolState,
            EnumSet<KnowledgeEvidenceExchangeProtocolMessageType>
            > transitions =
            new EnumMap<>(
                    KnowledgeEvidenceExchangeProtocolState.class
            );

    public DefaultKnowledgeEvidenceExchangeProtocolStateMachine() {

        transitions.put(
                KnowledgeEvidenceExchangeProtocolState.NEW,
                EnumSet.of(
                        KnowledgeEvidenceExchangeProtocolMessageType.HELLO
                )
        );

        transitions.put(
                KnowledgeEvidenceExchangeProtocolState.HELLO_SENT,
                EnumSet.of(
                        KnowledgeEvidenceExchangeProtocolMessageType.HELLO,
                        KnowledgeEvidenceExchangeProtocolMessageType.IDENTITY,
                        KnowledgeEvidenceExchangeProtocolMessageType.ERROR
                )
        );

        transitions.put(
                KnowledgeEvidenceExchangeProtocolState.HELLO_RECEIVED,
                EnumSet.of(
                        KnowledgeEvidenceExchangeProtocolMessageType.IDENTITY,
                        KnowledgeEvidenceExchangeProtocolMessageType.ERROR
                )
        );

        transitions.put(
                KnowledgeEvidenceExchangeProtocolState.IDENTITY_SENT,
                EnumSet.of(
                        KnowledgeEvidenceExchangeProtocolMessageType.AUTHENTICATE,
                        KnowledgeEvidenceExchangeProtocolMessageType.ERROR
                )
        );

        transitions.put(
                KnowledgeEvidenceExchangeProtocolState.IDENTITY_RECEIVED,
                EnumSet.of(
                        KnowledgeEvidenceExchangeProtocolMessageType.AUTHENTICATE,
                        KnowledgeEvidenceExchangeProtocolMessageType.ERROR
                )
        );

        transitions.put(
                KnowledgeEvidenceExchangeProtocolState
                        .AUTHENTICATION_SENT,
                EnumSet.of(
                        KnowledgeEvidenceExchangeProtocolMessageType
                                .AUTHENTICATE,
                        KnowledgeEvidenceExchangeProtocolMessageType
                                .CAPABILITIES,
                        KnowledgeEvidenceExchangeProtocolMessageType.ERROR
                )
        );

        transitions.put(
                KnowledgeEvidenceExchangeProtocolState.AUTHENTICATED,
                EnumSet.of(
                        KnowledgeEvidenceExchangeProtocolMessageType
                                .CAPABILITIES,
                        KnowledgeEvidenceExchangeProtocolMessageType.ERROR
                )
        );

        transitions.put(
                KnowledgeEvidenceExchangeProtocolState.CAPABILITIES_SENT,
                EnumSet.of(
                        KnowledgeEvidenceExchangeProtocolMessageType
                                .CAPABILITIES,
                        KnowledgeEvidenceExchangeProtocolMessageType
                                .NEGOTIATE,
                        KnowledgeEvidenceExchangeProtocolMessageType.ERROR
                )
        );

        transitions.put(
                KnowledgeEvidenceExchangeProtocolState
                        .CAPABILITIES_RECEIVED,
                EnumSet.of(
                        KnowledgeEvidenceExchangeProtocolMessageType
                                .NEGOTIATE,
                        KnowledgeEvidenceExchangeProtocolMessageType.ERROR
                )
        );

        transitions.put(
                KnowledgeEvidenceExchangeProtocolState.NEGOTIATED,
                EnumSet.of(
                        KnowledgeEvidenceExchangeProtocolMessageType
                                .ESTABLISH,
                        KnowledgeEvidenceExchangeProtocolMessageType.ERROR
                )
        );

        transitions.put(
                KnowledgeEvidenceExchangeProtocolState.ESTABLISHED,
                EnumSet.of(
                        KnowledgeEvidenceExchangeProtocolMessageType
                                .REQUEST,
                        KnowledgeEvidenceExchangeProtocolMessageType
                                .CLOSE,
                        KnowledgeEvidenceExchangeProtocolMessageType
                                .ERROR
                )
        );

        transitions.put(
                KnowledgeEvidenceExchangeProtocolState.EXCHANGING,
                EnumSet.of(
                        KnowledgeEvidenceExchangeProtocolMessageType
                                .REQUEST,
                        KnowledgeEvidenceExchangeProtocolMessageType
                                .RESPONSE,
                        KnowledgeEvidenceExchangeProtocolMessageType
                                .CLOSE,
                        KnowledgeEvidenceExchangeProtocolMessageType
                                .ERROR
                )
        );

        transitions.put(
                KnowledgeEvidenceExchangeProtocolState.CLOSE_SENT,
                EnumSet.of(
                        KnowledgeEvidenceExchangeProtocolMessageType
                                .CLOSE
                )
        );

        transitions.put(
                KnowledgeEvidenceExchangeProtocolState.CLOSED,
                EnumSet.noneOf(
                        KnowledgeEvidenceExchangeProtocolMessageType.class
                )
        );

        transitions.put(
                KnowledgeEvidenceExchangeProtocolState.FAILED,
                EnumSet.of(
                        KnowledgeEvidenceExchangeProtocolMessageType
                                .CLOSE
                )
        );
    }

    @Override
    public synchronized KnowledgeEvidenceExchangeProtocolState state() {
        return state;
    }

    @Override
    public synchronized boolean canAccept(
            KnowledgeEvidenceExchangeProtocolMessageType messageType
    ) {

        return transitions
                .getOrDefault(
                        state,
                        EnumSet.noneOf(
                                KnowledgeEvidenceExchangeProtocolMessageType
                                        .class
                        )
                )
                .contains(messageType);
    }

    @Override
    public synchronized void transition(
            KnowledgeEvidenceExchangeProtocolMessageType messageType
    ) {

        if (!canAccept(messageType)) {

            throw new KnowledgeEvidenceExchangeProtocolStateException(
                    "Invalid protocol transition: state="
                            + state
                            + ", message="
                            + messageType
            );
        }

        state = nextState(
                state,
                messageType
        );
    }

    private KnowledgeEvidenceExchangeProtocolState nextState(
            KnowledgeEvidenceExchangeProtocolState current,
            KnowledgeEvidenceExchangeProtocolMessageType message
    ) {

        return switch (message) {

            case HELLO ->
                    current == KnowledgeEvidenceExchangeProtocolState.NEW
                            ? KnowledgeEvidenceExchangeProtocolState
                                    .HELLO_SENT
                            : KnowledgeEvidenceExchangeProtocolState
                                    .HELLO_RECEIVED;

            case IDENTITY ->
                    current == KnowledgeEvidenceExchangeProtocolState
                            .HELLO_RECEIVED
                            ? KnowledgeEvidenceExchangeProtocolState
                                    .IDENTITY_RECEIVED
                            : KnowledgeEvidenceExchangeProtocolState
                                    .IDENTITY_SENT;

            case AUTHENTICATE ->
                    current == KnowledgeEvidenceExchangeProtocolState
                            .IDENTITY_RECEIVED
                            ? KnowledgeEvidenceExchangeProtocolState
                                    .AUTHENTICATED
                            : KnowledgeEvidenceExchangeProtocolState
                                    .AUTHENTICATION_SENT;

            case CAPABILITIES ->
                    current == KnowledgeEvidenceExchangeProtocolState
                            .AUTHENTICATED
                            ? KnowledgeEvidenceExchangeProtocolState
                                    .CAPABILITIES_RECEIVED
                            : KnowledgeEvidenceExchangeProtocolState
                                    .CAPABILITIES_SENT;

            case NEGOTIATE ->
                    KnowledgeEvidenceExchangeProtocolState.NEGOTIATED;

            case ESTABLISH ->
                    KnowledgeEvidenceExchangeProtocolState.ESTABLISHED;

            case REQUEST, RESPONSE ->
                    KnowledgeEvidenceExchangeProtocolState.EXCHANGING;

            case CLOSE ->
                    KnowledgeEvidenceExchangeProtocolState.CLOSED;

            case ERROR ->
                    KnowledgeEvidenceExchangeProtocolState.FAILED;
        };
    }

    @Override
    public synchronized void fail(
            KnowledgeEvidenceExchangeProtocolErrorCode errorCode,
            String reason
    ) {

        state =
                KnowledgeEvidenceExchangeProtocolState.FAILED;
    }
}
