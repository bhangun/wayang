package tech.kayys.wayang.knowledge.exchange.identity;

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


import java.nio.charset.StandardCharsets;

public final class DefaultKnowledgeEvidenceExchangeRuntimeIdentityCanonicalizer
        implements KnowledgeEvidenceExchangeRuntimeIdentityCanonicalizer {

    @Override
    public byte[] canonicalize(
            KnowledgeEvidenceExchangeRuntimeIdentity identity
    ) {

        String value = String.join(
                "\n",
                identity.runtimeId(),
                identity.identityVersion(),
                nullSafe(identity.displayName()),
                nullSafe(identity.runtimeType()),
                nullSafe(identity.organizationId()),
                nullSafe(identity.tenantId()),
                nullSafe(identity.primaryKeyId()),
                nullSafe(identity.primaryKeyVersion()),
                nullSafe(identity.trustAnchorId())
        );

        return value.getBytes(StandardCharsets.UTF_8);
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
