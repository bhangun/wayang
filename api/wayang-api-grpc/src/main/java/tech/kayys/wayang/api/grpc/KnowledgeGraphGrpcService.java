package tech.kayys.wayang.api.grpc;

import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;
import tech.kayys.wayang.knowledge.exchange.compact.InMemoryKnowledgeAnswerArtifactGraphStore;
import tech.kayys.wayang.knowledge.exchange.selection.InMemoryKnowledgeAnswerResolutionDependencyGraph;
import tech.kayys.wayang.knowledge.graph.DefaultKnowledgeGraphProjectionService;
import tech.kayys.wayang.knowledge.graph.KnowledgeGraphProjectionService;
import tech.kayys.wayang.knowledge.graph.KnowledgeGraphQuery;
import tech.kayys.wayang.knowledge.graph.KnowledgeGraphView;
import tech.kayys.wayang.knowledge.lineage.InMemoryKnowledgeLineageStore;
import tech.kayys.wayang.knowledge.snapshot.dependency.InMemoryKnowledgeSnapshotDependencyGraph;

/**
 * gRPC service implementation for knowledge graph visualization.
 *
 * <p>Implements {@code KnowledgeGraphService} from {@code wayang_api.proto}.
 * The lineage endpoint uses server-side streaming to handle large graphs.</p>
 */
@GrpcService
public class KnowledgeGraphGrpcService extends KnowledgeGraphServiceGrpc.KnowledgeGraphServiceImplBase {

    private final KnowledgeGraphProjectionService projectionService;

    public KnowledgeGraphGrpcService() {
        this.projectionService = new DefaultKnowledgeGraphProjectionService(
                new InMemoryKnowledgeAnswerArtifactGraphStore(),
                new InMemoryKnowledgeAnswerResolutionDependencyGraph(),
                new InMemoryKnowledgeLineageStore(),
                new InMemoryKnowledgeSnapshotDependencyGraph()
        );
    }

    @Override
    public void getArtifactGraph(KnowledgeGraphRequest request,
                                  StreamObserver<KnowledgeGraphResponse> observer) {
        try {
            KnowledgeGraphView view = projectionService.artifactGraph(toQuery(request));
            observer.onNext(toResponse(view));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(e);
        }
    }

    @Override
    public void getProvenanceGraph(KnowledgeGraphRequest request,
                                    StreamObserver<KnowledgeGraphResponse> observer) {
        try {
            KnowledgeGraphView view = projectionService.provenanceGraph(toQuery(request));
            observer.onNext(toResponse(view));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(e);
        }
    }

    @Override
    public void getClaimGraph(KnowledgeGraphRequest request,
                               StreamObserver<KnowledgeGraphResponse> observer) {
        try {
            KnowledgeGraphView view = projectionService.claimGraph(toQuery(request));
            observer.onNext(toResponse(view));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(e);
        }
    }

    @Override
    public void getFusionGraph(KnowledgeGraphRequest request,
                                StreamObserver<KnowledgeGraphResponse> observer) {
        try {
            KnowledgeGraphView view = projectionService.fusionGraph(toQuery(request));
            observer.onNext(toResponse(view));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(e);
        }
    }

    @Override
    public void getResolutionDependencyGraph(KnowledgeGraphRequest request,
                                              StreamObserver<KnowledgeGraphResponse> observer) {
        try {
            KnowledgeGraphView view = projectionService.resolutionDependencyGraph(toQuery(request));
            observer.onNext(toResponse(view));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(e);
        }
    }

    /**
     * Server-side streaming: each lineage edge is streamed individually for large graphs.
     */
    @Override
    public void getLineageGraph(KnowledgeLineageGraphRequest request,
                                 StreamObserver<KnowledgeGraphEdgeProto> observer) {
        try {
            int maxDepth = request.getMaxDepth() > 0 ? request.getMaxDepth() : 5;
            KnowledgeGraphQuery query = new KnowledgeGraphQuery(
                    request.getNodeId(), request.getTenantId(), request.getWorkspaceId(),
                    request.getProjectId(), request.getSessionId(), maxDepth);

            KnowledgeGraphView view = projectionService.lineageGraph(query);
            for (tech.kayys.wayang.knowledge.graph.KnowledgeGraphEdge edge : view.edges()) {
                observer.onNext(KnowledgeGraphEdgeProto.newBuilder()
                        .setEdgeId(edge.id())
                        .setSource(edge.source())
                        .setTarget(edge.target())
                        .setRelation(edge.relation())
                        .setConfidence(edge.confidence())
                        .build());
            }
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(e);
        }
    }

    @Override
    public void getSnapshotDependencyGraph(KnowledgeGraphRequest request,
                                            StreamObserver<KnowledgeGraphResponse> observer) {
        try {
            KnowledgeGraphView view = projectionService.snapshotDependencyGraph(toQuery(request));
            observer.onNext(toResponse(view));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(e);
        }
    }

    @Override
    public void getFullGraph(KnowledgeFullGraphRequest request,
                              StreamObserver<KnowledgeGraphResponse> observer) {
        try {
            int maxDepth = request.getMaxDepth() > 0 ? request.getMaxDepth() : 5;
            KnowledgeGraphQuery query = new KnowledgeGraphQuery(
                    request.getWorkspaceId(), request.getTenantId(), request.getWorkspaceId(),
                    request.getProjectId(), request.getSessionId(), maxDepth);
            KnowledgeGraphView view = projectionService.fullGraph(query);
            observer.onNext(toResponse(view));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(e);
        }
    }

    // ─── Mapping helpers ──────────────────────────────────────────────────────

    private KnowledgeGraphQuery toQuery(KnowledgeGraphRequest req) {
        int maxDepth = req.getMaxDepth() > 0 ? req.getMaxDepth() : 5;
        return new KnowledgeGraphQuery(
                req.getEntityId(), req.getTenantId(), req.getWorkspaceId(),
                req.getProjectId().isBlank() ? null : req.getProjectId(),
                req.getSessionId().isBlank() ? null : req.getSessionId(),
                maxDepth);
    }

    private KnowledgeGraphResponse toResponse(KnowledgeGraphView view) {
        KnowledgeGraphResponse.Builder builder = KnowledgeGraphResponse.newBuilder()
                .setGraphId(view.graphId())
                .setGraphType(view.graphType().name())
                .setTenantId(safe(view.tenantId()))
                .setWorkspaceId(safe(view.workspaceId()))
                .setProjectId(safe(view.projectId()))
                .setSessionId(safe(view.sessionId()))
                .setGeneratedAt(view.generatedAt().toString())
                .setStats(KnowledgeGraphStatsProto.newBuilder()
                        .setNodeCount(view.stats().nodeCount())
                        .setEdgeCount(view.stats().edgeCount())
                        .build());

        for (tech.kayys.wayang.knowledge.graph.KnowledgeGraphNode n : view.nodes()) {
            builder.addNodes(KnowledgeGraphNodeProto.newBuilder()
                    .setGraphNodeId(n.id())
                    .setType(n.type())
                    .setLabel(n.label())
                    .setWeight(n.weight())
                    .setTenantId(safe(n.tenantId()))
                    .setWorkspaceId(safe(n.workspaceId()))
                    .setProjectId(safe(n.projectId()))
                    .build());
        }

        for (tech.kayys.wayang.knowledge.graph.KnowledgeGraphEdge e : view.edges()) {
            builder.addEdges(KnowledgeGraphEdgeProto.newBuilder()
                    .setEdgeId(e.id())
                    .setSource(e.source())
                    .setTarget(e.target())
                    .setRelation(e.relation())
                    .setConfidence(e.confidence())
                    .build());
        }

        return builder.build();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
