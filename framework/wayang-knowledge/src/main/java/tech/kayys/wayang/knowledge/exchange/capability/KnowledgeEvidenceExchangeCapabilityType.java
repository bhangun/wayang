package tech.kayys.wayang.knowledge.exchange.capability;

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


public enum KnowledgeEvidenceExchangeCapabilityType {

    EVIDENCE_EXCHANGE,

    ARTIFACT_RESOLUTION,

    MANIFEST_RETRIEVAL,

    RESOURCE_RETRIEVAL,

    MERKLE_PROOF,

    PARTIAL_VERIFICATION,

    INTEGRITY_VERIFICATION,

    SECURE_SEAL_VERIFICATION,

    SNAPSHOT_VERIFICATION,

    SNAPSHOT_REPLAY,

    STREAMING,

    COMPRESSION,

    MCP,

    REMOTE_KNOWLEDGE,

    GOVERNED_RETRIEVAL
}
