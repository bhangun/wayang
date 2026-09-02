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


import java.util.Comparator;
import java.util.Set;

public final class DefaultKnowledgeEvidenceExchangeProtocolVersionPolicy
        implements KnowledgeEvidenceExchangeProtocolVersionPolicy {

    @Override
    public KnowledgeEvidenceExchangeProtocolVersion negotiate(
            KnowledgeEvidenceExchangeProtocolVersion localPreferred,
            Set<KnowledgeEvidenceExchangeProtocolVersion> localSupported,
            KnowledgeEvidenceExchangeProtocolVersion remotePreferred,
            Set<KnowledgeEvidenceExchangeProtocolVersion> remoteSupported
    ) {

        return localSupported.stream()
                .filter(remoteSupported::contains)
                .filter(version ->
                        version.major() ==
                                localPreferred.major())
                .max(Comparator.naturalOrder())
                .orElseThrow(() ->
                        new KnowledgeEvidenceExchangeProtocolStateException(
                                "No compatible protocol version"
                        )
                );
    }

    @Override
    public void validateNoDowngrade(
            KnowledgeEvidenceExchangeProtocolVersion selected,
            Set<KnowledgeEvidenceExchangeProtocolVersion>
                    localSupported,
            Set<KnowledgeEvidenceExchangeProtocolVersion>
                    remoteSupported
    ) {

        var highestCommon =
                localSupported.stream()
                        .filter(remoteSupported::contains)
                        .max(Comparator.naturalOrder());

        if (highestCommon.isPresent() &&
                selected.compareTo(highestCommon.get()) < 0) {

            throw new KnowledgeEvidenceExchangeProtocolStateException(
                    "Protocol downgrade detected: selected="
                            + selected
                            + ", highestCommon="
                            + highestCommon.get()
            );
        }
    }
}
