package tech.kayys.wayang.knowledge.replay;

import tech.kayys.wayang.knowledge.decision.KnowledgeDecisionTrace;
import tech.kayys.wayang.knowledge.decision.KnowledgeDecisionTraceService;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class DefaultKnowledgeReplayEngine implements KnowledgeReplayEngine {

    private final KnowledgeDecisionTraceService traceService;
    private final KnowledgeDecisionReexecutor reexecutor;
    private final KnowledgeReplayContextProvider contextProvider;

    public DefaultKnowledgeReplayEngine(
            KnowledgeDecisionTraceService traceService,
            KnowledgeDecisionReexecutor reexecutor,
            KnowledgeReplayContextProvider contextProvider) {

        this.traceService = Objects.requireNonNull(traceService, "traceService is required");
        this.reexecutor = Objects.requireNonNull(reexecutor, "reexecutor is required");
        this.contextProvider = Objects.requireNonNull(contextProvider, "contextProvider is required");
    }

    @Override
    public KnowledgeReplayResult replay(KnowledgeReplayRequest request) {
        var original = traceService.get(request.traceId());
        if (original.isEmpty()) {
            return new KnowledgeReplayResult(
                    UUID.randomUUID().toString(),
                    request.traceId(),
                    KnowledgeReplayStatus.INCOMPLETE,
                    null,
                    null,
                    List.of("original-trace-not-found"),
                    java.util.Map.of()
            );
        }

        KnowledgeDecisionTrace originalTrace = original.get();
        KnowledgeReplaySnapshot snapshot = KnowledgeReplaySnapshotFactory.fromTrace(originalTrace);
        KnowledgeReplayContext context = contextProvider.context(originalTrace, request);
        KnowledgeDecisionTrace replayed = reexecutor.execute(snapshot, context);

        List<String> divergences = KnowledgeDecisionTraceComparator.compare(originalTrace, replayed);
        KnowledgeReplayStatus status = divergences.isEmpty()
                ? KnowledgeReplayStatus.REPRODUCED
                : KnowledgeReplayStatus.DIVERGED;

        return new KnowledgeReplayResult(
                UUID.randomUUID().toString(),
                request.traceId(),
                status,
                originalTrace,
                replayed,
                divergences,
                java.util.Map.of("mode", request.mode().name())
        );
    }
}
