package tech.kayys.wayang.knowledge.exchange.routing;

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


import java.util.List;
import java.util.Map;

public record KnowledgeEvidenceRuntimeKnowledgeDescriptor(

        String runtimeId,

        List<String> domains,

        List<String> knowledgeTypes,

        List<String> tags,

        List<String> languages,

        double estimatedCoverage,

        Map<String, String> metadata

) {

    public KnowledgeEvidenceRuntimeKnowledgeDescriptor {

        domains = domains == null
                ? List.of()
                : List.copyOf(domains);

        knowledgeTypes = knowledgeTypes == null
                ? List.of()
                : List.copyOf(knowledgeTypes);

        tags = tags == null
                ? List.of()
                : List.copyOf(tags);

        languages = languages == null
                ? List.of()
                : List.copyOf(languages);

        metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }
}
