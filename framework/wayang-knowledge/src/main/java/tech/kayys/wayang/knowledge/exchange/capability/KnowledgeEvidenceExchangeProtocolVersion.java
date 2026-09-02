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


import java.util.Objects;

public record KnowledgeEvidenceExchangeProtocolVersion(

        int major,

        int minor

) implements Comparable<KnowledgeEvidenceExchangeProtocolVersion> {

    public KnowledgeEvidenceExchangeProtocolVersion {
        if (major < 0) {
            throw new IllegalArgumentException(
                    "major must be >= 0"
            );
        }

        if (minor < 0) {
            throw new IllegalArgumentException(
                    "minor must be >= 0"
            );
        }
    }

    @Override
    public int compareTo(
            KnowledgeEvidenceExchangeProtocolVersion other
    ) {

        Objects.requireNonNull(other);

        int majorCompare =
                Integer.compare(major, other.major);

        if (majorCompare != 0) {
            return majorCompare;
        }

        return Integer.compare(minor, other.minor);
    }

    @Override
    public String toString() {
        return major + "." + minor;
    }
}
