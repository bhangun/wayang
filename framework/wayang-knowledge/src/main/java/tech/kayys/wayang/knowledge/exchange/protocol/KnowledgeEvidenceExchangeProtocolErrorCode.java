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


public enum KnowledgeEvidenceExchangeProtocolErrorCode {

    INVALID_STATE,

    INVALID_MESSAGE,

    INVALID_PROTOCOL_VERSION,

    PROTOCOL_DOWNGRADE,

    UNKNOWN_RUNTIME,

    UNTRUSTED_RUNTIME,

    UNKNOWN_KEY,

    REVOKED_KEY,

    EXPIRED_KEY,

    INVALID_SIGNATURE,

    INVALID_SESSION,

    INVALID_NONCE,

    CAPABILITY_MISMATCH,

    REQUIRED_CAPABILITY_MISSING,

    NEGOTIATION_FAILED,

    SESSION_EXPIRED,

    SESSION_REVOKED,

    TENANT_MISMATCH,

    SCOPE_MISMATCH,

    REQUEST_REPLAYED,

    RESPONSE_MISMATCH,

    INTERNAL_ERROR
}
