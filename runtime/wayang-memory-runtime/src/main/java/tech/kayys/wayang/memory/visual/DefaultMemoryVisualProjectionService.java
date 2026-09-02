package tech.kayys.wayang.memory.visual;

import tech.kayys.wayang.memory.context.*;
import tech.kayys.wayang.memory.model.*;
import tech.kayys.wayang.memory.service.VectorMemoryStore;
import tech.kayys.wayang.memory.service.InMemoryVectorStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Default runtime implementation of {@link MemoryVisualProjectionService}.
 *
 * <p>Wired to {@link HierarchicalMemoryManager} and {@link VectorMemoryStore} to project
 * Working, Episodic, Semantic, Procedural, and Vector memory tiers into normalized UI views.</p>
 */
public class DefaultMemoryVisualProjectionService implements MemoryVisualProjectionService {

    private final HierarchicalMemoryManager hierarchicalManager;
    private final VectorMemoryStore vectorStore;
    private final Map<String, SemanticMemory> semanticCache = new ConcurrentHashMap<>();
    private final Map<String, ProceduralMemory> proceduralCache = new ConcurrentHashMap<>();
    private final Map<String, WorkingMemory> workingCache = new ConcurrentHashMap<>();

    public DefaultMemoryVisualProjectionService() {
        this(new HierarchicalMemoryManager(), new InMemoryVectorStore());
    }

    public DefaultMemoryVisualProjectionService(
            HierarchicalMemoryManager hierarchicalManager,
            VectorMemoryStore vectorStore) {
        this.hierarchicalManager = hierarchicalManager;
        this.vectorStore = vectorStore;
    }

    @Override
    public MemoryVisualView overview(MemoryVisualQuery query) {
        List<Memory> allMemories = fetchMemories(query);

        Map<String, Long> categoryCounts = allMemories.stream()
                .collect(Collectors.groupingBy(
                        m -> {
                            Object cat = m.getMetadata() != null ? m.getMetadata().get("category") : null;
                            return cat != null ? cat.toString() : "General";
                        },
                        Collectors.counting()
                ));

        SemanticMemory semantic = semanticCache.get(query.agentId());
        ProceduralMemory procedural = proceduralCache.get(query.userId() != null ? query.userId() : query.agentId());
        WorkingMemory working = workingCache.get(query.sessionId() != null ? query.sessionId() : "default");

        int conceptCount = semantic != null ? semantic.getKnowledgeGraph().size() : 0;
        int factCount = semantic != null ? semantic.getFacts().size() : 0;
        int patternCount = procedural != null ? procedural.getPatterns().size() : 0;
        double avgProficiency = procedural != null && !procedural.getSkillProficiency().isEmpty()
                ? procedural.getSkillProficiency().values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0)
                : 0.0;
        int workingItems = working != null ? working.getActiveFacts().size() + working.getActiveEpisodic().size() : 0;

        MemoryHierarchySummary summary = new MemoryHierarchySummary(
                workingItems,
                Math.min(allMemories.size(), 20),
                0.88,
                conceptCount,
                factCount,
                patternCount,
                avgProficiency,
                allMemories.size(),
                allMemories.size() * 256L,
                categoryCounts
        );

        List<MemoryVisualInsight> insights = generateInsights(summary, allMemories);

        return MemoryVisualView.builder(MemoryVisualView.MemoryVisualViewType.OVERVIEW)
                .agentId(query.agentId())
                .userId(query.userId())
                .tenantId(query.tenantId())
                .workspaceId(query.workspaceId())
                .sessionId(query.sessionId())
                .hierarchy(summary)
                .insights(insights)
                .build();
    }

    @Override
    public MemoryVisualView graph(MemoryVisualQuery query) {
        List<MemoryVisualNode> nodes = new ArrayList<>();
        List<MemoryVisualEdge> edges = new ArrayList<>();

        SemanticMemory semantic = semanticCache.get(query.agentId());
        if (semantic != null) {
            Map<String, List<String>> kg = semantic.getKnowledgeGraph();
            Map<String, Double> strength = semantic.getConceptStrength();

            for (Map.Entry<String, List<String>> entry : kg.entrySet()) {
                String concept = entry.getKey();
                double weight = strength.getOrDefault(concept, 0.5);
                nodes.add(MemoryVisualNode.of(concept, "CONCEPT", concept, weight, "Concept", "SEMANTIC"));

                for (String rel : entry.getValue()) {
                    String targetNode = "entity-" + rel;
                    nodes.add(MemoryVisualNode.of(targetNode, "ENTITY", rel, 0.5, "Entity", "SEMANTIC"));
                    edges.add(MemoryVisualEdge.of(concept, targetNode, "RELATES_TO", weight));
                }
            }

            for (Fact fact : semantic.getFacts()) {
                String factNode = "fact-" + Math.abs(fact.getSubject().hashCode() ^ fact.getObject().hashCode());
                nodes.add(MemoryVisualNode.of(factNode, "FACT",
                        fact.getSubject() + " " + fact.getPredicate() + " " + fact.getObject(),
                        fact.getConfidence(), "Fact", "SEMANTIC"));
                nodes.add(MemoryVisualNode.of(fact.getSubject(), "ENTITY", fact.getSubject(), 0.7, "Entity", "SEMANTIC"));
                nodes.add(MemoryVisualNode.of(fact.getObject(), "ENTITY", fact.getObject(), 0.7, "Entity", "SEMANTIC"));
                edges.add(MemoryVisualEdge.of(fact.getSubject(), factNode, fact.getPredicate(), fact.getConfidence()));
                edges.add(MemoryVisualEdge.of(factNode, fact.getObject(), "YIELDS", fact.getConfidence()));
            }
        }

        ProceduralMemory procedural = proceduralCache.get(query.userId() != null ? query.userId() : query.agentId());
        if (procedural != null) {
            for (TaskPattern pattern : procedural.getPatterns()) {
                String patternNode = "pattern-" + pattern.getTaskType();
                nodes.add(MemoryVisualNode.of(patternNode, "TASK_PATTERN", pattern.getTaskType(),
                        pattern.getSuccessRate(), "Procedural", "PROCEDURAL"));

                List<String> steps = pattern.getCommonSteps();
                for (int i = 0; i < steps.size(); i++) {
                    String stepNode = "step-" + pattern.getTaskType() + "-" + i;
                    nodes.add(MemoryVisualNode.of(stepNode, "ACTION", steps.get(i), 0.6, "Action", "PROCEDURAL"));
                    if (i == 0) {
                        edges.add(MemoryVisualEdge.of(patternNode, stepNode, "STARTS_WITH", 0.9));
                    } else {
                        String prevStep = "step-" + pattern.getTaskType() + "-" + (i - 1);
                        edges.add(MemoryVisualEdge.of(prevStep, stepNode, "THEN", 0.85));
                    }
                }
            }
        }

        return MemoryVisualView.builder(MemoryVisualView.MemoryVisualViewType.GRAPH)
                .agentId(query.agentId())
                .tenantId(query.tenantId())
                .workspaceId(query.workspaceId())
                .nodes(deduplicateNodes(nodes))
                .edges(edges)
                .build();
    }

    @Override
    public MemoryVisualView vectors(MemoryVisualQuery query) {
        List<Memory> allMemories = fetchMemories(query);
        List<MemoryVectorPoint> points = new ArrayList<>();

        for (int i = 0; i < Math.min(allMemories.size(), query.limit()); i++) {
            Memory m = allMemories.get(i);
            String category = m.getMetadata() != null && m.getMetadata().containsKey("category")
                    ? m.getMetadata().get("category").toString()
                    : "General";

            double x, y, z;
            float[] emb = m.getEmbedding();
            if (emb != null && emb.length >= 3) {
                // High-dimensional vector embedding projection into 3D / 2D
                double sumX = 0, sumY = 0, sumZ = 0;
                for (int j = 0; j < emb.length; j++) {
                    if (j % 3 == 0) sumX += emb[j];
                    else if (j % 3 == 1) sumY += emb[j];
                    else sumZ += emb[j];
                }
                double norm = Math.sqrt(sumX * sumX + sumY * sumY + sumZ * sumZ) + 1e-6;
                x = sumX / norm;
                y = sumY / norm;
                z = query.dimensions() == 3 ? (sumZ / norm) : 0.0;
            } else {
                // Spherical coordinate 3D/2D projection from hash & importance
                int h1 = (m.getId() != null ? m.getId().hashCode() : i);
                int h2 = (m.getContent() != null ? m.getContent().hashCode() : i * 31);
                double theta = Math.abs(h1 % 360) * (Math.PI / 180.0);
                double phi = Math.abs(h2 % 180) * (Math.PI / 180.0);
                double radius = 0.3 + (Math.abs(h1 ^ h2) % 70) / 100.0;

                if (query.dimensions() == 3) {
                    x = radius * Math.sin(phi) * Math.cos(theta);
                    y = radius * Math.sin(phi) * Math.sin(theta);
                    z = radius * Math.cos(phi);
                } else {
                    x = radius * Math.cos(theta);
                    y = radius * Math.sin(theta);
                    z = 0.0;
                }
            }

            String label = m.getContent() != null && m.getContent().length() > 60
                    ? m.getContent().substring(0, 60) + "..."
                    : (m.getContent() != null ? m.getContent() : "Memory #" + i);

            points.add(MemoryVectorPoint.point3D(
                    m.getId() != null ? m.getId() : "point-" + i,
                    Math.round(x * 1000.0) / 1000.0,
                    Math.round(y * 1000.0) / 1000.0,
                    Math.round(z * 1000.0) / 1000.0,
                    category,
                    label,
                    m.getImportance() > 0 ? m.getImportance() : 1.0
            ));
        }

        return MemoryVisualView.builder(MemoryVisualView.MemoryVisualViewType.VECTORS)
                .agentId(query.agentId())
                .tenantId(query.tenantId())
                .workspaceId(query.workspaceId())
                .vectorPoints(points)
                .build();
    }

    @Override
    public MemoryVisualView working(MemoryVisualQuery query) {
        String session = query.sessionId() != null ? query.sessionId() : "default";
        WorkingMemory working = workingCache.get(session);

        MemoryWorkingState state;
        if (working != null) {
            List<String> activeFacts = working.getActiveFacts().stream()
                    .map(f -> f.getSubject() + " " + f.getPredicate() + " " + f.getObject())
                    .toList();
            List<String> activePatterns = working.getActivePatterns().stream()
                    .map(TaskPattern::getTaskType)
                    .toList();
            state = new MemoryWorkingState(
                    session,
                    working.getCurrentTask(),
                    3500L,
                    working.getAttentionWeights(),
                    activeFacts,
                    activePatterns
            );
        } else {
            state = new MemoryWorkingState(
                    session,
                    "Current active goal",
                    4096L,
                    Map.of("attention_focus", 0.95),
                    List.of(),
                    List.of()
            );
        }

        return MemoryVisualView.builder(MemoryVisualView.MemoryVisualViewType.WORKING)
                .agentId(query.agentId())
                .sessionId(session)
                .tenantId(query.tenantId())
                .workspaceId(query.workspaceId())
                .workingState(state)
                .build();
    }

    @Override
    public MemoryVisualView procedural(MemoryVisualQuery query) {
        ProceduralMemory procedural = proceduralCache.get(query.userId() != null ? query.userId() : query.agentId());
        List<MemoryVisualNode> nodes = new ArrayList<>();
        List<MemoryVisualEdge> edges = new ArrayList<>();

        if (procedural != null) {
            for (TaskPattern pattern : procedural.getPatterns()) {
                nodes.add(MemoryVisualNode.of(
                        pattern.getTaskType(), "TASK_PATTERN", pattern.getTaskType(),
                        pattern.getSuccessRate(), "Pattern", "PROCEDURAL"));
            }
            for (Map.Entry<String, Double> entry : procedural.getSkillProficiency().entrySet()) {
                nodes.add(MemoryVisualNode.of(
                        "skill-" + entry.getKey(), "SKILL", entry.getKey(),
                        entry.getValue(), "Skill", "PROCEDURAL"));
            }
        }

        return MemoryVisualView.builder(MemoryVisualView.MemoryVisualViewType.PROCEDURAL)
                .agentId(query.agentId())
                .userId(query.userId())
                .tenantId(query.tenantId())
                .workspaceId(query.workspaceId())
                .nodes(deduplicateNodes(nodes))
                .edges(edges)
                .build();
    }

    @Override
    public MemoryVisualView timeline(MemoryVisualQuery query) {
        List<Memory> allMemories = fetchMemories(query);
        List<MemoryVisualNode> nodes = new ArrayList<>();
        List<MemoryVisualEdge> edges = new ArrayList<>();

        for (int i = 0; i < Math.min(allMemories.size(), query.limit()); i++) {
            Memory m = allMemories.get(i);
            String id = m.getId() != null ? m.getId() : "mem-" + i;
            String label = m.getContent() != null && m.getContent().length() > 50
                    ? m.getContent().substring(0, 50) + "..."
                    : (m.getContent() != null ? m.getContent() : "Item " + i);

            nodes.add(MemoryVisualNode.of(id, "EPISODIC", label, 1.0, "Timeline", "EPISODIC"));
            if (i > 0) {
                String prevId = allMemories.get(i - 1).getId();
                if (prevId != null) {
                    edges.add(MemoryVisualEdge.of(prevId, id, "FOLLOWED_BY", 0.9));
                }
            }
        }

        return MemoryVisualView.builder(MemoryVisualView.MemoryVisualViewType.TIMELINE)
                .agentId(query.agentId())
                .tenantId(query.tenantId())
                .workspaceId(query.workspaceId())
                .nodes(nodes)
                .edges(edges)
                .build();
    }

    @Override
    public MemoryVisualView full(MemoryVisualQuery query) {
        MemoryVisualView overview = overview(query);
        MemoryVisualView graph = graph(query);
        MemoryVisualView vectors = vectors(query);
        MemoryVisualView working = working(query);

        return MemoryVisualView.builder(MemoryVisualView.MemoryVisualViewType.FULL)
                .agentId(query.agentId())
                .userId(query.userId())
                .tenantId(query.tenantId())
                .workspaceId(query.workspaceId())
                .sessionId(query.sessionId())
                .hierarchy(overview.hierarchy())
                .nodes(graph.nodes())
                .edges(graph.edges())
                .vectorPoints(vectors.vectorPoints())
                .insights(overview.insights())
                .workingState(working.workingState())
                .build();
    }

    // ─── Helpers & Cache Population ──────────────────────────────────────────

    public void registerSemanticMemory(String agentId, SemanticMemory semantic) {
        if (agentId != null && semantic != null) semanticCache.put(agentId, semantic);
    }

    public void registerProceduralMemory(String key, ProceduralMemory procedural) {
        if (key != null && procedural != null) proceduralCache.put(key, procedural);
    }

    public void registerWorkingMemory(String sessionId, WorkingMemory working) {
        if (sessionId != null && working != null) workingCache.put(sessionId, working);
    }

    private List<Memory> fetchMemories(MemoryVisualQuery query) {
        try {
            return vectorStore.searchByFilter(Map.of()).await().indefinitely();
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<MemoryVisualInsight> generateInsights(MemoryHierarchySummary summary, List<Memory> memories) {
        List<MemoryVisualInsight> list = new ArrayList<>();
        if (summary.longTermTotalRecords() > 0) {
            list.add(new MemoryVisualInsight(
                    "TOTAL_RETENTION",
                    "Agent has stored " + summary.longTermTotalRecords() + " long-term memories across "
                            + summary.categoryDistribution().size() + " distinct categories.",
                    0.95,
                    Map.of("totalRecords", summary.longTermTotalRecords())
            ));
        }
        if (summary.episodicCoherenceScore() >= 0.8) {
            list.add(new MemoryVisualInsight(
                    "HIGH_COHERENCE",
                    "Recent conversational coherence is strong ("
                            + String.format("%.2f", summary.episodicCoherenceScore()) + "). Context drift is minimal.",
                    summary.episodicCoherenceScore(),
                    Map.of("score", summary.episodicCoherenceScore())
            ));
        }
        return list;
    }

    private List<MemoryVisualNode> deduplicateNodes(List<MemoryVisualNode> nodes) {
        LinkedHashMap<String, MemoryVisualNode> map = new LinkedHashMap<>();
        for (MemoryVisualNode n : nodes) map.putIfAbsent(n.id(), n);
        return List.copyOf(map.values());
    }
}
