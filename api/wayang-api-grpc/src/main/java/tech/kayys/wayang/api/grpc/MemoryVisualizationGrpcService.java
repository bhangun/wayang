package tech.kayys.wayang.api.grpc;

import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;
import tech.kayys.wayang.memory.visual.*;

/**
 * gRPC service implementation for Memory Visualization.
 *
 * <p>Implements {@code MemoryVisualizationService} defined in {@code wayang_api.proto}.
 * Supports 4-tier overview, concept graph, vector maps, working memory, and streaming vector points.</p>
 */
@GrpcService
public class MemoryVisualizationGrpcService extends MemoryVisualizationServiceGrpc.MemoryVisualizationServiceImplBase {

    private final MemoryVisualProjectionService projectionService;

    public MemoryVisualizationGrpcService() {
        this.projectionService = new DefaultMemoryVisualProjectionService();
    }

    public MemoryVisualizationGrpcService(MemoryVisualProjectionService projectionService) {
        this.projectionService = projectionService;
    }

    @Override
    public void getMemoryOverview(MemoryVisualProtoQuery request,
                                  StreamObserver<MemoryVisualProtoResponse> observer) {
        try {
            MemoryVisualView view = projectionService.overview(toQuery(request));
            observer.onNext(toResponse(view));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(e);
        }
    }

    @Override
    public void getMemoryGraph(MemoryVisualProtoQuery request,
                                StreamObserver<MemoryVisualProtoResponse> observer) {
        try {
            MemoryVisualView view = projectionService.graph(toQuery(request));
            observer.onNext(toResponse(view));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(e);
        }
    }

    @Override
    public void getVectorMap(MemoryVisualProtoQuery request,
                             StreamObserver<MemoryVisualProtoResponse> observer) {
        try {
            MemoryVisualView view = projectionService.vectors(toQuery(request));
            observer.onNext(toResponse(view));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(e);
        }
    }

    @Override
    public void getWorkingMemory(MemoryVisualProtoQuery request,
                                 StreamObserver<MemoryVisualProtoResponse> observer) {
        try {
            MemoryVisualView view = projectionService.working(toQuery(request));
            observer.onNext(toResponse(view));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(e);
        }
    }

    @Override
    public void getProceduralMemory(MemoryVisualProtoQuery request,
                                    StreamObserver<MemoryVisualProtoResponse> observer) {
        try {
            MemoryVisualView view = projectionService.procedural(toQuery(request));
            observer.onNext(toResponse(view));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(e);
        }
    }

    @Override
    public void getMemoryTimeline(MemoryVisualProtoQuery request,
                                  StreamObserver<MemoryVisualProtoResponse> observer) {
        try {
            MemoryVisualView view = projectionService.timeline(toQuery(request));
            observer.onNext(toResponse(view));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(e);
        }
    }

    @Override
    public void getFullMemoryView(MemoryVisualProtoQuery request,
                                  StreamObserver<MemoryVisualProtoResponse> observer) {
        try {
            MemoryVisualView view = projectionService.full(toQuery(request));
            observer.onNext(toResponse(view));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(e);
        }
    }

    /**
     * Server-side streaming of vector scatter points for large memory sets.
     */
    @Override
    public void streamVectorPoints(MemoryVisualProtoQuery request,
                                   StreamObserver<MemoryVectorPointProto> observer) {
        try {
            MemoryVisualView view = projectionService.vectors(toQuery(request));
            for (MemoryVectorPoint p : view.vectorPoints()) {
                observer.onNext(MemoryVectorPointProto.newBuilder()
                        .setPointId(p.id())
                        .setX(p.x())
                        .setY(p.y())
                        .setZ(p.z())
                        .setCategory(safe(p.category()))
                        .setLabel(safe(p.label()))
                        .setRelevance(p.relevance())
                        .build());
            }
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(e);
        }
    }

    // ─── Mapping helpers ──────────────────────────────────────────────────────

    private MemoryVisualQuery toQuery(MemoryVisualProtoQuery req) {
        int limit = req.getLimit() > 0 ? req.getLimit() : MemoryVisualQuery.DEFAULT_LIMIT;
        int dims = req.getDimensions() == 2 || req.getDimensions() == 3 ? req.getDimensions() : MemoryVisualQuery.DEFAULT_DIMENSIONS;
        return new MemoryVisualQuery(
                req.getAgentId().isBlank() ? "default" : req.getAgentId(),
                req.getUserId().isBlank() ? null : req.getUserId(),
                req.getTenantId().isBlank() ? "default" : req.getTenantId(),
                req.getWorkspaceId().isBlank() ? "default" : req.getWorkspaceId(),
                req.getSessionId().isBlank() ? null : req.getSessionId(),
                req.getCategory().isBlank() ? null : req.getCategory(),
                limit,
                dims
        );
    }

    private MemoryVisualProtoResponse toResponse(MemoryVisualView view) {
        MemoryVisualProtoResponse.Builder b = MemoryVisualProtoResponse.newBuilder()
                .setViewId(view.viewId())
                .setViewType(view.viewType().name())
                .setAgentId(safe(view.agentId()))
                .setUserId(safe(view.userId()))
                .setTenantId(safe(view.tenantId()))
                .setWorkspaceId(safe(view.workspaceId()))
                .setSessionId(safe(view.sessionId()))
                .setGeneratedAt(view.generatedAt().toString());

        if (view.hierarchy() != null) {
            MemoryHierarchySummary h = view.hierarchy();
            MemoryHierarchySummaryProto.Builder hb = MemoryHierarchySummaryProto.newBuilder()
                    .setWorkingActiveItems(h.workingMemoryActiveItems())
                    .setEpisodicRecentCount(h.episodicMemoryRecentCount())
                    .setEpisodicCoherenceScore(h.episodicCoherenceScore())
                    .setSemanticConceptCount(h.semanticConceptCount())
                    .setSemanticFactCount(h.semanticFactCount())
                    .setProceduralPatternCount(h.proceduralPatternCount())
                    .setProceduralAvgProficiency(h.proceduralAvgProficiency())
                    .setLongTermTotalRecords(h.longTermTotalRecords())
                    .setLongTermSizeBytes(h.longTermSizeBytes());

            if (h.categoryDistribution() != null) {
                hb.putAllCategoryDistribution(h.categoryDistribution());
            }
            b.setHierarchy(hb.build());
        }

        if (view.workingState() != null) {
            MemoryWorkingState ws = view.workingState();
            MemoryWorkingStateProto.Builder wsb = MemoryWorkingStateProto.newBuilder()
                    .setSessionId(safe(ws.sessionId()))
                    .setCurrentTask(safe(ws.currentTask()))
                    .setTokenBudgetRemaining(ws.tokenBudgetRemaining());

            if (ws.attentionWeights() != null) {
                wsb.putAllAttentionWeights(ws.attentionWeights());
            }
            if (ws.activeFacts() != null) {
                wsb.addAllActiveFacts(ws.activeFacts());
            }
            if (ws.activePatterns() != null) {
                wsb.addAllActivePatterns(ws.activePatterns());
            }
            b.setWorkingState(wsb.build());
        }

        for (MemoryVisualNode n : view.nodes()) {
            b.addNodes(MemoryVisualNodeProto.newBuilder()
                    .setNodeId(n.id())
                    .setType(n.type())
                    .setLabel(n.label())
                    .setWeight(n.weight())
                    .setCluster(safe(n.cluster()))
                    .setTier(safe(n.tier()))
                    .build());
        }

        for (MemoryVisualEdge e : view.edges()) {
            b.addEdges(MemoryVisualEdgeProto.newBuilder()
                    .setEdgeId(e.id())
                    .setSource(e.source())
                    .setTarget(e.target())
                    .setRelation(e.relation())
                    .setConfidence(e.confidence())
                    .build());
        }

        for (MemoryVectorPoint p : view.vectorPoints()) {
            b.addVectorPoints(MemoryVectorPointProto.newBuilder()
                    .setPointId(p.id())
                    .setX(p.x())
                    .setY(p.y())
                    .setZ(p.z())
                    .setCategory(safe(p.category()))
                    .setLabel(safe(p.label()))
                    .setRelevance(p.relevance())
                    .build());
        }

        for (MemoryVisualInsight in : view.insights()) {
            b.addInsights(MemoryVisualInsightProto.newBuilder()
                    .setType(in.type())
                    .setDescription(in.description())
                    .setConfidence(in.confidence())
                    .build());
        }

        return b.build();
    }

    private String safe(String val) {
        return val == null ? "" : val;
    }
}
