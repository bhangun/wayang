package tech.kayys.wayang.knowledge.exchange.federation;

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


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class DefaultKnowledgeEvidenceFederationQueryService
        implements KnowledgeEvidenceFederationQueryService {

    private final KnowledgeEvidenceFederationRouter router;

    private final java.util.Map<
            String,
            KnowledgeEvidenceFederationQueryEndpoint
            > endpoints;

    public DefaultKnowledgeEvidenceFederationQueryService(
            KnowledgeEvidenceFederationRouter router,
            java.util.Map<
                    String,
                    KnowledgeEvidenceFederationQueryEndpoint
                    > endpoints
    ) {

        this.router = router;

        this.endpoints =
                java.util.Map.copyOf(endpoints);
    }

    @Override
    public CompletableFuture<
            KnowledgeEvidenceFederationAggregateResult
            > query(
                    KnowledgeEvidenceFederatedQuery query
            ) {

        var candidates =
                router.route(query);

        var queried =
                new ArrayList<String>();

        var failed =
                new ArrayList<String>();

        var futures =
                new ArrayList<
                        CompletableFuture<
                                KnowledgeEvidenceFederationQueryResult
                                >
                        >();

        for (var candidate : candidates) {

            if (queried.size() >= query.limit()) {
                break;
            }

            var runtimeId =
                    candidate.location().runtimeId();

            var endpoint =
                    endpoints.get(runtimeId);

            if (endpoint == null) {

                failed.add(runtimeId);
                continue;
            }

            queried.add(runtimeId);

            futures.add(
                    endpoint.query(query)
                            .exceptionally(error -> {

                                failed.add(runtimeId);

                                return null;
                            })
            );
        }

        if (futures.isEmpty()) {

            return CompletableFuture.completedFuture(
                    new KnowledgeEvidenceFederationAggregateResult(
                            query.queryId(),
                            List.of(),
                            queried,
                            failed,
                            false,
                            java.util.Map.of(
                                    "reason",
                                    "No reachable federation endpoints"
                            )
                    )
            );
        }

        return CompletableFuture
                .allOf(
                        futures.toArray(
                                new CompletableFuture[0]
                        )
                )
                .thenApply(ignore -> {

                    var evidence =
                            new ArrayList<
                                    KnowledgeEvidenceReference
                                    >();

                    for (var future : futures) {

                        var result =
                                future.join();

                        if (result == null) {
                            continue;
                        }

                        if (result.authorized()) {

                            evidence.addAll(
                                    result.evidence()
                            );
                        }
                    }

                    return new KnowledgeEvidenceFederationAggregateResult(
                            query.queryId(),
                            evidence,
                            queried,
                            failed,
                            failed.isEmpty(),
                            java.util.Map.of(
                                    "candidateCount",
                                    Integer.toString(
                                            candidates.size()
                                    )
                            )
                    );
                });
    }
}
