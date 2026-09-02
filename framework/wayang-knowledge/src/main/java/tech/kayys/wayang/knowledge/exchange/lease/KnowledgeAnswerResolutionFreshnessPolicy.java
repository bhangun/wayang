package tech.kayys.wayang.knowledge.exchange.lease;

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
import tech.kayys.wayang.knowledge.exchange.coverage.*;
import tech.kayys.wayang.knowledge.exchange.gap.*;
import tech.kayys.wayang.knowledge.exchange.attribution.*;
import tech.kayys.wayang.knowledge.exchange.contradiction.*;
import tech.kayys.wayang.knowledge.exchange.factuality.*;
import tech.kayys.wayang.knowledge.exchange.uncertainty.*;
import tech.kayys.wayang.knowledge.exchange.compact.*;
import tech.kayys.wayang.knowledge.exchange.resolution.*;
import tech.kayys.wayang.knowledge.exchange.quorum.*;
import tech.kayys.wayang.knowledge.exchange.selection.*;
import tech.kayys.wayang.knowledge.exchange.coordination.*;
import tech.kayys.wayang.knowledge.exchange.attestation.*;
import tech.kayys.wayang.knowledge.exchange.proof.*;
import tech.kayys.wayang.knowledge.exchange.validity.*;
import tech.kayys.wayang.knowledge.exchange.lease.*;
import tech.kayys.wayang.knowledge.exchange.recovery.*;


import java.time.Duration;
import java.util.Objects;

public record KnowledgeAnswerResolutionFreshnessPolicy(
        Duration leaseDuration,
        Duration gracePeriod,
        Duration heartbeatTimeout,
        Duration maximumClockSkew,
        int minimumLiveParticipants,
        boolean requireParticipantLiveness,
        boolean requireLease,
        boolean allowGracePeriod,
        boolean allowOfflineGrace
) {

    public KnowledgeAnswerResolutionFreshnessPolicy {
        Objects.requireNonNull(leaseDuration, "leaseDuration");
        Objects.requireNonNull(gracePeriod, "gracePeriod");
        Objects.requireNonNull(heartbeatTimeout, "heartbeatTimeout");
        Objects.requireNonNull(maximumClockSkew, "maximumClockSkew");

        if (leaseDuration.isZero() || leaseDuration.isNegative()) {
            throw new IllegalArgumentException(
                    "leaseDuration must be positive");
        }

        if (gracePeriod.isNegative()) {
            throw new IllegalArgumentException(
                    "gracePeriod must not be negative");
        }

        if (heartbeatTimeout.isZero() || heartbeatTimeout.isNegative()) {
            throw new IllegalArgumentException(
                    "heartbeatTimeout must be positive");
        }

        if (minimumLiveParticipants < 0) {
            throw new IllegalArgumentException(
                    "minimumLiveParticipants must be >= 0");
        }
    }

    public static KnowledgeAnswerResolutionFreshnessPolicy defaults() {
        return new KnowledgeAnswerResolutionFreshnessPolicy(
                Duration.ofMinutes(15),
                Duration.ofMinutes(5),
                Duration.ofMinutes(2),
                Duration.ofSeconds(30),
                1,
                true,
                true,
                true,
                false
        );
    }
}
