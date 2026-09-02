package tech.kayys.wayang.knowledge.exchange.selection;

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
import java.util.concurrent.CopyOnWriteArrayList;

public final class
InMemoryKnowledgeAnswerResolutionDependencyGraph
        implements KnowledgeAnswerResolutionDependencyGraph {

    private final ConcurrentHashMap<
            String,
            CopyOnWriteArrayList<
                    KnowledgeAnswerResolutionDependency>>
            byResolution =
            new ConcurrentHashMap<>();

    private final ConcurrentHashMap<
            String,
            CopyOnWriteArrayList<String>>
            byTarget =
            new ConcurrentHashMap<>();

    @Override
    public void add(
            KnowledgeAnswerResolutionDependency dependency) {

        byResolution
                .computeIfAbsent(
                        dependency.resolutionKeyFingerprint(),
                        ignored -> new CopyOnWriteArrayList<>()
                )
                .add(dependency);

        byTarget
                .computeIfAbsent(
                        targetKey(
                                dependency.type(),
                                dependency.targetId()
                        ),
                        ignored -> new CopyOnWriteArrayList<>()
                )
                .add(
                        dependency.resolutionKeyFingerprint()
                );
    }

    @Override
    public void remove(String dependencyId) {

        byResolution.values()
                .forEach(list ->
                        list.removeIf(
                                dependency ->
                                        dependency.dependencyId()
                                                .equals(dependencyId)
                        ));
    }

    @Override
    public List<
            KnowledgeAnswerResolutionDependency>
    dependenciesOf(
            String resolutionKeyFingerprint) {

        return List.copyOf(
                byResolution.getOrDefault(
                        resolutionKeyFingerprint,
                        new CopyOnWriteArrayList<>()
                )
        );
    }

    @Override
    public List<String>
    resolutionsDependingOn(
            KnowledgeAnswerResolutionDependencyType type,
            String targetId) {

        return List.copyOf(
                byTarget.getOrDefault(
                        targetKey(type, targetId),
                        new CopyOnWriteArrayList<>()
                )
        );
    }

    @Override
    public List<String>
    resolutionsDependingOnFingerprint(
            String fingerprint) {

        List<String> result =
                new ArrayList<>();

        byResolution.values()
                .forEach(dependencies -> {

                    if (dependencies.stream()
                            .anyMatch(d ->
                                    fingerprint.equals(
                                            d.targetFingerprint()
                                    ))) {

                        result.add(
                                dependencies.get(0)
                                        .resolutionKeyFingerprint()
                        );
                    }
                });

        return List.copyOf(result);
    }

    private String targetKey(
            KnowledgeAnswerResolutionDependencyType type,
            String targetId) {

        return type.name()
                + ":"
                + targetId;
    }
}
