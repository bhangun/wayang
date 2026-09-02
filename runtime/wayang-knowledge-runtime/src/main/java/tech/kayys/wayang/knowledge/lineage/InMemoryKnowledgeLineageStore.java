package tech.kayys.wayang.knowledge.lineage;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryKnowledgeLineageStore implements KnowledgeLineageStore {

    private final Map<String, List<KnowledgeLineageEdge>> edges = new ConcurrentHashMap<>();
    private final Map<String, EvidenceBundle> bundles = new ConcurrentHashMap<>();

    @Override
    public CompletionStage<KnowledgeLineageEdge> save(KnowledgeLineageEdge edge) {
        if (edge == null) return CompletableFuture.failedFuture(new IllegalArgumentException("Edge is null"));
        edges.computeIfAbsent(edge.sourceId(), k -> Collections.synchronizedList(new ArrayList<>())).add(edge);
        edges.computeIfAbsent(edge.targetId(), k -> Collections.synchronizedList(new ArrayList<>())).add(edge);
        return CompletableFuture.completedFuture(edge);
    }

    @Override
    public CompletionStage<List<KnowledgeLineageEdge>> getEdgesFor(String nodeId) {
        return CompletableFuture.completedFuture(List.copyOf(edges.getOrDefault(nodeId, List.of())));
    }

    @Override
    public CompletionStage<List<KnowledgeLineageEdge>> getAncestors(String nodeId, int maxDepth) {
        Set<String> visited = new HashSet<>();
        List<KnowledgeLineageEdge> result = new ArrayList<>();
        collectAncestors(nodeId, maxDepth, visited, result);
        return CompletableFuture.completedFuture(result);
    }

    private void collectAncestors(String current, int depthRemaining, Set<String> visited, List<KnowledgeLineageEdge> result) {
        if (depthRemaining <= 0 || visited.contains(current)) return;
        visited.add(current);
        List<KnowledgeLineageEdge> connected = edges.getOrDefault(current, List.of());
        for (KnowledgeLineageEdge edge : connected) {
            if (edge.sourceId().equals(current)) {
                result.add(edge);
                collectAncestors(edge.targetId(), depthRemaining - 1, visited, result);
            }
        }
    }

    @Override
    public CompletionStage<EvidenceBundle> saveBundle(EvidenceBundle bundle) {
        bundles.put(bundle.bundleId(), bundle);
        return CompletableFuture.completedFuture(bundle);
    }
}
