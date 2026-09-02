package tech.kayys.wayang.knowledge.exchange.transport;

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


import java.time.Duration;

public record KnowledgeEvidenceExchangeDeadlinePolicy(

        Duration defaultDeadline,

        Duration maximumDeadline

) {

    public static KnowledgeEvidenceExchangeDeadlinePolicy
    defaults() {

        return new KnowledgeEvidenceExchangeDeadlinePolicy(
                Duration.ofSeconds(30),
                Duration.ofMinutes(10)
        );
    }

    public Duration normalize(Duration requested) {

        if (requested == null) {
            return defaultDeadline;
        }

        if (requested.isNegative() ||
                requested.isZero()) {

            throw new IllegalArgumentException(
                    "Deadline must be positive"
            );
        }

        return requested.compareTo(maximumDeadline) > 0
                ? maximumDeadline
                : requested;
    }
}
