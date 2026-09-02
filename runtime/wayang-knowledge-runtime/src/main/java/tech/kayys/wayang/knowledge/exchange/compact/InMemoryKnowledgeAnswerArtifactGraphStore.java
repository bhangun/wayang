package tech.kayys.wayang.knowledge.exchange.compact;

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


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryKnowledgeAnswerArtifactGraphStore
        implements KnowledgeAnswerArtifactGraphStore {

    private final ConcurrentHashMap<
            String,
            KnowledgeAnswerArtifactRelation> relations =
            new ConcurrentHashMap<>();

    @Override
    public void addRelation(
            KnowledgeAnswerArtifactRelation relation) {

        relations.putIfAbsent(
                relation.relationId(),
                relation
        );
    }

    @Override
    public void removeRelation(
            String relationId) {

        relations.remove(relationId);
    }

    @Override
    public List<KnowledgeAnswerArtifactRelation>
            relationsFrom(String artifactId) {

        return relations.values()
                .stream()
                .filter(r ->
                        r.sourceArtifactId()
                                .equals(artifactId))
                .toList();
    }

    @Override
    public List<KnowledgeAnswerArtifactRelation>
            relationsTo(String artifactId) {

        return relations.values()
                .stream()
                .filter(r ->
                        r.targetArtifactId()
                                .equals(artifactId))
                .toList();
    }

    @Override
    public List<KnowledgeAnswerArtifactRelation>
            relationsBetween(
                    String leftArtifactId,
                    String rightArtifactId) {

        List<KnowledgeAnswerArtifactRelation>
                result = new ArrayList<>();

        for (var relation : relations.values()) {

            boolean direct =
                    relation.sourceArtifactId()
                            .equals(leftArtifactId)
                    && relation.targetArtifactId()
                            .equals(rightArtifactId);

            boolean reverse =
                    relation.sourceArtifactId()
                            .equals(rightArtifactId)
                    && relation.targetArtifactId()
                            .equals(leftArtifactId);

            if (direct || reverse) {
                result.add(relation);
            }
        }

        return List.copyOf(result);
    }
}
