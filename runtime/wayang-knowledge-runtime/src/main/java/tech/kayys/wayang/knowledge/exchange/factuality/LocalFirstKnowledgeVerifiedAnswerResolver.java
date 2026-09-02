package tech.kayys.wayang.knowledge.exchange.factuality;

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


import java.util.concurrent.CompletableFuture;

public final class LocalFirstKnowledgeVerifiedAnswerResolver
        implements KnowledgeVerifiedAnswerRemoteResolver {

    private final KnowledgeVerifiedAnswerArtifactStore localStore;

    private final KnowledgeVerifiedAnswerRemoteResolver remote;

    public LocalFirstKnowledgeVerifiedAnswerResolver(
            KnowledgeVerifiedAnswerArtifactStore localStore,
            KnowledgeVerifiedAnswerRemoteResolver remote) {

        this.localStore = localStore;
        this.remote = remote;
    }

    @Override
    public CompletableFuture<
            KnowledgeVerifiedAnswerExchangeResponse> resolve(
            KnowledgeVerifiedAnswerExchangeRequest request) {

        var local =
                localStore.get(request.artifactId());

        if (local.isPresent()) {

            return CompletableFuture.completedFuture(
                    new KnowledgeVerifiedAnswerExchangeResponse(
                            true,
                            request.operation(),
                            request.artifactId(),
                            local.get(),
                            null,
                            null,
                            java.util.Map.of(
                                    "source",
                                    "local"
                            )
                    )
            );
        }

        if (!request.allowRemoteFetch()) {

            return CompletableFuture.completedFuture(
                    KnowledgeVerifiedAnswerExchangeResponse.failure(
                            request.operation(),
                            request.artifactId(),
                            "REMOTE_FETCH_DISABLED",
                            "Artifact is not locally available"
                    )
            );
        }

        return remote.resolve(request);
    }
}
